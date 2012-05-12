package com.topologi.diffx.config;

/**
 * The configuration to use with a DiffX operation.
 * 
 * <p>This class acts as a container for a set of properties that can be applied to the
 * main components of Diffx such as the:
 * <ul>
 *   <li>The {@link com.topologi.diffx.load.XMLRecorder} implementations,</li>
 *   <li>The {@link com.topologi.diffx.algorithm.DiffXAlgorithm} implementations,<li>
 *   <li>and the {@link com.topologi.diffx.format.DiffXFormatter} implementations.<li>
 * </ul>
 * 
 * <p>In order to produce the correct results, the configuration must be applied
 * throughout the three steps of processing.
 * 
 * <p>There is an illegal state in this configuration, if the the diffx is not namespace
 * aware it cannot not report the differences in the prefixes. Therefore it is impossible
 * to set both flags to <code>false</code>.
 * 
 * <p>The <code>set</code> methods for those flags will ensure that this situation does
 * not occur. The general rule is that the flag being set takes precedence.
 * 
 * <p>Note that it simply mimics SAX2 which cannot have the features
 * <code>http://xml.org/sax/features/namespaces</code> and
 * <code>http://xml.org/sax/features/namespace-prefixes</code> both set to 
 * <code>false</code>. 
 * 
 * @see com.topologi.diffx.load.XMLRecorder
 * @see com.topologi.diffx.algorithm.DiffXAlgorithm
 * @see com.topologi.diffx.format.DiffXFormatter
 * 
 * @author Christophe Lauret
 * @version 15 April 2004
 */
public final class DiffXConfig {

// class attributes ---------------------------------------------------------------------------

  /**
   * Indicates whether the differences in white spaces should be ignored.
   */
  private boolean ignoreWhiteSpace = true;  //changed

  /**
   * Indicates whether the white spaces should be preserved.
   */ 
  private boolean preserveWhiteSpace = true;

  /**
   * Indicates whether the namespaces should be handled or ignored.
   */ 
  private boolean isNamespaceAware = true;

  /**
   * Indicates whether difference in prefixes should be reported.
   */
  private boolean reportPrefixDifferences = false;
  /**
   * Whether to tokenize on sentences or entire text blocks
   */
  private boolean tokenizeSentences = false;
  private boolean tokenizeBlocks = false;

// class attributes ---------------------------------------------------------------------------

  /**
   * Creates a new configuration for DiffX
   */
  public DiffXConfig() {
  }

// methods ------------------------------------------------------------------------------------

  /**
   * Sets whether the differences in white spaces should be ignored or not.
   * 
   * @param ignore <code>true</code> to ignore differences in white spaces;
   *               <code>false</code> otherwise.
   */
  public void setIgnoreWhiteSpace(boolean ignore) {
    this.ignoreWhiteSpace = ignore;
  }

  /**
   * Sets whether the white spaces should be preserved or not.
   * 
   * @param preserve <code>true</code> to preserve the white spaces;
   *                 <code>false</code> otherwise.
   */
  public void setPreserveWhiteSpace(boolean preserve) {
    this.preserveWhiteSpace = preserve;
  }

  /**
   * Sets whether the Diff-X should take namespaces into account.
   * 
   * <p>It is more efficient to disable namespace processing when the XML to 
   * compare are not expected to use any namespace.
   * 
   * <p>In order to avoid an illegal state, if this flag is set to <code>false</code>
   * and the differences in prefixes will be automatically reported.
   * 
   * @param aware <code>true</code> to preserve the white spaces;
   *              <code>false</code> otherwise.
   */
  public void setNamespaceAware(boolean aware) {
    this.isNamespaceAware = aware;
    if (!aware)
      this.reportPrefixDifferences = true;
  }

  /**
   * Sets whether the Diff-X should report differences in prefixes.
   *
   * <p>In order to avoid an illegal state, if this flag is set to <code>false</code>
   * and then the processor becomes namespace aware.
   *
   * @param report <code>true</code> to report differences in prefixes;
   *               <code>false</code> to ignore them.
   */
  public void setReportPrefixDifferences(boolean report) {
    this.reportPrefixDifferences = report;
    if (!report)
      this.isNamespaceAware = true;
  }

  public void setTokenizeBlocks(boolean tokenizeBlocks) {
    this.tokenizeBlocks = tokenizeBlocks;
  }

  public void setTokenizeSentences(boolean tokenizeSentences) {
    this.tokenizeSentences = tokenizeSentences;
  }

    /**
   * Indicates whether the differences in white spaces should be ignored or not.
   * 
   * @return <code>true</code> to ignore differences in white spaces;
   *         <code>false</code> otherwise.
   */
  public boolean isIgnoreWhiteSpace() {
    return this.ignoreWhiteSpace;
  }

  /**
   * Indicates whether the white spaces are preserved or not.
   * 
   * @return <code>true</code> to preserve the white spaces;
   *         <code>false</code> otherwise.
   */
  public boolean isPreserveWhiteSpace() {
    return this.preserveWhiteSpace;
  }

  /**
   * Indicates whether the Diff-X takes namespaces into account.
   * 
   * @return <code>true</code> to preserve the white spaces;
   *         <code>false</code> otherwise.
   */
  public boolean isNamespaceAware() {
    return this.isNamespaceAware;
  }

  /**
   * Returns whether the differences in prefixes are reported.
   * 
   * @return <code>true</code> to report differences in prefixes;
   *         <code>false</code> to ignore them.
   */
  public boolean isReportPrefixDifferences() {
    return this.reportPrefixDifferences;
  }

  public boolean isTokenizeSentences() {
    return tokenizeSentences;
  }

  public boolean isTokenizeBlocks() {
    return tokenizeBlocks;
  }
}
