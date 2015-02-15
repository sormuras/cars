package de.stonebone.cars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.stonebone.cars.ControllerState;

@WebServlet(urlPatterns = "/event", asyncSupported = true)
public class CarsEventSource extends HttpServlet implements Runnable {

  private static final long serialVersionUID = 1L;

  private StringBuilder builder = new StringBuilder();

  private int counter = 0;

  private Set<HttpServletResponse> responses = new HashSet<>();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/event-stream");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.

    responses.add(response);
    
    Server server = (Server) getServletContext().getAttribute("server");
    server.setSocketTimeoutListener(this);
  }

  public void run() {
    Server server = (Server) getServletContext().getAttribute("server");

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

    Iterator<HttpServletResponse> iterator = responses.iterator();
    while (iterator.hasNext()) {
      HttpServletResponse response = iterator.next();
      try {
        PrintWriter writer = response.getWriter();
        writer.write(s);
        writer.flush();
      } catch (IOException e) {
        iterator.remove();
      }
    }

  }

}
