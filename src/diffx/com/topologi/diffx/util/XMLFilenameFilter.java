package com.topologi.diffx.util;

import java.io.FileFilter;
import java.io.File;

/**
 * Filename filter for XML files.
 * 
 * <p>This filter assumes that an file simply as the .xml file extension. 
 *
 * @author  Christophe Lauret
 * @version 4 April 2005
 */
public final class XMLFilenameFilter implements FileFilter {

  /**
   * The XML extension to be used for filtering the files.
   */
  public final String ext = "xml";

  /**
   * Set to <code>true</code> to ignore the case of the extension.
   */
  public final boolean ignoreCase;

  /**
   * Creates a new case-insensitive XML file filter.
   */
  public XMLFilenameFilter() {
    this.ignoreCase = false;
  }

  /**
   * Creates a new XML file filter.
   *
   * @param ignoreCase <code>true</code> to ignore the case of the extension.
   */
  public XMLFilenameFilter(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  /**
   * Tests whether or not the specified abstract pathname should be included in a pathname 
   * list.
   *
   * <p>A file is accepted if its name has a file extension matching the "xml".
   *
   * @param pathname The abstract pathname to be tested;
   *
   * @return <code>true</code> if and only if pathname has an extension matching "xml".
   *
   * @throws NullPointerException If the path name is <code>null</code>.
   */
  public boolean accept(File pathname) throws NullPointerException {
    if (pathname == null) throw new NullPointerException("The specified file is null.");
    String name = pathname.getName();
    int dot = name.lastIndexOf('.');
    if (dot == -1) return false;
    String local = name.substring(dot+1);
    return (this.ignoreCase)? ext.equalsIgnoreCase(local) : ext.equals(local);
  }

}