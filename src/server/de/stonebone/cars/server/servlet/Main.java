package de.stonebone.cars.server.servlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

  private final Set<AsyncContext> asyncContexts = Collections.synchronizedSet(new HashSet<>());
  private Thread asyncThread;
  private Server server;

  public void addAsyncContext(AsyncContext asyncContext) {
    asyncContext.setTimeout(0);
    synchronized (asyncContexts) {
      asyncContexts.add(asyncContext);
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    asyncThread.interrupt();
    synchronized (asyncContexts) {
      for (AsyncContext asyncContext : asyncContexts) {
        asyncContext.complete();
      }
    }
    try {
      asyncThread.join(1000);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  public void contextInitialized(ServletContextEvent event) {
    event.getServletContext().setAttribute("main", this);

    asyncThread = new Thread(this, "asyncer");
    asyncThread.setDaemon(true);
    asyncThread.start();

    server = new Server();
  }

  @Override
  public void run() {
    int id = 0;
    StringBuilder builder = new StringBuilder();
    while (true) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        return;
      }
      synchronized (asyncContexts) {
        if (asyncContexts.isEmpty())
          continue;
        String event = toDataString(builder, ++id);
        Iterator<AsyncContext> iterator = asyncContexts.iterator();
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
    }

  }

  private String toDataString(StringBuilder builder, int id) {
    builder.setLength(0);

    builder.append("id: ").append(id).append('\n');

    builder.append("data:");

    builder.append("/").append(server);
    builder.append("/").append(server.getServerState());
    builder.append("/").append(server.getServerState().getSerial());
    builder.append("/").append(server.getServerState().getControllers()[0].getSerial());

    ControllerState[] cons = server.getServerState().getControllers();
    for (int i = 0; i < cons.length; i++) {
      builder.append("<br>").append(i);
      builder.append("=").append(cons[i].toString());
    }

    builder.append("\n\n");

    return builder.toString();
  }

}
