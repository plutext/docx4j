package org.merlin.io;

import java.io.*;

/**
 * An input stream that reads data from an OutputEngine.
 *
 * @author Copyright (c) 2002 Merlin Hughes <merlin@merlin.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
public class OutputEngineInputStream extends InputStream {
  private static final int DEFAULT_INITIAL_BUFFER_SIZE = 8192;
  
  private OutputEngine engine;
  private byte[] buffer;
  private int index, limit, capacity;
  private boolean closed, eof;
  
  public OutputEngineInputStream (OutputEngine engine) throws IOException {
    this (engine, DEFAULT_INITIAL_BUFFER_SIZE);
  }

  public OutputEngineInputStream (OutputEngine engine, int initialBufferSize) throws IOException {
    this.engine = engine;
    capacity = initialBufferSize;
    buffer = new byte[capacity];
    engine.initialize (new OutputStreamImpl ());
  }

  private byte[] one = new byte[1];
  
  public int read () throws IOException {
    int amount = read (one, 0, 1);
    return (amount < 0) ? -1 : one[0] & 0xff;
  }
  
  public int read (byte data[], int offset, int length) throws IOException {
    if (data == null) {
      throw new NullPointerException ();
    } else if ((offset < 0) || (offset + length > data.length) || (length < 0)) {
      throw new IndexOutOfBoundsException ();
    } else if (closed) {
      throw new IOException ("Stream closed");
    } else {
      while (index >= limit) {
        if (eof)
          return -1;
        engine.execute ();
      }
      if (limit - index < length)
        length = limit - index;
      System.arraycopy (buffer, index, data, offset, length);
      index += length;
      return length;
    }
  }

  public long skip (long amount) throws IOException {
    if (closed) {
      throw new IOException ("Stream closed");
    } else if (amount <= 0) {
      return 0;
    } else {
      while (index >= limit) {
        if (eof)
          return 0;
        engine.execute ();
      }
      if (limit - index < amount)
        amount = limit - index;
      index += (int) amount;
      return amount;
    }
  }
  
  public int available () throws IOException {
    if (closed) {
      throw new IOException ("Stream closed");
    } else {
      return limit - index;
    }
  }
  
  public void close () throws IOException {
    if (!closed) {
      closed = true;
      engine.finish ();
    }
  }

  private void writeImpl (byte[] data, int offset, int length) {
    if (index >= limit)
      index = limit = 0;
    if (limit + length > capacity) {
      capacity = capacity * 2 + length;
      byte[] tmp = new byte[capacity];
      System.arraycopy (buffer, index, tmp, 0, limit - index);
      buffer = tmp;
      limit -= index;
      index = 0;
    }
    System.arraycopy (data, offset, buffer, limit, length);
    limit += length;
  }

  private class OutputStreamImpl extends OutputStream {
    public void write (int datum) throws IOException {
      one[0] = (byte) datum;
      write (one, 0, 1);
    }

    public void write (byte[] data, int offset, int length) throws IOException {
      if (data == null) {
        throw new NullPointerException ();
      } else if ((offset < 0) || (offset + length > data.length) || (length < 0)) {
        throw new IndexOutOfBoundsException ();
      } else if (eof) {
        throw new IOException ("Stream closed");
      } else {
        writeImpl (data, offset, length);
      }
    }

    public void close () {
      eof = true;
    }
  }
}
