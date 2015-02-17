package de.stonebone.cars.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.stonebone.cars.ControllerState;
import de.stonebone.cars.ServerState;

public class Server implements Runnable {

  public static void main(String[] args) {
    new Server().run();
  }

  private final ByteBuffer buffer;
  private final Map<SocketAddress, Token> map;
  private final int port;
  private boolean running;
  private final EnumSet<Token> set;
  private final ServerState state;
  private final long tokenNanosToLive;

  public Server() {
    this(4, 4231, 10);
  }

  public Server(int maxControllers, int port, int tokenSecondsToLive) {
    this.port = port;
    this.tokenNanosToLive = TimeUnit.SECONDS.toNanos(tokenSecondsToLive);
    this.state = new ServerState(maxControllers);
    this.map = new HashMap<>();
    this.set = EnumSet.range(Token.A, Token.values()[maxControllers - 1]);

    if (maxControllers != set.size())
      throw new IllegalStateException(String.format("Expected %d, but got %d controller tokens?!", maxControllers, set.size()));

    this.buffer = ByteBuffer.allocateDirect(ControllerState.CAPACITY);
  }

  public void close() {
    this.running = false;
  }

  public int getPort() {
    return port;
  }

  public ServerState getServerState() {
    return state;
  }

  public void handleChannel(DatagramChannel channel) throws IOException {
    assert !channel.isBlocking();
    assert channel.getLocalAddress() != null;
    buffer.clear();
    SocketAddress address = channel.receive(buffer);
    if (address == null)
      return;
    buffer.flip();
    handleDatagram(address, buffer);
  }

  private void handleDatagram(SocketAddress address, ByteBuffer buffer) {
    long nanos = System.nanoTime();

    Token token = map.get(address);
    if (token == null) {
      releaseTokens(nanos);
      if (set.isEmpty())
        return;
      token = set.iterator().next();
      if (token == null)
        throw new RuntimeException("Null token?! Should not happen in an enum set!");
      set.remove(token);
      map.put(address, token);
      ControllerState controller = state.getControllers()[token.ordinal()];
      controller.setSocketAddress(address);
      controller.setTouched(nanos);
    }

    ControllerState controller = state.getControllers()[token.ordinal()];

    int serial = buffer.getInt(0);
    if (serial <= controller.getSerial())
      return;

    controller.setTouched(nanos);
    controller.fromByteBuffer(buffer);
  }

  public DatagramChannel open() throws IOException {
    DatagramChannel channel = DatagramChannel.open();
    channel.configureBlocking(false);
    channel.bind(new InetSocketAddress(port));
    return channel;
  }

  private void releaseTokens(long currentNanos) {
    List<SocketAddress> release = new ArrayList<>();
    for (Token token : map.values()) {
      ControllerState controller = state.getControllers()[token.ordinal()];
      long touched = controller.getTouched();
      if (touched == 0)
        continue;
      long age = currentNanos - touched;
      if (age < tokenNanosToLive)
        continue;
      controller.setSocketAddress(null);
      controller.setTouched(0);
      controller.setSerial(0);
      set.add(token);
      release.add(controller.getSocketAddress());
    }
    if (release.size() > 0) {
      for (SocketAddress address : release)
        map.remove(address);
    }
  }

  public void run() {
    try (DatagramChannel channel = open()) {

      running = true;
      while (running) {
        handleChannel(channel);
        Thread.yield();
      }

    } catch (Exception e) {
      throw new RuntimeException("Exception in run()!", e);
    }
  }

}
