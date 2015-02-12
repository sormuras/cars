package de.stonebone.cars.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Buffer utility methods.
 * 
 * @author Bregosch
 * @author Sormuras
 */
public class Nio {

  /**
   * Creates a new, read-only byte buffer array that shares this buffer's array content.
   * 
   * @param buffers
   *          backing buffer array
   * @return the new byte buffer array
   */
  public static ByteBuffer[] asReadOnlyByteBuffers(ByteBuffer[] buffers) {
    ByteBuffer[] targets = new ByteBuffer[buffers.length];
    for (int i = 0; i < targets.length; i++) {
      targets[i] = buffers[i].asReadOnlyBuffer();
    }
    return targets;
  }

  public static ByteBuffer createAndPutBoolean(boolean value) {
    return putBoolean(createByteBuffer(1, true), value);
  }

  public static ByteBuffer createAndPutChar(char value) {
    return createByteBuffer(2, true).putChar(value);
  }

  public static ByteBuffer createAndPutChar7(char value) {
    return putChar7(createByteBuffer(3, true), value);
  }

  public static ByteBuffer createAndPutInt(int value) {
    return createByteBuffer(4, true).putInt(value);
  }

  public static ByteBuffer createAndPutInt7(int value) {
    return putInt7(createByteBuffer(5, true), value);
  }

  public static ByteBuffer createAndPutLong(long value) {
    return createByteBuffer(8, true).putLong(value);
  }

  public static ByteBuffer createAndPutLong7(long value) {
    return putLong7(createByteBuffer(9, true), value);
  }

  public static ByteBuffer createAndPutString7(String text) {
    return putString7(createByteBuffer(text.length() << 2, true), text);
  }

  /**
   * Constructs a native-order ByteBuffer with the specified number of bytes.
   * 
   * @param size
   *          the size in bytes
   * @param direct
   *          whether or not this buffer is a direct buffer
   * @return the new <code>ByteBuffer</code>
   */
  public static ByteBuffer createByteBuffer(int size, boolean direct) {
    return direct ? //
    ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        : ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
  }

  /**
   * Constructs an array of native-order buffers providing space for the specified size.
   * 
   * @param totalSize
   *          the total size
   * @param chunkSize
   *          the size of each buffer
   * @param direct
   *          if the chunks are to allocated as direct buffers or not
   * @return an array holding all chunks
   */
  public static ByteBuffer[] createByteBuffers(int totalSize, int chunkSize, boolean direct) {
    if ((totalSize <= 0) || (chunkSize <= 0)) {
      throw new IllegalArgumentException("sizes must be greater than zero");
    }
    int length = totalSize / chunkSize;
    int modulo = totalSize % chunkSize;
    if (modulo > 0) {
      length++;
    }
    ByteBuffer[] buffers = new ByteBuffer[length];
    for (int i = 0; i < length; i++) {
      buffers[i] = createByteBuffer(chunkSize, direct);
    }
    if (modulo > 0) {
      buffers[length - 1].limit(modulo);
    }
    return buffers;
  }

  /**
   * Fills an arbitrary byte buffer with a given value. Filling is done from the current position to the end of the buffer.
   * 
   * @param buffer
   *          the buffer to fill
   * @param value
   *          the value to fill the buffer with
   */
  public static void fill(ByteBuffer buffer, byte value) {
    while (buffer.hasRemaining()) {
      buffer.put(value);
    }
  }

  /**
   * Fills an arbitrary double buffer with a given value. Filling is done from the current position to the end of the buffer.
   * 
   * @param buffer
   *          the buffer to fill
   * @param value
   *          the value to fill the buffer with
   */
  public static void fill(DoubleBuffer buffer, double value) {
    while (buffer.hasRemaining()) {
      buffer.put(value);
    }
  }

  /**
   * Fills an arbitrary float buffer with a given value. Filling is done from the current position to the end of the buffer.
   * 
   * @param buffer
   *          the buffer to fill
   * @param value
   *          the value to fill the buffer with
   */
  public static void fill(FloatBuffer buffer, float value) {
    while (buffer.hasRemaining()) {
      buffer.put(value);
    }
  }

