/*
 * Copyright (c) 2006, Wygwam
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * - Neither the name of Wygwam nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without 
 * specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.docx4j.openpackaging.parts;

import java.net.URI;
import java.net.URISyntaxException;

import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * An immutable Open Packaging Convention compliant part name.
 * 
 * [Docx4J comment: Note that in docx4J, part names should be resolved,
 * before being set, so that they are absolute
 * (ie start with '/').  In contrast, this class enforces the
 * OPC specification, which says that a part name can't be 
 * absolute.  For this reason, you'll see the leading '/'
 * being added and removed in various places :(              ]
 * 
 * 
 * @author Julien Chable
 * @version 0.1
 */
public final class PartName implements Comparable<PartName> {

	private static Logger log = LoggerFactory.getLogger(PartName.class);	
	
	
	/**
	 * Part name stored as an URI.
	 */
	private URI partNameURI;

	/*
	 * URI Characters definition (RFC 3986)
	 */

	/**
	 * Reserved characters for sub delimitations.
	 */
	private static String[] RFC3986_PCHAR_SUB_DELIMS = { "!", "$", "&", "'",
			"(", ")", "*", "+", ",", ";", "=" };

	/**
	 * Unreserved character (+ ALPHA & DIGIT).
	 */
	private static String[] RFC3986_PCHAR_UNRESERVED_SUP = { "-", ".", "_", "~" };

	/**
	 * Authorized reserved characters for pChar.
	 */
	private static String[] RFC3986_PCHAR_AUTHORIZED_SUP = { ":", "@" };

	/**
	 * Flag to know if this part name is from a relationship part name.
	 */
	private boolean isRelationship;

	/**
	 * Constructor. Makes a ValidPartName object from a java.net.URI
	 * 
	 * @param uri
	 *            The URI to validate and to transform into ValidPartName.
	 * @param checkConformance
	 *            Flag to specify if the contructor have to validate the OPC
	 *            conformance. Must be always <code>true</code> except for
	 *            special URI like '/' which is needed for internal use by
	 *            OpenXML4J but is not valid.
	 * @throws InvalidFormatException
	 *             Throw if the specified part name is not conform to Open
	 *             Packaging Convention specifications.
	 * @see java.net.URI
	 */
	public PartName(URI uri, boolean checkConformance)
			throws InvalidFormatException {
		if (checkConformance) {
			throwExceptionIfInvalidPartUri(uri);
		} else {
			if (!URIHelper.PACKAGE_ROOT_URI.equals(uri)) {
				throw new Docx4JRuntimeException(
						"OCP conformance must be check for ALL part name except special cases : ['/']");
			}
		}
		this.partNameURI = uri;
		this.isRelationship = isRelationshipPartURI(this.partNameURI);
		//log.debug( getName() + " part name created.");
	}

	/**
	 * Constructor. Makes a ValidPartName object from a String part name, 
	 * provided it validates against OPC conformance.
	 * 
	 * @param partName
	 *            Part name to valid and to create.
	 * @throws InvalidFormatException
	 *             Throw if the specified part name is not conform to Open
	 *             Packaging Convention specifications.
	 */
	public PartName(String partName)
			throws InvalidFormatException {
		this(partName, true);
	}
	/**
	 * Constructor. Makes a ValidPartName object from a String part name.
	 * 
	 * @param partName
	 *            Part name to valid and to create.
	 * @param checkConformance
	 *            Flag to specify if the contructor have to validate the OPC
	 *            conformance. Must be always <code>true</code> except for
	 *            special URI like '/' which is needed for internal use by
	 *            OpenXML4J but is not valid.
	 * @throws InvalidFormatException
	 *             Throw if the specified part name is not conform to Open
	 *             Packaging Convention specifications.
	 */
	public PartName(String partName, boolean checkConformance)
			throws InvalidFormatException {
//		log.debug( "Trying to create part name " + partName);
		URI partURI;
		try {
			partURI = new URI(partName);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"partName argmument is not a valid OPC part name !");
		}

