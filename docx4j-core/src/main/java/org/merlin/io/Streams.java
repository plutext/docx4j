package org.merlin.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Various stream-related utility methods.
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
