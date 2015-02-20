package de.stonebone.cars;

import static java.util.Arrays.fill;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Controller state event send by clients to the server.
 *
 * @author Sor
 */
public class ControllerState {

  public static final int CAPACITY = 9;

  // 4 byte
  private final byte[] axis = new byte[4];

  // 1 byte
  private final boolean[] button = new boolean[8];

  // 4 byte
  private int serial;

  private transient SocketAddress socketAddress;

  private transient long touched;

  public void clear() {
    fill(axis, (byte) 0);
    fill(button, false);
    setSerial(0);
    setSocketAddress(null);
    setTouched(0);
  }

  public ControllerState fromByteBuffer(ByteBuffer source) {
    serial = source.getInt();
    source.get(axis);
    byte x = source.get();
    button[0] = (x & 0b0000_0001) != 0;
    button[1] = (x & 0b0000_0010) != 0;
    button[2] = (x & 0b0000_0100) != 0;
    button[3] = (x & 0b0000_1000) != 0;
    button[4] = (x & 0b0001_0000) != 0;
    button[5] = (x & 0b0010_0000) != 0;
    button[6] = (x & 0b0100_0000) != 0;
    button[7] = (x & 0b1000_0000) != 0;
    return this;
  }

  public byte[] getAxis() {
    return axis;
  }

  public boolean[] getButton() {
    return button;
  }

  public int getSerial() {
    return serial;
  }

  public SocketAddress getSocketAddress() {
    return socketAddress;
  }

  public long getTouched() {
    return touched;
  }

  public void setSerial(int serial) {
    this.serial = serial;
  }

  public void setSocketAddress(SocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  public void setTouched(long touched) {
    this.touched = touched;
  }

  public ControllerState toByteBuffer(ByteBuffer target) {
    target.putInt(serial);
    target.put(axis);
    byte x = button[0] ? 0b0000_0001 : (byte) 0;
    x += button[1] ? 0b0000_0010 : 0;
    x += button[2] ? 0b0000_0100 : 0;
    x += button[3] ? 0b0000_1000 : 0;
    x += button[4] ? 0b0001_0000 : 0;
    x += button[5] ? 0b0010_0000 : 0;
    x += button[6] ? 0b0100_0000 : 0;
    x += button[7] ? 0b1000_0000 : 0;
    target.put(x);
    return this;
  }
  
  public StringBuilder toCSV(StringBuilder builder) {
    builder.append(axis[0]).append(',');
    builder.append(axis[1]).append(',');
    builder.append(axis[2]).append(',');
    builder.append(axis[3]).append(',');
    builder.append(button[0]).append(',');
    builder.append(button[1]).append(',');
    builder.append(button[2]).append(',');
    builder.append(button[3]).append(',');
    builder.append(button[4]).append(',');
    builder.append(button[5]).append(',');
    builder.append(button[6]).append(',');
    builder.append(button[7]);
    return builder;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ControllerState [axis=");
    builder.append(Arrays.toString(axis));
    builder.append(", button=");
    builder.append(Arrays.toString(button));
    builder.append(", serial=");
    builder.append(serial);
    builder.append(", touched=");
    builder.append(touched);
    builder.append("]");
    return builder.toString();
  }

}
