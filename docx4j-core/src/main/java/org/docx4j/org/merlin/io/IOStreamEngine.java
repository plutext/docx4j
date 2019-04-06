package org.docx4j.org.merlin.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An output engine that copies data from an InputStream through
 * a FilterOutputStream to the target OutputStream.
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
public class IOStreamEngine implements OutputEngine {
  private static final int DEFAULT_BUFFER_SIZE = 8192;
  
  private InputStream in;
  private OutputStreamFactory factory;
  private byte[] buffer;
  private OutputStream out;

  public IOStreamEngine (InputStream in, OutputStreamFactory factory) {
    this (in, factory, DEFAULT_BUFFER_SIZE);
  }

  public IOStreamEngine (InputStream in, OutputStreamFactory factory, int bufferSize) {
    this.in = in;
    this.factory = factory;
    buffer = new byte[bufferSize];
  }

  public void initialize (OutputStream out) throws IOException {
    if (this.out != null) {
      throw new IOException ("Already initialized");
    } else {
      this.out = factory.getOutputStream (out);
    }
  }

  public void execute () throws IOException {
    if (out == null) {
      throw new IOException ("Not yet initialized");
    } else {
      int amount = in.read (buffer);
      if (amount < 0) {
        out.close ();
      } else {
        out.write (buffer, 0, amount);
      }
    }
  }

  public void finish () throws IOException {
    in.close ();
  }

  public static interface OutputStreamFactory {
    public OutputStream getOutputStream (OutputStream out) throws IOException;
  }
}
