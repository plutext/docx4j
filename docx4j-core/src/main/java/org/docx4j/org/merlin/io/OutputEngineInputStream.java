package org.docx4j.org.merlin.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An input stream that reads data from an OutputEngine.
 *
 * @author Copyright (c) 2002 Merlin Hughes <merlin@merlin.org>
 *
 * org.merlin.io was released by its author under the Apache License, 
 * Version 2.0 (the "License") on 9 April 2008; 
 * you may not use this file except in compliance with the License. 
 *
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
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
