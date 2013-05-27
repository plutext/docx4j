package com.topologi.diffx.load.text;

/**
 * A utility class for tokenizers.
 * 
 * @author Christophe Lauret
 * @version 11 May 2010
 */
final class TokenizerUtils {

  /** Utility class. */
  private TokenizerUtils() {
  }

  /**
   * Returns the length in characters of the leading white space in the given char sequence.
   *
   * @param s the char sequence to look at.
   * @return the number of whitespace characters at the beginning of the sequence..
   */
  public static int getLeadingWhiteSpace(CharSequence s) {
    int i = 0;
    if (0 == s.length()) return 0;
    char c = s.charAt(0);
    while (c == ' ' || c == '\t' || c == '\n') {
      i++;
      if (i == s.length()) {
        break;
      }
      c = s.charAt(i);
    }
    return i;
  }

  /**
   * Returns the length in characters of the trailing white spaces in the given char sequence.
   *
   * @param s the char sequence to look at.
   * @return the number of whitespace characters at the end of the sequence..
   */
  public static int getTrailingWhiteSpace(CharSequence s) {
    int i = 0;
    if (s.length() == 0) return 0;
    char c = s.charAt(s.length() - 1 - i);
    while (c == ' ' || c == '\t' || c == '\n') {
      i++;
      if (i == s.length()) {
        break;
      }
      c = s.charAt(s.length() - 1 - i);
    }
    return i;
  }

}