  /**
   * Fills an arbitrary int buffer with a given value. Filling is done from the current position to the end of the buffer.
   * 
   * @param buffer
   *          the buffer to fill
   * @param value
   *          the value to fill the buffer with
   */
  public static void fill(IntBuffer buffer, int value) {
    while (buffer.hasRemaining()) {
      buffer.put(value);
    }
  }

  /**
   * Fills an arbitrary short buffer with a given value. Filling is done from the current position to the end of the buffer.
   * 
   * @param buffer
   *          the buffer to fill
   * @param value
   *          the value to fill the buffer with
   */
  public static void fill(ShortBuffer buffer, short value) {
    while (buffer.hasRemaining()) {
      buffer.put(value);
    }
  }

  public static boolean getBoolean(ByteBuffer source) {
    return source.get() == 1;
  }

  public static char getChar7(ByteBuffer source) {
    byte b0 = source.get();
    if (b0 >= 0) {
      return (char) b0;
    }
    b0 &= 0x7F;
    byte b1 = source.get();
    if (b1 >= 0) {
      return (char) ((b1 << 7) | b0);
    }
    b1 &= 0x7F;
    byte b2 = source.get();
    assert (b2 & 0xFC) == 0;
    return (char) ((b2 << 14) | (b1 << 7) | b0);
  }

  public static <E extends Enum<E>> E getEnum(Class<E> ec, ByteBuffer source) {
    return ec.getEnumConstants()[getInt7(source)];
  }

  public static int getInt7(ByteBuffer source) {
    int b = source.get(); // b must be an int here!
    if (b >= 0) {
      return b;
    }
    int result = b & 0x7F;
    int shift = 1;
    while (true) {
      b = source.get();
      result |= (b & 0x7F) << (7 * shift);
      if (b >= 0) {
        return result;
      }
      shift++;
    }
  }

  public static long getLong7(ByteBuffer source) {
    long b = source.get(); // b must be a long here!
    if (b >= 0) {
      return b;
    }
    long result = b & 0x7F;
    int shift = 1;
    while (true) {
      b = source.get();
      result |= (b & 0x7F) << (7 * shift);
      if (b >= 0) {
        return result;
      }
      shift++;
    }
  }

  /**
   * Gets the byte offset in an arbitrary buffer based on its position.
   * 
   * @param buffer
   *          the buffer of interest
   * @return the position within the buffer in bytes
   */
  public static int getOffset(Buffer buffer) {
    if (buffer instanceof FloatBuffer || buffer instanceof IntBuffer)
      return buffer.position() << 2;
    else if (buffer instanceof ShortBuffer || buffer instanceof CharBuffer)
      return buffer.position() << 1;
    else if (buffer instanceof DoubleBuffer || buffer instanceof LongBuffer)
      return buffer.position() << 3;
    else
      return buffer.position();
  }

  // TODO (speed, Sormuras, 11.01.2007) Unroll and use local byte array.
  public static String getString7(ByteBuffer source) {
    int length = getInt7(source);
    StringBuilder builder = new StringBuilder(length);
    while (--length >= 0) { // treats length as index now
      builder.append(getChar7(source));
    }
    return builder.toString();
  }

  public static int getUnsignedByte(ByteBuffer source) {
    return source.get() & 0xFF;
  }

  public static int getUnsignedByte(ByteBuffer source, int position) {
    return source.get(position) & 0xFF;
  }

  public static long getUnsignedInt(ByteBuffer source) {
    return 0xFFFFFFFFL & source.getInt();
  }

  public static long getUnsignedInt(ByteBuffer source, int position) {
    return source.getInt(position) & 0xFFFFFFFFL;
  }

  public static int getUnsignedShort(ByteBuffer source) {
    return source.getShort() & 0xFFFF;
  }

  public static int getUnsignedShort(ByteBuffer source, int position) {
    return source.getShort(position) & 0xFFFF;
  }

  public static ByteBuffer putBoolean(ByteBuffer target, boolean value) {
    target.put((byte) (value ? 1 : 0));
    return target;
  }

