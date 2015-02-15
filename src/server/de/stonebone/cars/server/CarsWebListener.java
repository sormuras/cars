package de.stonebone.cars.server;

import java.io.Serializable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class CarsWebListener implements ServletContextListener, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    Server server = (Server) event.getServletContext().getAttribute("server");
    server.close();
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {

    Server server = new Server();
    event.getServletContext().setAttribute("server", server);

    Thread worker = new Thread(server);
    worker.setDaemon(true);
    worker.start();
  }

}
