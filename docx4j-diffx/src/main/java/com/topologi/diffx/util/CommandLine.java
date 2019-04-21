/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.util;

/**
 * A set of utility methods to help with command-line interface.
 * 
 * <p>The methods in this class would typically be used in the <code>main(String[])</code>
 * method of a class.
 *
 * @author  Christophe Lauret
 * @version 17 May 2005
 */
public final class CommandLine {

  /**
   * Prevents creation of instances.
   */
  private CommandLine() {
  }

  /**
   * Returns the value corresponding to the given switch.
   * 
   * <p>Returns <code>null</code> if any of the parameters is <code>null</code>.
   *
   * @param name The name of the command line switch
   * @param args The command line arguments
   * 
   * @return The value of the parameter or <code>null</code>.
   */
  public static String getParameter(String name, String[] args) {
    if (args == null || args.length < 2 || name == null) return null;
    // find the argument
    for (int i = 0; i < args.length; i++) {
      if (name.equals(args[i]) && i+1 < args.length)
        return args[i+1];
    }
    return null;
  }

  /**
   * Return <code>true</code> if the specified switch exists in the arguments.
   *
   * <p>This method will go through every argument to check whether the switch exists
   * or not.
   * 
   * <p>Returns <code>false</code> if any of the parameters is <code>null</code>.
   *
   * @param name The name of the command line switch.
   * @param args The command line arguments.
   * 
   * @return <code>true</code> if the switch if available; <code>false</code> otherwise.
   */
  public static boolean hasSwitch(String name, String[] args) {
    if (args == null || name == null) return false;
    for (String arg : args) {
      if (name.equals(arg)) return true;
    }
    return false;
  }

}
