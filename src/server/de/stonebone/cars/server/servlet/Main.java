package de.stonebone.cars.server.servlet;

import java.nio.channels.DatagramChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;

import de.stonebone.cars.ControllerState;
import de.stonebone.cars.server.Server;

@WebListener
public class Main implements ServletContextListener, Runnable {

  private final Set<AsyncContext> browsers = Collections.synchronizedSet(new HashSet<>());
  private Thread thread;
  private Server server;

  public void addAsyncContext(AsyncContext asyncContext) {
    asyncContext.setTimeout(0);
    synchronized (browsers) {
      browsers.add(asyncContext);
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    thread.interrupt();
    server.close();
    synchronized (browsers) {
      for (AsyncContext asyncContext : browsers) {
        asyncContext.complete();
      }
    }
    try {
      thread.join(1000);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  public void contextInitialized(ServletContextEvent event) {
    event.getServletContext().setAttribute("main", this);

    thread = new Thread(this, "asyncer");
    thread.setDaemon(true);
    thread.start();

    server = new Server();
  }

  private String createDataString(StringBuilder builder, int id) {
    builder.setLength(0);

    builder.append("id: ").append(id).append('\n');

    ControllerState[] cons = server.getServerState().getControllers();
    for (int i = 0; i < cons.length; i++) {
      builder.append("data:").append(i);
      builder.append("=").append(cons[i].toString());
      builder.append("<br>").append('\n');
    }

    builder.append("\n\n");

    return builder.toString();
  }

  @Override
  public void run() {
    long nanos = System.nanoTime();
    long next = nanos + TimeUnit.SECONDS.toNanos(1);
    int id = 0;
    StringBuilder builder = new StringBuilder();
    while (true) {
      
      try (DatagramChannel channel = server.open()) {
        Thread.yield();

        nanos = System.nanoTime();

        // receive and handle inbound packets...
        server.handleChannel(channel);

        if (nanos < next)
          continue;

        next = nanos + TimeUnit.SECONDS.toNanos(1);

        // broadcast to connected browsers...
        synchronized (browsers) {
          if (browsers.isEmpty())
            continue;
          String event = createDataString(builder, ++id);
          Iterator<AsyncContext> iterator = browsers.iterator();
          while (iterator.hasNext()) {
            AsyncContext asyncContext = iterator.next();
            try {
              ServletResponse response = asyncContext.getResponse();
              ServletOutputStream stream = response.getOutputStream();
              stream.print(event);
              stream.flush();
            } catch (Exception e) {
              iterator.remove();
            }
          }
        }

      } catch (Exception e) {
        throw new RuntimeException("Exception in run()!", e);
      }

    }

  }

}
