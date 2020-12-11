/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.config;

/**
 * The configuration to use with a DiffX operation.
 * 
 * <p>This class acts as a container for a set of properties that can be applied to the
 * main components of Diff-X such as the:
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
 * @version 10 May 2010
 */
public final class DiffXConfig {

  /**
   * Indicates whether the namespaces should be handled or ignored.
   */
  private boolean isNamespaceAware = true;

  /**
   * Indicates whether difference in prefixes should be reported.
   */
  private boolean reportPrefixDifferences = false;

  /**
   * Defines the whitespace for this configuration.
   */
  private WhiteSpaceProcessing whitespace = WhiteSpaceProcessing.COMPARE;

  /**
   * Required for backward compatibility only.
   */
  private boolean preserveWhiteSpace = true;

  /**
   * Defines the text granularity for this configuration.
   */
  private TextGranularity granularity = TextGranularity.WORD;

  /**
   * Creates a new configuration for Diff-X.
   */
  public DiffXConfig() {
  }

  /**
   * Creates a new configuration for Diff-X.
   * 
   * @param granularity The granularity of text diffing.
   */
  public DiffXConfig(TextGranularity granularity) {
    if (granularity == null)
      throw new NullPointerException("The granularity cannot be configured to be not be null.");
    this.granularity = granularity;
  }

  /**
   * Creates a new configuration for Diff-X.
   * 
   * @param whitespace  How whitespace should be processed.
   * @param granularity The granularity of text diffing.
   */
  public DiffXConfig(WhiteSpaceProcessing whitespace, TextGranularity granularity) {
    if (granularity == null)
      throw new NullPointerException("The granularity cannot be configured to be not be null.");
    if (whitespace == null)
      throw new NullPointerException("The whitespace processing cannot be configured to be not be null.");
    this.granularity = granularity;
    this.whitespace = whitespace;
  }

  // methods ----------------------------------------------------------------------------------------

  /**
   * Sets the granularity of text diffing for this configuration.
   * 
   * @param granularity the text granularity of text diffing for this configuration.
   */
  public void setGranularity(TextGranularity granularity) {
    if (granularity == null)
      throw new NullPointerException("The granularity cannot be configured to be not be null.");
    this.granularity = granularity;
  }

  /**
   * Sets the white space processing for this configuration.
   * 
   * @param whitespace the white space processing for this configuration.
   */
  public void setWhiteSpaceProcessing(WhiteSpaceProcessing whitespace) {
    if (whitespace == null)
      throw new NullPointerException("The whitespace cannot be configured to be not be null.");
    this.whitespace = whitespace;
  }

  /**
   * Sets whether Diff-X should take namespaces into account.
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
    if (!aware) {
      this.reportPrefixDifferences = true;
    }
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
    if (!report) {
      this.isNamespaceAware = true;
    }
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

  /**
   * Returns the granularity of text diffing for this configuration.
   * 
   * @return the text granularity of text diffing for this configuration.
   */
  public TextGranularity getGranularity() {
    return this.granularity;
  }

  /**
   * Returns the granularity of text diffing for this configuration.
   * 
   * @return the text granularity of text diffing for this configuration.
   */
  public WhiteSpaceProcessing getWhiteSpaceProcessing() {
    return this.whitespace;
  }

  /**
   * Indicates whether the differences in white spaces should be ignored or not.
   * 
   * @return <code>true</code> to ignore differences in white spaces;
   *         <code>false</code> otherwise.
   */
  public boolean isIgnoreWhiteSpace() {
    return this.whitespace != WhiteSpaceProcessing.COMPARE;
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

  // deprecated methods ---------------------------------------------------------------------------

  /**
   * Sets whether the differences in white spaces should be ignored or not.
   * 
   * @deprecated use <code>setWhiteSpaceProcessing</code> instead
   * 
   * @param ignore <code>true</code> to ignore differences in white spaces;
   *               <code>false</code> otherwise.
   */
  @Deprecated
  public void setIgnoreWhiteSpace(boolean ignore) {
    // COMPARE  <-> ignore = false, preserve = true/false
    // PRESERVE <-> ignore = true, preserve = true
    // IGNORE   <-> ignore = true, preserve = false
    if (!ignore) {
      this.whitespace = WhiteSpaceProcessing.COMPARE;
    } else {
      this.whitespace = this.preserveWhiteSpace? WhiteSpaceProcessing.PRESERVE : WhiteSpaceProcessing.IGNORE;
    }
  }

  /**
   * Sets whether the white spaces should be preserved or not.
   * 
   * @deprecated use <code>setWhiteSpaceProcessing</code> instead
   * 
   * @param preserve <code>true</code> to preserve the white spaces;
   *                 <code>false</code> otherwise.
   */
  @Deprecated
  public void setPreserveWhiteSpace(boolean preserve) {
    // COMPARE  <-> ignore = false, preserve = true/false
    // PRESERVE <-> ignore = true, preserve = true
    // IGNORE   <-> ignore = true, preserve = false
    this.preserveWhiteSpace = preserve;
    if (this.whitespace != WhiteSpaceProcessing.COMPARE) {
      this.whitespace = preserve? WhiteSpaceProcessing.PRESERVE : WhiteSpaceProcessing.IGNORE;
    }
  }

}
