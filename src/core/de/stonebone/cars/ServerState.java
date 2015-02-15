package de.stonebone.cars;

import java.nio.ByteBuffer;

public class ServerState {

  private final ControllerState[] controllers;
  private long serial;

  public ServerState(int numberOfControllers) {
    if (numberOfControllers < 1)
      throw new IllegalArgumentException("At least one controller expected!");

    this.controllers = new ControllerState[numberOfControllers];

    for (int i = 0; i < controllers.length; i++) {
      controllers[i] = new ControllerState();
    }
  }

  public ServerState fromByteBuffer(ByteBuffer source) {
    serial = source.getLong();
    for (int i = 0; i < controllers.length; i++) {
      controllers[i].fromByteBuffer(source);
    }
    return this;
  }

  public ControllerState[] getControllers() {
    return controllers;
  }

  public long getSerial() {
    return serial;
  }

  public void setSerial(long serial) {
    this.serial = serial;
  }

  public ServerState toByteBuffer(ByteBuffer target) {
    target.putLong(serial);
    for (int i = 0; i < controllers.length; i++) {
      controllers[i].toByteBuffer(target);
    }
    return this;
  }

}
