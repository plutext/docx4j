package org.merlin.io;

import java.io.*;

/**
 * Various stream-related utility methods.
 *
 * @author Copyright (c) 2002 Merlin Hughes <merlin@merlin.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
public class Streams {
  private static final int DEFAULT_BUFFER_SIZE = 8192;
  
  private Streams () {
  }

  public static void io (InputStream in, OutputStream out) throws IOException {
    io (in, out, DEFAULT_BUFFER_SIZE);
  }
  
  public static void io (InputStream in, OutputStream out, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int amount;
    while ((amount = in.read (buffer)) >= 0)
      out.write (buffer, 0, amount);
  }
}
