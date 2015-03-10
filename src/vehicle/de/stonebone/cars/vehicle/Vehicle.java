package de.stonebone.cars.vehicle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import de.stonebone.cars.ServerState;
import de.stonebone.cars.util.Nio;

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

    byte[] queryData = {17};
    DatagramPacket query = new DatagramPacket(queryData, 0, 1, host, port);
    
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    DatagramPacket answer = new DatagramPacket(buffer.array(), buffer.capacity());

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(2000);

      while (true) {
        socket.send(query);

        try {
          socket.receive(answer);
        } catch (SocketTimeoutException e) {
          System.err.println("Timed out. Retry...");
          continue;
        }
        if (host.isLoopbackAddress())
          Thread.sleep(23);
        
        buffer.clear();
        buffer.limit(answer.getLength());
        
        System.out.println(Nio.toString(buffer));


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
