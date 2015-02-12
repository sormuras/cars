package de.stonebone.cars.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


/*
#!/bin/bash

java \
 -Djava.rmi.server.hostname=85.25.48.124 \
 -Dcom.sun.management.jmxremote \
 -Dcom.sun.management.jmxremote.port=1099 \
 -Dcom.sun.management.jmxremote.rmi.port=1099 \
 -Dcom.sun.management.jmxremote.local.only=false \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.ssl=false \
 de.stonebone.cars.util.Inbox
 */
public class Inbox {
  
  public static boolean running = true;

  public static void main(String[] args) throws Exception {

    int port = 4231;
    int maxPlayers = 4;
    int serial = 0;

    try (DatagramSocket socket = new DatagramSocket(port)) {

      Configuration[] cons = new Configuration[maxPlayers];
      for (int id = 0; id < cons.length; id++) {
        cons[id] = new Configuration();
        cons[id].id = id;
      }

      ByteBuffer in = ByteBuffer.allocate(10);
      DatagramPacket client = new DatagramPacket(in.array(), in.capacity());
      ByteBuffer out = ByteBuffer.allocate(1000);
      DatagramPacket server = new DatagramPacket(out.array(), out.capacity());

      System.out.println("The server is ready...");
      while (running) {
        socket.receive(client);
        in.position(client.getLength());
        in.flip();

        out.clear();
        out.putInt(serial);
        for (int id = 0; id < cons.length; id++) {
          out.put((byte) cons[id].steering);
          out.put((byte) cons[id].throttle);
        }
        out.flip();
        server.setLength(out.remaining());
        server.setAddress(client.getAddress());
        server.setPort(client.getPort());

        if (in.remaining() == 1 && in.get(0) == 0x23) {
          socket.send(server);
          continue;
        }

        int se = in.getInt();
        byte id = in.get();

        Configuration c = cons[id];
        if (se <= c.serial) // drop late or duplicated packets
          continue;

        c.steering = in.get();
        c.throttle = in.get();

        assert in.remaining() == 0;

        System.out.println(client.getAddress() + ":" + client.getPort() + "|" + id + "." + se + " " + c.steering + "/" + c.throttle //
            + " -> " + Nio.toString(out));

        socket.send(server);
      }
      
      System.out.println("Run out.");
    }

  }

}
