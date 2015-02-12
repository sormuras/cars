package de.stonebone.cars.server;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.stonebone.cars.util.Inbox;

@WebServlet(urlPatterns = "/state")
public class CarsServlet extends HttpServlet implements ServletContextListener {

  private static final long serialVersionUID = 1L;

  private int counter = 0;

  private Thread worker;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/event-stream");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.

    PrintWriter writer = response.getWriter();
    writer.write("data: " + counter++ + " " + System.currentTimeMillis() + "\n\n");
    writer.flush();
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    System.out.println(event.toString());
    Inbox.running = false;

    try {
      Thread.sleep(333);
    } catch (InterruptedException e) {
      //
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    System.out.println(event.toString());
    
    String path = event.getServletContext().getRealPath("/");

    worker = new Thread(() -> {
      Inbox.running = true;
      try {
        Inbox.main(new String[0]);
      } catch (Exception e) {
        try {
          Files.write(new File(path, "exception.txt").toPath(), e.toString().getBytes());
        } catch (Exception e1) {
          // ignore
        }
      }
    });
    

    worker.setDaemon(true);
    worker.start();
  }

}
