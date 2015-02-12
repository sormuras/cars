package de.stonebone.cars.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/state")
public class CarsServlet extends HttpServlet implements ServletContextListener {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
    //content type must be set to text/event-stream
    response.setContentType("text/event-stream");   

    //encoding must be set to UTF-8
    response.setCharacterEncoding("UTF-8");

    PrintWriter writer = response.getWriter();

    for(int i=0; i<10; i++) {

        writer.write("data: "+ System.currentTimeMillis() +"\n\n");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    writer.close();
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    System.out.println(event.toString());
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {    
    System.out.println(event.toString());
  }

}
