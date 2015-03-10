package de.stonebone.cars.server;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class ServerDaemon implements Daemon {

  private Server server;

  @Override
  public void destroy() {
    server = null;
  }

  @Override
  public void init(DaemonContext context) throws DaemonInitException, Exception {
    server = new Server();
  }

  @Override
  public void start() throws Exception {
    new Thread(server).start();
  }

  @Override
  public void stop() throws Exception {
    server.close();
  }

}
