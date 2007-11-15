package org.merlin.io;

import java.io.*;

/**
 * An incremental data source that writes data to an OutputStream.
 *
 * @author Copyright (c) 2002 Merlin Hughes <merlin@merlin.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
public interface OutputEngine {
  public void initialize (OutputStream out) throws IOException;
  public void execute () throws IOException;
  public void finish () throws IOException;
}
