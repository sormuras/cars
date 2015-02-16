package de.stonebone.cars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletResponse;

import de.stonebone.cars.ControllerState;

@WebListener
public class CarsWebListener implements Runnable, ServletContextListener, Serializable {

  private static final long serialVersionUID = 1L;

  private final Set<AsyncContext> asyncContexts = new HashSet<>();
  private final StringBuilder builder = new StringBuilder();
  private int counter = 0;
  private final Server server = new Server();

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    server.close();
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    event.getServletContext().setAttribute("cars", this);

    Thread worker = new Thread(server);
    worker.setDaemon(true);
    // worker.start();

    server.setSocketTimeoutListener(this);
  }

  public Set<AsyncContext> getAsyncContexts() {
    return asyncContexts;
  }

  public void run() {
    builder.setLength(0);
    builder.append("data:");
    builder.append(++counter);

    builder.append("/").append(server);
    builder.append("/").append(server.getServerState());
    builder.append("/").append(server.getServerState().getSerial());
    builder.append("/").append(server.getServerState().getControllers()[0].getSerial());

    ControllerState[] cons = server.getServerState().getControllers();
    for (int id = 0; id < cons.length; id++) {
      builder.append("<br>").append(id);
      builder.append("=").append(cons[id].toString());
    }

    builder.append("\n\n");

    String s = builder.toString();

    Iterator<AsyncContext> iterator = getAsyncContexts().iterator();
    while (iterator.hasNext()) {
      AsyncContext context = iterator.next();
      try {
        HttpServletResponse response = (HttpServletResponse) context.getResponse();
        response.setContentType("text/event-stream");

        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        PrintWriter writer = response.getWriter();
        writer.write(s);
        writer.flush();
      } catch (IOException e) {
        iterator.remove();
        context.complete();
      }
    }

  }

}
