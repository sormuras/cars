package de.stonebone.cars.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Inbox {

  public static void main(String[] args) throws Exception {

    int port = 4231;

    try (DatagramSocket socket = new DatagramSocket(port)) {

      System.out.println("The server is ready...");

      DatagramPacket packet = new DatagramPacket(new byte[100], 100);
      while (true) {
        socket.receive(packet);
        System.out.println(packet.getAddress() + " " + packet.getPort() + ": " + new String(packet.getData()));
        socket.send(packet);
      }
    }

  }

}