  public static ByteBuffer putChar7(ByteBuffer target, char value) {
    if (value < 0) {
      throw new IllegalArgumentException("value must be positive: " + value);
    }
    while (value > Byte.MAX_VALUE) {
      target.put((byte) (0x80 | (value & 0x7F)));
      value >>>= 7;
    }
    target.put((byte) value);
    return target;
  }

  public static <E extends Enum<E>> ByteBuffer putEnum(ByteBuffer target, E value) {
    putInt7(target, value.ordinal());
    return target;
  }

  public static ByteBuffer putInt7(ByteBuffer target, int value) {
    if (value < 0) {
      throw new IllegalArgumentException("value must be positive: " + value);
    }
    while (value > Byte.MAX_VALUE) {
      target.put((byte) (0x80 | (value & 0x7F)));
      value >>>= 7;
    }
    target.put((byte) value);
    return target;
  }

  public static ByteBuffer putLong7(ByteBuffer target, long value) {
    if (value < 0) {
      throw new IllegalArgumentException("value must be positive: " + value);
    }
    while (value > Byte.MAX_VALUE) {
      target.put((byte) (0x80 | (value & 0x7F)));
      value >>>= 7;
    }
    target.put((byte) value);
    return target;
  }

  public static ByteBuffer putString7(ByteBuffer target, String text) {
    return putString7(target, text, 0, text.length());
  }

  public static ByteBuffer putString7(ByteBuffer target, String text, int length) {
    return putString7(target, text, 0, length);
  }

  // TODO (speed, Sormuras, 11.01.2007) Unroll and use local byte array.
  public static ByteBuffer putString7(ByteBuffer target, String text, int offset, int length) {
    putInt7(target, length);
    for (int i = offset; i < offset + length && i < text.length(); i++) {
      putChar7(target, text.charAt(i));
    }
    return target;
  }

  public static void putUnsignedByte(ByteBuffer bb, int value) {
    bb.put((byte) (value & 0xFF));
  }

  public static void putUnsignedByte(ByteBuffer bb, int position, int value) {
    bb.put(position, (byte) (value & 0xFF));
  }

  public static void putUnsignedInt(ByteBuffer bb, int position, long value) {
    bb.putInt(position, (int) (value & 0xFFFFFFFFL));
  }

  public static void putUnsignedInt(ByteBuffer bb, long value) {
    bb.putInt((int) (value & 0xFFFFFFFFL));
  }

  public static void putUnsignedShort(ByteBuffer bb, int value) {
    bb.putShort((short) (value & 0xFFFFFFFF));
  }

  public static void putUnsignedShort(ByteBuffer bb, int position, int value) {
    bb.putShort(position, (short) (value & 0xFFFF));
  }

  /**
   * Counts all remaining bytes.
   * 
   * @param buffers
   *          to scan
   * @return amount of remaining bytes
   */
  public static long remaining(ByteBuffer[] buffers) {
    assert buffers != null;
    long remaining = 0;
    for (int i = 0; i < buffers.length; i++) {
      remaining += buffers[i].remaining();
    }
    return remaining;
  }

  /**
   * Skip over and discards {@code n} bytes of data from the byte buffer passed.
   * <p>
   * The <code>skip</code> method may, for a variety of reasons, end up skipping over some smaller number of bytes, possibly {@code 0} may
   * result from any of a number of conditions; reaching current limit before {@code n} bytes have been skipped is only one possibility. The
   * actual number of bytes skipped is returned. If {@code n} is negative, no bytes are skipped.
   * 
   * @param buffer
   *          The buffer to skip {@code n} bytes.
   * @param n
   *          The number of bytes to skip.
   */
  public static int skip(ByteBuffer buffer, int n) {
    if (n <= 0) {
      return 0;
    }
    int newPosition = buffer.position() + n;
    if (newPosition > buffer.limit()) {
      newPosition = buffer.limit();
      n = buffer.remaining();
    }
    buffer.position(newPosition);
    return n;
  }

  /**
   * Same as: <code>toString(bytes, 16, 16)</code>.
   */
  public static String toString(byte[] bytes) {
    return toString(bytes, 16, 16);
  }

