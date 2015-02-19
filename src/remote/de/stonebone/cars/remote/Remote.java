package de.stonebone.cars.remote;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import de.stonebone.cars.ControllerState;

public class Remote {

  private static InetAddress host;

  public static void main(String[] args) throws Exception {
    host = InetAddress.getByName(System.getProperty("host", "stonebone.de"));
    new Remote().run();
  }

  private GLFWErrorCallback errorCallback;

  private GLFWKeyCallback keyCallback = GLFW.GLFWKeyCallback((window, key, scancode, action, mods) -> {
    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
      glfwSetWindowShouldClose(window, GL_TRUE);
  });

  private long window;

  private void init() {
    glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

    if (glfwInit() != GL11.GL_TRUE)
      throw new IllegalStateException("Unable to initialize GLFW");

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

    int WIDTH = 300;
    int HEIGHT = 300;

    window = glfwCreateWindow(WIDTH, HEIGHT, "cars-remote", NULL, NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    glfwSetKeyCallback(window, keyCallback);

    ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - WIDTH) / 2, (GLFWvidmode.height(vidmode) - HEIGHT) / 2);

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);

    glfwShowWindow(window);
  }

  private void loop(DatagramSocket socket) {

    ByteBuffer buffer = ByteBuffer.allocate(123);
    DatagramPacket packet = new DatagramPacket(buffer.array(), 0, 0, host, 4231);
    ControllerState controller = new ControllerState();
    int serial = 0;

    FloatBuffer axis;

    GLContext.createFromCurrent();

    glClearColor(0.23529412f, 0.34509805f, 0.5176471f, 0.0f);

    while (glfwWindowShouldClose(window) == GL_FALSE) {
      glfwPollEvents();

      axis = GLFW.glfwGetJoystickAxes(GLFW_JOYSTICK_1);

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      if (axis != null && axis.capacity() >= 2) {
        float a0 = axis.get(0); // x
        GL11.glColor3f(1f, 0.4f, 0.3f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0.25f);
        GL11.glVertex2f(a0, 0.25f);
        GL11.glVertex2f(a0, -0.25f);
        GL11.glVertex2f(0, -0.25f);
        GL11.glEnd();
        float a1 = -axis.get(1); // -y
        GL11.glColor3f(1f, 0.3f, 0.4f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-0.25f, 0);
        GL11.glVertex2f(-0.25f, a1);
        GL11.glVertex2f(0.25f, a1);
        GL11.glVertex2f(0.25f, 0);
        GL11.glEnd();
      }

      if (axis != null && axis.capacity() >= 4) {
        float a0 = axis.get(2); // x
        GL11.glColor3f(0.4f, 0.14f, 0.13f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0.1f);
        GL11.glVertex2f(a0, 0.1f);
        GL11.glVertex2f(a0, -0.1f);
        GL11.glVertex2f(0, -0.1f);
        GL11.glEnd();
        float a1 = -axis.get(3); // -y
        GL11.glColor3f(.5f, 0.13f, 0.14f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-0.1f, 0);
        GL11.glVertex2f(-0.1f, a1);
        GL11.glVertex2f(0.1f, a1);
        GL11.glVertex2f(0.1f, 0);
        GL11.glEnd();
      }

      ByteBuffer buttons = GLFW.glfwGetJoystickButtons(GLFW_JOYSTICK_1);

      if (buttons != null)
        for (int i = 0; i < buttons.remaining(); i++) {
          float s = 4f / buttons.capacity();
          byte state = buttons.get();
          GL11.glColor3f(state == GLFW.GLFW_PRESS ? .9f : .1f, 0.13f, 0.14f);
          GL11.glBegin(GL11.GL_QUADS);
          GL11.glVertex2f(-1f + i * s + 0, -0.04f);
          GL11.glVertex2f(-1f + i * s + 0, 0.04f);
          GL11.glVertex2f(-1f + i * s + s, 0.04f);
          GL11.glVertex2f(-1f + i * s + s, -0.04f);
          GL11.glEnd();
        }

      glfwSwapBuffers(window);

      if (axis != null && buttons != null) {

        controller.setSerial(++serial);
        int max = Math.min(axis.capacity(), controller.getAxis().length);
        for (int i = 0; i < max; i++)
          controller.getAxis()[i] = (byte) (axis.get(i) * 127);
        max = Math.min(buttons.capacity(), controller.getButton().length);
        for (int i = 0; i < max; i++)
          controller.getButton()[i] = buttons.get(i) == GLFW.GLFW_PRESS;
        buffer.clear();
        controller.toByteBuffer(buffer);
        buffer.flip();
        packet.setLength(buffer.remaining());
        try {
          socket.send(packet);
        } catch (IOException e) {
          // ignore
        }
      }

    }
  }

  public void run() {
    System.out.println("Hello LWJGL " + Sys.getVersion() + "!");

    try (DatagramSocket socket = new DatagramSocket()) {
      init();
      loop(socket);

      glfwDestroyWindow(window);
      keyCallback.release();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      glfwTerminate();
      errorCallback.release();
    }
  }

}
