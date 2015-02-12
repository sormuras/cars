package de.stonebone.cars.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

public class Sender {

  public static void main(String[] args) throws Exception {

    InetAddress host = InetAddress.getLoopbackAddress(); // InetAddress.getByName("stonebone.de");
    int port = 4231;

    byte id = Byte.valueOf(args[0]);

    ByteBuffer buffer = ByteBuffer.allocate(123);

    Random random = new Random();

    int serial = 0;

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(2000);

      DatagramPacket packet = new DatagramPacket(buffer.array(), 0, 0, host, port);
      while (true) {
        buffer.clear();
        buffer.putInt(++serial);
        buffer.put(id);
        buffer.put((byte) (random.nextInt(256) - 128)); // steering
        buffer.put((byte) (random.nextInt(256) - 128)); // throttle
        buffer.flip();

        packet.setLength(buffer.remaining());
        System.out.println("Sending packet with " + packet.getLength() + " bytes to " + host + "...");

        long start = System.nanoTime();
        socket.send(packet);
        

        buffer.clear();
        packet.setLength(buffer.capacity());
        socket.receive(packet);
        if (host.isLoopbackAddress())
          Thread.sleep(23);
        long duration = MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);
        buffer.position(packet.getLength());
        buffer.flip();

        // Print the response
        System.out.println(Nio.toString(buffer) + " after " + duration + " ms");
        Thread.sleep(2000);
      }
    }

  }

}
