package com.topologi.diffx.format;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.impl.CharEvent;
import com.topologi.diffx.event.impl.CharactersEventBase;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.impl.IgnorableSpaceEvent;
import com.topologi.diffx.event.impl.LineEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.impl.SpaceEvent;
import com.topologi.diffx.event.impl.WordEvent;

/**
 * A simple formatter to write the short string version of the events.
 * 
 * @author Christophe Lauret
 * @version 18 March 2005
 */
public final class ShortStringFormatter implements DiffXFormatter {

//  class attributes ---------------------------------------------------------------------------

  /**
   * The output goes here.
   */
  private final Writer out;

//  constructors -------------------------------------------------------------------------------

  /**
   * Creates a new formatter on the standard output.
   * 
   * @see System#out
   * 
   * @throws IOException should an I/O exception occurs.
   */
  public ShortStringFormatter() throws IOException {
    this(new PrintWriter(System.out));
  }

  /**
   * Creates a new formatter using the specified writer.
   * 
   * @param w The writer to use.
   * 
   * @throws IOException should an I/O exception occurs.
   */
  public ShortStringFormatter(Writer w) throws IOException {
    this.out = w;
  }

// methods ------------------------------------------------------------------------------

  /**
   * Writes the event as a short string.
   * 
   * @see DiffXFormatter#format(DiffXEvent)
   */
  public void format(DiffXEvent e) throws IOException, IllegalStateException {
    this.out.write(toShortString(e));
  }

  /**
   * Writes the event as a short string preceded by '+'.
   * 
   * @see DiffXFormatter#insert(DiffXEvent)
   */
  public void insert(DiffXEvent e) throws IOException, IllegalStateException {
    this.out.write("+");
    this.out.write(toShortString(e));
  }

  /**
   * Writes the event as a short string preceded by '+'.
   * 
   * @see DiffXFormatter#delete(DiffXEvent)
   */
  public void delete(DiffXEvent e) throws IOException, IllegalStateException {
    this.out.write("-");
    this.out.write(toShortString(e));
  }

  /**
   * Ignored.
   * 
   * @see com.topologi.diffx.format.DiffXFormatter#setConfig(com.topologi.diffx.DiffXConfig)
   */
  public void setConfig(DiffXConfig config) {
  }

// private helpers ----------------------------------------------------------------------

  /**
   * Returns the short string for the given event.
   * 
   * @param e The event.
   * 
   * @return The short string for the given event.
   */
  public static String toShortString(DiffXEvent e) {
    // an element to open
    if (e instanceof OpenElementEvent) {
      return '<'+((OpenElementEvent)e).getName()+'>';
    // an element to close
    } else if (e instanceof CloseElementEvent) {
      return "</"+((CloseElementEvent)e).getName()+'>';
    // an attribute
    } else if (e instanceof AttributeEvent) {
      return "@"+((AttributeEvent)e).getName();
    // a word
    } else if (e instanceof WordEvent) {
      return '"'+((CharactersEventBase)e).getCharacters()+'"';
    // a white space event
    } else if (e instanceof SpaceEvent) {
      return "_s_";
    // a single character
    } else if (e instanceof CharEvent) {
      return '\''+((CharactersEventBase)e).getCharacters()+'\'';
    // an ignorable space event
    } else if (e instanceof IgnorableSpaceEvent) {
      return "_i_";
    // a single line
    } else if (e instanceof LineEvent) {
      return "L#"+((LineEvent)e).getLineNumber();
    }
    return "???";
  }

}
