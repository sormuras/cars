package de.stonebone.cars.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
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

  private final ByteBuffer controllerStateBuffer;
  private final DatagramPacket controllerStatePacket;
  private final Map<SocketAddress, Token> map;
  private final int port;
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

    this.controllerStateBuffer = ByteBuffer.allocate(ControllerState.CAPACITY);
    this.controllerStatePacket = new DatagramPacket(controllerStateBuffer.array(), controllerStateBuffer.capacity());
  }

  public ServerState getServerState() {
    return state;
  }

  private void handlePacket() throws Exception {

    // System.out.println("packet: " + controllerStatePacket);

    long nanos = System.nanoTime();

    SocketAddress address = controllerStatePacket.getSocketAddress();
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

    controllerStateBuffer.clear();
    int serial = controllerStateBuffer.getInt(0);
    if (serial <= controller.getSerial())
      return;

    controller.setTouched(nanos);
    controllerStateBuffer.limit(controllerStatePacket.getLength());
    controller.fromByteBuffer(controllerStateBuffer);

    // System.out.println(token + " (" + address + ") -> " + controller);

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
      // System.out.println(String.format("Released %d token(s).", release.size()));
    }
  }

  public void run() {
    try (DatagramSocket socket = new DatagramSocket(port)) {
      socket.setSoTimeout(1000);
      // System.out.println("socket: " + socket.getLocalSocketAddress());

      while (!Thread.currentThread().isInterrupted()) {

        try {
          socket.receive(controllerStatePacket);
        } catch (SocketTimeoutException e) {
          releaseTokens(System.nanoTime());
          continue;
        }

        if (controllerStatePacket.getLength() != ControllerState.CAPACITY) {
          continue;
        }

        handlePacket();
      }

    } catch (Exception e) {
      throw new RuntimeException("Exception in run()!", e);
    }
  }

}
