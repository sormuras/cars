package de.stonebone.cars.server.servlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;

@WebListener
public class Main implements ServletContextListener, Runnable {

  private final Set<AsyncContext> asyncContexts = Collections.synchronizedSet(new HashSet<>());
  private Thread asyncThread;

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
  }

  @Override
  public void run() {
    Random random = new Random();
    while (true) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        return;
      }
      synchronized (asyncContexts) {
        if (asyncContexts.isEmpty())
          continue;
        Iterator<AsyncContext> iterator = asyncContexts.iterator();
        while (iterator.hasNext()) {
          AsyncContext asyncContext = iterator.next();
          try {
            ServletResponse response = asyncContext.getResponse();
            ServletOutputStream stream = response.getOutputStream();
            stream.print("data:" + random.nextInt(9) + "\n\n");
            stream.flush();
          } catch (Exception e) {
            iterator.remove();
          }
        }
      }
    }

  }

}