		if (checkConformance) {
			throwExceptionIfInvalidPartUri(partURI);
		} else {
			if (!URIHelper.PACKAGE_ROOT_URI.equals(partURI)) {
				throw new Docx4JRuntimeException(
						"OCP conformance must be check for ALL part name except special cases : ['/']");
			}
		}
		this.partNameURI = partURI;
		this.isRelationship = isRelationshipPartURI(this.partNameURI);
		//log.debug( getName() + " part name created.");
	}

	/**
	 * Check if the specified part name is a relationship part name.
	 * 
	 * @param partUri
	 *            The URI to check.
	 * @return <code>true</code> if this part name respect the relationship
	 *         part naming convention else <code>false</code>.
	 */
	private boolean isRelationshipPartURI(URI partUri) {
		if (partUri == null)
			throw new IllegalArgumentException("partUri");

		return partUri.getPath().matches(
				".*" + URIHelper.RELATIONSHIP_PART_SEGMENT_NAME + ".*"
						+ URIHelper.RELATIONSHIP_PART_EXTENSION_NAME
						+ "$");
	}

	/**
	 * To know if this part name is a relationship part name.
	 * 
	 * @return <code>true</code> if this part name respect the relationship
	 *         part naming convention else <code>false</code>.
	 */
	public boolean isRelationshipPartURI() {
		return this.isRelationship;
	}

	/**
	 * Throws an exception (of any kind) if the specified part name does not
	 * follow the Open Packaging Convention specifications naming rules.
	 * 
	 * @param partUri
	 *            The part name to check.
	 * @throws Exception
	 *             Throws if the part name is invalid.
	 */
	private static void throwExceptionIfInvalidPartUri(URI partUri)
			throws InvalidFormatException {
		if (partUri == null)
			throw new IllegalArgumentException("partUri");
		// Check if the part name URI is empty [M1.1]
		throwExceptionIfEmptyURI(partUri);

		// Check if the part name URI is absolute
		throwExceptionIfAbsoluteUri(partUri);

		// Check if the part name URI doesn't start with a forward slash [M1.4]
		throwExceptionIfPartNameNotStartsWithForwardSlashChar(partUri);

		// Check if the part name URI ends with a forward slash [M1.5]
		throwExceptionIfPartNameEndsWithForwardSlashChar(partUri);

		// Check if the part name does not have empty segments. [M1.3]
		// Check if a segment ends with a dot ('.') character. [M1.9]
		throwExceptionIfPartNameHaveInvalidSegments(partUri);
	}

	/**
	 * Throws an exception if the specified URI is empty. [M1.1]
	 * 
	 * @param partURI
	 *            Part URI to check.
	 * @throws InvalidFormatException
	 *             If the specified URI is empty.
	 */
	private static void throwExceptionIfEmptyURI(URI partURI)
			throws InvalidFormatException {
		if (partURI == null) {
			throw new IllegalArgumentException("partURI");
		}
		
		String uriPath = partURI.getPath();
		if (uriPath.length() == 0
				|| ((uriPath.length() == 1) && (uriPath.charAt(0) == URIHelper.FORWARD_SLASH_CHAR))) {
			throw new InvalidFormatException(
					"A part name shall not be empty [M1.1]: "
							+ partURI.getPath());
			
		}
	}

	/**
	 * Throws an exception if the part name has empty segments. [M1.3]
	 * 
	 * Throws an exception if a segment any characters other than pchar
	 * characters. [M1.6]
	 * 
	 * Throws an exception if a segment contain percent-encoded forward slash
	 * ('/'), or backward slash ('\') characters. [M1.7]
	 * 
	 * Throws an exception if a segment contain percent-encoded unreserved
	 * characters. [M1.8]
	 * 
	 * Throws an exception if the specified part name's segments end with a dot
	 * ('.') character. [M1.9]
	 * 
	 * Throws an exception if a segment doesn't include at least one non-dot
	 * character. [M1.10]
	 * 
	 * @param partUri
	 *            The part name to check.
	 * @throws InvalidFormatException
	 *             if the specified URI contain an empty segments or if one the
	 *             segments contained in the part name, ends with a dot ('.')
	 *             character.
	 */
	private static void throwExceptionIfPartNameHaveInvalidSegments(URI partUri)
			throws InvalidFormatException {
		if (partUri == null || "".equals(partUri.toASCIIString())) {
			throw new IllegalArgumentException("partUri");
		}

		// Split the URI into several part and analyze each
		String[] segments = partUri.toASCIIString().split("/");
		if (segments.length <= 1 || !segments[0].equals("")) {
			log.error( "" );
			throw new InvalidFormatException(
					"A part name shall not have empty segments [M1.3]: "
							+ partUri.getPath());
		}
		for (int i = 1; i < segments.length; ++i) {
			String seg = segments[i];
			if (seg == null || "".equals(seg)) {
				log.error( "" );
				throw new InvalidFormatException(
						"A part name shall not have empty segments [M1.3]: "
								+ partUri.getPath());
			}

			if (seg.endsWith(".")) {
				log.error( "" );
				throw new InvalidFormatException(
						"A segment shall not end with a dot ('.') character [M1.9]: "
								+ partUri.getPath());
			}

			if ("".equals(seg.replaceAll("\\\\.", ""))) {
				// Normally will never been invoked with the previous
				// implementation rule [M1.9]
				log.error( "" );
				throw new InvalidFormatException(
						"A segment shall include at least one non-dot character. [M1.10]: "
								+ partUri.getPath());
			}

			/*
			 * Check for rule M1.6, M1.7, M1.8
			 */
			checkPCharCompliance(seg);
		}
	}

	/**
	 * Throws an exception if a segment any characters other than pchar
	 * characters. [M1.6]
	 * 
	 * Throws an exception if a segment contain percent-encoded forward slash
	 * ('/'), or backward slash ('\') characters. [M1.7]
	 * 
	 * Throws an exception if a segment contain percent-encoded unreserved
	 * characters. [M1.8]
	 * 
	 * @param segment
	 *            The segment to check
	 */
	private static void checkPCharCompliance(String segment)
			throws InvalidFormatException {
		boolean errorFlag;
		for (int i = 0; i < segment.length(); ++i) {
			char c = segment.charAt(i);
			errorFlag = true;

			/* Check rule M1.6 */

			// Check for digit or letter
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
					|| (c >= '0' && c <= '9')) {
				errorFlag = false;
			} else {
				// Check "-", ".", "_", "~"
				for (int j = 0; j < RFC3986_PCHAR_UNRESERVED_SUP.length; ++j) {
					if (c == RFC3986_PCHAR_UNRESERVED_SUP[j].charAt(0)) {
						errorFlag = false;
						break;
					}
				}

				// Check ":", "@"
				for (int j = 0; errorFlag
						&& j < RFC3986_PCHAR_AUTHORIZED_SUP.length; ++j) {
					if (c == RFC3986_PCHAR_AUTHORIZED_SUP[j].charAt(0)) {
						errorFlag = false;
					}
				}

				// Check "!", "$", "&", "'", "(", ")", "*", "+", ",", ";", "="
				for (int j = 0; errorFlag
						&& j < RFC3986_PCHAR_SUB_DELIMS.length; ++j) {
					if (c == RFC3986_PCHAR_SUB_DELIMS[j].charAt(0)) {
						errorFlag = false;
					}
				}
			}

			if (errorFlag && c == '%') {
				// We certainly found an encoded character, check for length
				// now ( '%' HEXDIGIT HEXDIGIT)
				if (((segment.length() - i) < 2)) {
					log.error( "" );
					throw new InvalidFormatException("The segment " + segment
							+ " contain invalid encoded character !");
				}

				// If not percent encoded character error occur then reset the
				// flag -> the character is valid
				errorFlag = false;

				// Decode the encoded character
				char decodedChar = (char) Integer.parseInt(segment.substring(
						i + 1, i + 3), 16);
				i += 2;

				/* Check rule M1.7 */
				if (decodedChar == '/' || decodedChar == '\\') {
					log.error( "" );
					throw new InvalidFormatException(
							"A segment shall not contain percent-encoded forward slash ('/'), or backward slash ('\') characters. [M1.7]");
				}
				/* Check rule M1.8 */

				// Check for unreserved character like define in RFC3986
				if ((decodedChar >= 'A' && decodedChar <= 'Z')
						|| (decodedChar >= 'a' && decodedChar <= 'z')
						|| (decodedChar >= '0' && decodedChar <= '9'))
					errorFlag = true;

				// Check for unreserved character "-", ".", "_", "~"
				for (int j = 0; !errorFlag
						&& j < RFC3986_PCHAR_UNRESERVED_SUP.length; ++j) {
					if (c == RFC3986_PCHAR_UNRESERVED_SUP[j].charAt(0)) {
						errorFlag = true;
						break;
					}
				}
				if (errorFlag) {
					log.error( "" );
					throw new InvalidFormatException(
							"A segment shall not contain percent-encoded unreserved characters. [M1.8]");
					}
			}

			if (errorFlag) 
			{
				log.error( "" );
				throw new InvalidFormatException(
						"A segment shall not hold any characters other than pchar characters. [M1.6]");
			}
		}
	}

	/**
	 * Throws an exception if the specified part name doesn't start with a
	 * forward slash character '/'. [M1.4]
	 * 
	 * @param partUri
	 *            The part name to check.
	 * @throws InvalidFormatException
	 *             If the specified part name doesn't start with a forward slash
	 *             character '/'.
	 */
	private static void throwExceptionIfPartNameNotStartsWithForwardSlashChar(
			URI partUri) throws InvalidFormatException {
		String uriPath = partUri.getPath();
		if (uriPath.length() > 0
				&& uriPath.charAt(0) != URIHelper.FORWARD_SLASH_CHAR)
		{
			log.error( "" );
			throw new InvalidFormatException(
					"A part name shall start with a forward slash ('/') character [M1.4]: "
							+ partUri.getPath());
		}
	}

	/**
	 * Throws an exception if the specified part name ends with a forward slash
	 * character '/'. [M1.5]
	 * 
	 * @param partUri
	 *            The part name to check.
	 * @throws InvalidFormatException
	 *             If the specified part name ends with a forwar slash character
	 *             '/'.
	 */
	private static void throwExceptionIfPartNameEndsWithForwardSlashChar(
			URI partUri) throws InvalidFormatException {
		String uriPath = partUri.getPath();
		if (uriPath.length() > 0
				&& uriPath.charAt(uriPath.length() - 1) == URIHelper.FORWARD_SLASH_CHAR) {
			log.error( "" );
			throw new InvalidFormatException(
					"A part name shall not have a forward slash as the last character [M1.5]: "
							+ partUri.getPath());
		}
	}

	/**
	 * Throws an exception if the specified URI is absolute.
	 * 
	 * @param partUri
	 *            The URI to check.
	 * @throws InvalidFormatException
	 *             Throws if the specified URI is absolute.
	 */
	private static void throwExceptionIfAbsoluteUri(URI partUri)
			throws InvalidFormatException {
		if (partUri.isAbsolute()) {
			log.error( "" );
			throw new InvalidFormatException("Absolute URI forbidden: "
					+ partUri);
		}
	}

	/**
	 * Compare two part name following the rule M1.12 :
	 * 
	 * Part name equivalence is determined by comparing part names as
	 * case-insensitive ASCII strings. Packages shall not contain equivalent
	 * part names and package implementers shall neither create nor recognize
	 * packages with equivalent part names. [M1.12]
	 */
	public int compareTo(PartName otherPartName) {
		if (otherPartName == null)
			return -1;
		return this.partNameURI.toASCIIString().toLowerCase().compareTo(
				otherPartName.partNameURI.toASCIIString().toLowerCase());
	}

	/**
	 * Retrieves the extension of the part name if any. If there is no extension
	 * returns an empty String. Example : '/document/content.xml' => 'xml'
	 * 
	 * @return The extension of the part name.
	 */
	public String getExtension() {
		String fragment = this.partNameURI.getPath();
		if (fragment.length() > 0) {
			int i = fragment.lastIndexOf(".");
			if (i > -1)
				return fragment.substring(i + 1);
		}
		return "";
	}

	/**
	 * Get this part name.
	 * 
	 * @return The name of this part name.
	 */
	public String getName() {
		return this.partNameURI.toASCIIString();
	}

	/**
	 * Part name equivalence is determined by comparing part names as
	 * case-insensitive ASCII strings. Packages shall not contain equivalent
	 * part names and package implementers shall neither create nor recognize
	 * packages with equivalent part names. [M1.12]
	 */
	@Override
	public boolean equals(Object otherPartName) {
		if (otherPartName == null
				|| !(otherPartName instanceof PartName))
			return false;
		return this.partNameURI.toASCIIString().toLowerCase().equals(
				((PartName) otherPartName).partNameURI.toASCIIString()
						.toLowerCase());
	}

	@Override
	public int hashCode() {
		return this.partNameURI.toASCIIString().toLowerCase().hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

	/* Getters and setters */

	/**
	 * Part name property getter.
	 * 
	 * @return This part name URI.
	 */
	public URI getURI() {
		return this.partNameURI;
	}

	public static String generateUniqueName(Base sourcePart, String proposedRelId,
			String directoryPrefix, String after_, String ext) {
		
		// In order to ensure unique part name,
		// idea is to use the relId, which ought to be unique
		
		// Also need partName, since images for different parts are stored in a common dir
		String sourcepartName = sourcePart.getPartName().getName();
		int beginIndex = sourcepartName.lastIndexOf("/")+1;
		int endIndex = sourcepartName.lastIndexOf(".");
		String partPrefix = sourcepartName.substring(beginIndex, endIndex);
		
		
		return directoryPrefix + partPrefix + "_" + after_ + "_" +  proposedRelId + "." + ext;
	}
	
	
	/** Return the path of this PartName
	 * ie up to and including its last '/', 
	 * but excluding the filename segment.  */
//	static public String base(String partName) {
//	//		word/document.xml --->
//	//		word/ 
//			String relationshipsPartName = null;
//	
//			log.info("Splitting " + partName );
//	
//			// Split partName at its last "/"
//			int pos = partName.lastIndexOf("/");
//			
//			return partName.substring(0, pos) + "/";
//			
//		}

	/**
	 *  @see URIHelper.getRelationshipPartName for Wygwam's
	 *  implementation which I only found after writing this
	 */
	static public String getRelationshipsPartName(String partName) {
	//		word/document.xml --->
	//		word/_rels/document.xml.rels 
			String relationshipsPartName = null;
	
//			log.info("Splitting " + partName );
	
			String rightBit = partName; 
			
			// Split partName at its last "/"
			int pos = partName.lastIndexOf("/");
			if (pos>0) {
				String leftBit = partName.substring(0, pos);
				rightBit = partName.substring(pos);
//				log.info("**" + leftBit + "/_rels" + rightBit + ".rels" );
				return leftBit + "/_rels" + rightBit + ".rels" ;
			} else {
				// eg partname: foo.ext (ie in root)
				if (!rightBit.startsWith("/"))
					rightBit="/" + rightBit;
//				log.info("**" + "/_rels" + rightBit + ".rels" );
				return "/_rels" + rightBit + ".rels" ;				
			}
		}
	
	/*
	 * OpenXML4J's perhaps more robust alternative implementation
	 * of above. 
	 * 
	 * Build a part name where the relationship should be stored ((ex
	 * /word/document.xml -> /word/_rels/document.xml.rels)
	 * 
	 * @param partUri
	 *            Source part URI
	 * @return the full path (as URI) of the relation file
	 * @throws InvalidOperationException
	 *             Throws if the specified URI is a relationshp part.
	 * @see  PartName.getRelationshipsPartName which is the
	 * implementation I use in the IO package.             
	
	public static PartName getRelationshipPartName(
			PartName partName) {
		if (partName == null)
			throw new IllegalArgumentException("partName");

		if (URIHelper.PACKAGE_ROOT_URI.getPath() == partName.getURI()
				.getPath())
			return URIHelper.PACKAGE_RELATIONSHIPS_ROOT_PART_NAME;

		if (partName.isRelationshipPartURI())
			throw new InvalidOperationException("Can't be a relationship part");

		String fullPath = partName.getURI().getPath();
		String filename = getFilename(partName.getURI());
		fullPath = fullPath.substring(0, fullPath.length() - filename.length());
		fullPath = combine(fullPath,
				URIHelper.RELATIONSHIP_PART_SEGMENT_NAME);
		fullPath = combine(fullPath, filename);
		fullPath = fullPath
				+ URIHelper.RELATIONSHIP_PART_EXTENSION_NAME;

		PartName retPartName;
		try {
			retPartName = createPartName(fullPath);
		} catch (InvalidFormatException e) {
			// Should never happen in production as all data are fixed but in
			// case of return null:
			return null;
		}
		return retPartName;
	}  */	
}
