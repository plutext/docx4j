package org.merlin.io;

import java.io.*;

/**
 * An output engine that copies data from a Reader through
 * a OutputStreamWriter to the target OutputStream.
 *
 * @author Copyright (c) 2002 Merlin Hughes <merlin@merlin.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
public class ReaderWriterEngine implements OutputEngine {
  private static final int DEFAULT_BUFFER_SIZE = 8192;
  
  private Reader reader;
  private String encoding;
  private char[] buffer;
  private Writer writer;

  public ReaderWriterEngine (Reader in, String encoding) {
    this (in, encoding, DEFAULT_BUFFER_SIZE);
  }

  public ReaderWriterEngine (Reader reader, String encoding, int bufferSize) {
    this.reader = reader;
    this.encoding = encoding;
    buffer = new char[bufferSize];
  }

  public void initialize (OutputStream out) throws IOException {
    if (writer != null) {
      throw new IOException ("Already initialized");
    } else {
      writer = new OutputStreamWriter (out, encoding);
    }
  }

  public void execute () throws IOException {
    if (writer == null) {
      throw new IOException ("Not yet initialized");
    } else {
      int amount = reader.read (buffer);
      if (amount < 0) {
        writer.close ();
      } else {
        writer.write (buffer, 0, amount);
      }
    }
  }

  public void finish () throws IOException {
    reader.close ();
  }
}
