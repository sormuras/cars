package de.stonebone.cars.vehicle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import de.stonebone.cars.ServerState;

public class Vehicle implements Runnable {

  public static void main(String[] args) throws Exception {
    InetAddress host = InetAddress.getByName(System.getProperty("host", "stonebone.de"));
    new Vehicle(host).run();
  }

  private final InetAddress host;

  public Vehicle(InetAddress host) {
    this.host = host;
  }

  @Override
  public void run() {
    int port = 4231;

    ByteBuffer buffer = ByteBuffer.allocate(1000);

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(2000);

      DatagramPacket packet = new DatagramPacket(buffer.array(), 0, 0, host, port);
      while (true) {
        buffer.clear();
        buffer.put((byte) 17);
        buffer.flip();

        packet.setLength(1);

        socket.send(packet);

        buffer.clear();
        packet.setLength(buffer.capacity());
        try {
          socket.receive(packet);
        } catch (SocketTimeoutException e) {
          System.err.println("Timed out. Retry...");
          continue;
        }
        if (host.isLoopbackAddress())
          Thread.sleep(23);
        buffer.position(packet.getLength());
        buffer.flip();

        ServerState state = new ServerState(4);
        state.fromByteBuffer(buffer);

        System.out.println(state);
        Thread.sleep(200);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
