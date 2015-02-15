package de.stonebone.cars.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.stonebone.cars.ControllerState;

@WebServlet(urlPatterns = "/state")
@WebListener
public class CarsServlet extends HttpServlet implements ServletContextListener {

  private static final long serialVersionUID = 1L;

  private StringBuilder builder = new StringBuilder();

  private int counter = 0;

  private Server server = new Server();

  private Thread worker;

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    System.out.println(event.toString());
    worker.interrupt();
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    System.out.println(event.toString());

    // String path = event.getServletContext().getRealPath("/");
    // Files.write(new File(path, "exception.txt").toPath(), e.toString().getBytes());

    worker = new Thread(server);
    worker.setDaemon(true);
    worker.start();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/event-stream");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.

    builder.setLength(0);
    builder.append("data:");
    builder.append(++counter);

    ControllerState[] cons = server.getServerState().getControllers();
    for (int id = 0; id < cons.length; id++) {
      builder.append("<br>").append(id);
      builder.append("=").append(cons[id].toString());
    }
    builder.append("\n\n");

    PrintWriter writer = response.getWriter();
    writer.write(builder.toString());
    writer.flush();
  }

}
