package de.stonebone.cars.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/event", asyncSupported = true)
public class CarsEventSource extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    CarsWebListener cars = (CarsWebListener) getServletContext().getAttribute("cars");
    cars.getAsyncContexts().add(request.startAsync(request, response));
  }

}
