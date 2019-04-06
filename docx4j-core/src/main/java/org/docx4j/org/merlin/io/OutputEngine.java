package org.docx4j.org.merlin.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An incremental data source that writes data to an OutputStream.
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
public interface OutputEngine {
  public void initialize (OutputStream out) throws IOException;
  public void execute () throws IOException;
  public void finish () throws IOException;
}