  /**
   * Same as: <code>toString(ByteBuffer.wrap(bytes), bytesPerLine)</code>.
   */
  public static String toString(byte[] bytes, int bytesPerLine, int maxLines) {
    return toString(ByteBuffer.wrap(bytes), bytesPerLine, maxLines);
  }

  /**
   * Same as: <code>toString(buffer, 16, 16)</code>.
   */
  public static String toString(ByteBuffer buffer) {
    return toString(buffer, 16, 16);
  }

  public static String toString(ByteBuffer buffer, int bytesPerLine, int maxLines) {
    return toString(new StringBuilder(), buffer, bytesPerLine, maxLines);
  }

  public static String toString(StringBuilder builder, ByteBuffer buffer) {
    return toString(builder, buffer, 16, 16);
  }

  public static String toString(StringBuilder builder, ByteBuffer buffer, int bytesPerLine, int maxLines) {
    final boolean INCLUDE_SEGMENT_NUMBERS = true;
    final boolean INCLUDE_VIEW_HEX = true;
    final boolean INCLUDE_VIEW_ASCII = true;
    final int BLOCK_LENGTH = 4;
    final char BLOCK_SEPARATOR = ' ';
    int i, j, n, k, line;
    builder.append(buffer).append(" {\n");
    line = 0;
    for (n = 0; n < buffer.remaining(); n += bytesPerLine, line++) {
      // builder.append(" ");
      if (line >= maxLines) {
        int omitted = buffer.remaining() - n;
        builder.append("...(");
        builder.append(omitted);
        builder.append(" byte");
        builder.append(omitted != 1 ? "s" : "");
        builder.append(" omitted)\n");
        break;
      }
      if (INCLUDE_SEGMENT_NUMBERS) {
        String segment = Integer.toHexString(n).toUpperCase();
        for (j = 0, k = 4 - segment.length(); j < k; j++) {
          builder.append('0');
        }
        builder.append(segment).append(" | ");
      }
      if (INCLUDE_VIEW_HEX) {
        for (i = n; i < n + bytesPerLine && i < buffer.remaining(); i++) {
          String s = Integer.toHexString(buffer.get(i) & 255).toUpperCase();
          if (s.length() == 1) {
            builder.append('0');
          }
          builder.append(s).append(' ');
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
        }
        while (i < n + bytesPerLine) {
          builder.append("   ");
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
          i++;
        }
        builder.append('|').append(' ');
      }
      if (INCLUDE_VIEW_ASCII) {
        for (i = n; i < n + bytesPerLine && i < buffer.remaining(); i++) {
          int v = buffer.get(i) & 255;
          if (v > 127 || Character.isISOControl((char) v)) {
            builder.append('.');
          } else {
            builder.append((char) v);
          }
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
        }
        while (i < n + bytesPerLine) {
          builder.append(' ');
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
          i++;
        }
      }
      builder.append('\n');
    }
    builder.append("}");
    return builder.toString();
  }

  public static byte[] zip(ByteBuffer source, boolean unzip) {
    /*
     * Zip!
     */
    if (!unzip) {
      byte[] raw = new byte[source.remaining()];
      source.get(raw, 0, raw.length);
      ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length);
      baos.write(raw.length >> 0);
      baos.write(raw.length >> 8);
      baos.write(raw.length >> 16);
      try {
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(raw, 0, raw.length);
        gzos.close();
      } catch (IOException e) {
        throw new RuntimeException("GZIP compression failed.", e);
      } finally {
        // no need to baos.close();
      }
      byte[] zip = baos.toByteArray();
      return zip;
    }
    /*
     * Unzip!
     */
    int length = source.get() + source.get() << 8 + source.get() << 16;
    byte[] raw = new byte[length];
    byte[] zip = new byte[source.remaining()];
    source.get(zip);
    ByteArrayInputStream bais = new ByteArrayInputStream(zip);
    try {
      GZIPInputStream gzis = new GZIPInputStream(bais);
      gzis.read(raw, 0, raw.length);
      gzis.close();
    } catch (IOException e) {
      throw new RuntimeException("GZIP decompression failed.", e);
    }
    return raw;
  }

}
