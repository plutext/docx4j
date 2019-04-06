package org.docx4j.org.merlin.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * An output engine that copies data from a Reader through
 * a OutputStreamWriter to the target OutputStream.
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
