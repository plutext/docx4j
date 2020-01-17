/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.DiffXEvent;

/**
 * A formatter which can relay the method calls to multiple formatters.
 *
 * @author Christophe Lauret
 * @version 11 December 2008
 */
public final class MultiplexFormatter implements DiffXFormatter {

  // class attributes ---------------------------------------------------------------------------

  /**
   * the list of formatters to use.
   */
  private final List<DiffXFormatter> formatters;

  // constructors -------------------------------------------------------------------------------

  /**
   * Creates a new formatter without any underlying formatters.
   */
  public MultiplexFormatter() {
    this.formatters = new ArrayList<DiffXFormatter>();
  }

  /**
   * Creates a new formatter wrapping the specified formatter.
   *
   * @param f The formatter to use.
   */
  public MultiplexFormatter(DiffXFormatter f) {
    this.formatters = new ArrayList<DiffXFormatter>(1);
    this.formatters.add(f);
  }

  // methods ------------------------------------------------------------------------------------

  /**
   * Adds a formatter to multiplex.
   *
   * @param f The Diff-X formatter to add.
   */
  public void add(DiffXFormatter f) {
    this.formatters.add(f);
  }

  @Override
  public void format(DiffXEvent e) throws IOException {
    for (DiffXFormatter f : this.formatters) {
      f.format(e);
    }
  }

  @Override
  public void insert(DiffXEvent e) throws IOException {
    for (DiffXFormatter f : this.formatters) {
      f.insert(e);
    }
  }

  @Override
  public void delete(DiffXEvent e) throws IOException, IllegalStateException {
    for (DiffXFormatter f : this.formatters) {
      f.delete(e);
    }
  }

  @Override
  public void setConfig(DiffXConfig config) {
    for (DiffXFormatter f : this.formatters) {
      f.setConfig(config);
    }
  }
}
