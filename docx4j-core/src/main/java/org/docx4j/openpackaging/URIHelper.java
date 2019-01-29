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

package org.docx4j.openpackaging;

import java.net.URI;
import java.net.URISyntaxException;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Helper for part and pack URI.
 * 
 * @author Julien Chable, CDubet
 * @version 0.3
 */
public final class URIHelper {

	private static Logger log = LoggerFactory.getLogger(URIHelper.class);	
	
	/**
	 * Package root URI.
	 */
	private static URI packageRootUri;

	/**
	 * Extension name of a relationship part.
	 */
	public static final String RELATIONSHIP_PART_EXTENSION_NAME;

	/**
	 * Segment name of a relationship part.
	 */
	public static final String RELATIONSHIP_PART_SEGMENT_NAME;

	/**
	 * Segment name of the package properties folder.
	 */
	public static final String PACKAGE_PROPERTIES_SEGMENT_NAME;

	/**
	 * Core package properties art name.
	 */
	public static final String PACKAGE_CORE_PROPERTIES_NAME;

	/**
	 * Forward slash URI separator.
	 */
	public static final char FORWARD_SLASH_CHAR;

	/**
	 * Forward slash URI separator.
	 */
	public static final String FORWARD_SLASH_STRING;

	/**
	 * Package relationships part URI
	 */
	public static final URI PACKAGE_RELATIONSHIPS_ROOT_URI;

	/**
	 * Package relationships part name.
	 */
	public static final PartName PACKAGE_RELATIONSHIPS_ROOT_PART_NAME;

	/**
	 * Core properties part URI.
	 */
	public static final URI CORE_PROPERTIES_URI;

	/**
	 * Core properties partname.
	 */
	public static final PartName CORE_PROPERTIES_PART_NAME;

	/**
	 * Root package URI.
	 */
	public static final URI PACKAGE_ROOT_URI;

	/**
	 * Root package part name.
	 */
	public static final PartName PACKAGE_ROOT_PART_NAME;

	/* Static initialization */
	static {
		RELATIONSHIP_PART_SEGMENT_NAME = "_rels";
		RELATIONSHIP_PART_EXTENSION_NAME = ".rels";
		FORWARD_SLASH_CHAR = '/';
		FORWARD_SLASH_STRING = "/";
		PACKAGE_PROPERTIES_SEGMENT_NAME = "docProps";
		PACKAGE_CORE_PROPERTIES_NAME = "core.xml";

		// Make URI
		URI uriPACKAGE_ROOT_URI = null;
		URI uriPACKAGE_RELATIONSHIPS_ROOT_URI = null;
		URI uriPACKAGE_PROPERTIES_URI = null;
		try {
			uriPACKAGE_ROOT_URI = new URI("/");
			uriPACKAGE_RELATIONSHIPS_ROOT_URI = new URI(FORWARD_SLASH_CHAR
					+ RELATIONSHIP_PART_SEGMENT_NAME + FORWARD_SLASH_CHAR
					+ RELATIONSHIP_PART_EXTENSION_NAME);
			packageRootUri = new URI("/");
			uriPACKAGE_PROPERTIES_URI = new URI(FORWARD_SLASH_CHAR
					+ PACKAGE_PROPERTIES_SEGMENT_NAME + FORWARD_SLASH_CHAR
					+ PACKAGE_CORE_PROPERTIES_NAME);
		} catch (URISyntaxException e) {
			// Should never happen in production as all data are fixed
		}
		PACKAGE_ROOT_URI = uriPACKAGE_ROOT_URI;
		PACKAGE_RELATIONSHIPS_ROOT_URI = uriPACKAGE_RELATIONSHIPS_ROOT_URI;
		CORE_PROPERTIES_URI = uriPACKAGE_PROPERTIES_URI;

		// Make part name from previous URI
		PartName tmpPACKAGE_ROOT_PART_NAME = null;
		PartName tmpPACKAGE_RELATIONSHIPS_ROOT_PART_NAME = null;
		PartName tmpCORE_PROPERTIES_URI = null;
		try {
			tmpPACKAGE_RELATIONSHIPS_ROOT_PART_NAME = createPartName(PACKAGE_RELATIONSHIPS_ROOT_URI);
			tmpCORE_PROPERTIES_URI = createPartName(CORE_PROPERTIES_URI);
			tmpPACKAGE_ROOT_PART_NAME = new PartName(PACKAGE_ROOT_URI,
					false);
		} catch (InvalidFormatException e) {
			// Should never happen in production as all data are fixed
		}
		PACKAGE_RELATIONSHIPS_ROOT_PART_NAME = tmpPACKAGE_RELATIONSHIPS_ROOT_PART_NAME;
		CORE_PROPERTIES_PART_NAME = tmpCORE_PROPERTIES_URI;
		PACKAGE_ROOT_PART_NAME = tmpPACKAGE_ROOT_PART_NAME;
	}

	/**
	 * Gets the URI for the package root.
	 * 
	 * @return URI of the package root.
	 */
	public static URI getPackageRootUri() {
		return packageRootUri;
	}


	/**
	 * Know if the specified URI is a relationship part name.
	 * 
	 * @param partUri
	 *            URI to check.
	 * @return <i>true</i> if the URI <i>false</i>.
	 */
	private static boolean isRelationshipPartURI(URI partUri) {
		if (partUri == null)
			throw new NullPointerException("partUri");

		return partUri.getPath().matches(
				".*" + RELATIONSHIP_PART_SEGMENT_NAME + ".*"
						+ RELATIONSHIP_PART_EXTENSION_NAME + "$");
	}

	/**
	 * Get file name from the specified URI.
	 */
	public static String getFilename(URI uri) {
		if (uri != null) {
			String path = uri.getPath();
			int len = path.length();
			int num2 = len;
			while (--num2 >= 0) {
				char ch1 = path.charAt(num2);
				if (ch1 == URIHelper.FORWARD_SLASH_CHAR)
					return path.substring(num2 + 1, len);
			}
		}
		return "";
	}

	/**
	 * Get the file name without the trailing extension.
	 */
	public static String getFilenameWithoutExtension(URI uri) {
		String filename = getFilename(uri);
		int dotIndex = filename.lastIndexOf(".");
		if (dotIndex == -1)
			return filename;
		return filename.substring(0, dotIndex);
	}

	/**
	 * Get the directory path from the specified URI.
	 */
	public static URI getPath(URI uri) {
		if (uri != null) {
			String path = uri.getPath();
			int len = path.length();
			int num2 = len;
			while (--num2 >= 0) {
				char ch1 = path.charAt(num2);
				if (ch1 == URIHelper.FORWARD_SLASH_CHAR) {
					try {
						return new URI(path.substring(0, num2));
					} catch (URISyntaxException e) {
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Combine two URI.
	 * 
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static URI combine(URI prefix, URI suffix) {
		URI retUri = null;
		try {
			retUri = new URI(combine(prefix.getPath(), suffix.getPath()));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Prefix and suffix can't be combined !");
		}
		return retUri;
	}

	/**
	 * Combine a string URI with a prefix and a suffix.
	 */
	public static String combine(String prefix, String suffix) {
		if (!prefix.endsWith("" + FORWARD_SLASH_CHAR)
				&& !suffix.startsWith("" + FORWARD_SLASH_CHAR))
			return prefix + FORWARD_SLASH_CHAR + suffix;
		else if ((!prefix.endsWith("" + FORWARD_SLASH_CHAR)
				&& suffix.startsWith("" + FORWARD_SLASH_CHAR) || (prefix
				.endsWith("" + FORWARD_SLASH_CHAR) && !suffix.startsWith(""
				+ FORWARD_SLASH_CHAR))))
			return prefix + suffix;
		else
			return "";
	}

	/**
	 * Fully relativize the target part URI against the source part URI.
	 * 
	 * @param sourceURI
	 *            The source part URI.
	 * @param targetURI
	 *            The target part URI.
	 * @return A fully relativize part name URI ('word/media/image1.gif',
	 *         '/word/document.xml' => 'media/image1.gif') else
	 *         <code>null</code>.
	 */
	public static URI relativizeURI(URI sourceURI, URI targetURI) {
		StringBuilder retVal = new StringBuilder();
		String[] segmentsSource = sourceURI.getPath().split("/", -1);
		String[] segmentsTarget = targetURI.getPath().split("/", -1);

		// If the source URI is empty
		if (segmentsSource.length == 0) {
			throw new IllegalArgumentException(
					"Can't relativize an empty source URI !");
		}

		// If target URI is empty
		if (segmentsTarget.length == 0) {
			throw new IllegalArgumentException(
					"Can't relativize an empty target URI !");
		}
		
		// If the source is the root, then the relativized
		//  form must actually be an absolute URI
		if(sourceURI.toString().equals("/")) {
			return targetURI;
		}


		// Relativize the source URI against the target URI.
		// First up, figure out how many steps along we can go
		// and still have them be the same
		int segmentsTheSame = 0;
		for (int i = 0; i < segmentsSource.length && i < segmentsTarget.length; i++) {
			if (segmentsSource[i].equals(segmentsTarget[i])) {
				// Match so far, good
				segmentsTheSame++;
			} else {
				break;
			}
		}

		// If we didn't have a good match or at least except a first empty element
		if ((segmentsTheSame == 0 || segmentsTheSame == 1) && 
				segmentsSource[0].equals("") && segmentsTarget[0].equals("")) {
			for (int i = 0; i < segmentsSource.length - 2; i++) {
				retVal.append("../");
			}
			for (int i = 0; i < segmentsTarget.length; i++) {
				if (segmentsTarget[i].equals(""))
					continue;
				retVal.append(segmentsTarget[i]);
				if (i != segmentsTarget.length - 1)
					retVal.append("/");
			}

			try {
				return new URI(retVal.toString());
			} catch (Exception e) {
				System.err.println(e);
				return null;
			}
		}

		// Special case for where the two are the same
		if (segmentsTheSame == segmentsSource.length
				&& segmentsTheSame == segmentsTarget.length) {
			retVal.append("");
		} else {
			// Matched for so long, but no more

			// Do we need to go up a directory or two from
			// the source to get here?
			// (If it's all the way up, then don't bother!)
			if (segmentsTheSame == 1) {
				retVal.append("/");
			} else {
				for (int j = segmentsTheSame; j < segmentsSource.length - 1; j++) {
					retVal.append("../");
				}
			}

			// Now go from here on down
			for (int j = segmentsTheSame; j < segmentsTarget.length; j++) {
				if (retVal.length() > 0
						&& retVal.charAt(retVal.length() - 1) != '/') {
					retVal.append("/");
				}
				retVal.append(segmentsTarget[j]);
			}
		}

		try {
			return new URI(retVal.toString());
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}
	

	/**
	 * Resolve a target uri against a source.
	 * 
	 * @param sourcePartUri
	 *            The source URI.
	 * @param targetUri
	 *            The target URI.
	 * @return The resolved URI.
	 */
	public static URI resolvePartUri(URI sourcePartUri, URI targetUri) {
//		log.info("source: " + sourcePartUri);
//		log.info("target: " + targetUri);
		URI uri;
		if (sourcePartUri == null || sourcePartUri.isAbsolute()) {
			throw new IllegalArgumentException("sourcePartUri");
		}
		if (targetUri == null) {
			log.error("targetUri was null");
			throw new IllegalArgumentException("targetUri");			
		} else if (targetUri.isAbsolute()) {			
			log.error("targetUri " + targetUri.toString() + " is absolute!");
			throw new IllegalArgumentException("targetUri");
		}

		uri = sourcePartUri.resolve(targetUri);
//		log.info("RESULT: " + uri);
		return uri;
	}

	/**
	 * Get URI from a string path.
	 */
	public static URI getURIFromPath(String path) {
		URI retUri = null;
		try {
			retUri = new URI(path);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("path");
		}
		return retUri;
	}

	public static URI getSourcePartUriFromRelationshipPartUri(
			URI relationshipPartUri) {
		if (relationshipPartUri == null)
			throw new IllegalArgumentException(
					"The relationshipPart Uri was null !");

		if (!isRelationshipPartURI(relationshipPartUri))
			throw new IllegalArgumentException(
					"L'URI ne doit pas �tre celle d'une partie de type relation.");

		if (relationshipPartUri.compareTo(PACKAGE_RELATIONSHIPS_ROOT_URI) == 0)
			return PACKAGE_ROOT_URI;

		String filename = relationshipPartUri.getPath();
		String filenameWithoutExtension = getFilenameWithoutExtension(relationshipPartUri);
		filename = filename
				.substring(0, ((filename.length() - filenameWithoutExtension
						.length()) - RELATIONSHIP_PART_EXTENSION_NAME.length()));
		filename = filename.substring(0, filename.length()
				- RELATIONSHIP_PART_SEGMENT_NAME.length() - 1);
		filename = combine(filename, filenameWithoutExtension);
		return getURIFromPath(filename);
	}

	/**
	 * Create an OPC compliant part name by throwing an exception if the URI is
	 * not valid.
	 * 
	 * @param partUri
	 *            The part name URI to validate.
	 * @return A valid part name object, else <code>null</code>.
	 * @throws InvalidFormatException
	 *             Throws if the specified URI is not OPC compliant.
	 */
	public static PartName createPartName(URI partUri)
			throws InvalidFormatException {
		if (partUri == null)
			throw new IllegalArgumentException("partName");

		return new PartName(partUri, true);
	}

	/**
	 * Create an OPC compliant part name by throwing an exception if the
	 * specified name is not valid.
	 * 
	 * @param partName
	 *            The part name to validate.
	 * @return The correspondant part name if valid, else <code>null</code>.
	 * @throws InvalidFormatException
	 *             Throws if the specified part name is not OPC compliant.
	 * @see #createPartName(URI)
	 */
	public static PartName createPartName(String partName)
			throws InvalidFormatException {
		URI partNameURI;
		try {
			partNameURI = new URI(partName);
		} catch (URISyntaxException e) {
			throw new InvalidFormatException(e.getMessage());
		}
		return createPartName(partNameURI);
	}

	/**
	 * Validate a part URI by returning a boolean.
	 * ([M1.1],[M1.3],[M1.4],[M1.5],[M1.6])
	 * 
	 * (OPC Specifications 8.1.1 Part names) :
	 * 
	 * Part Name Syntax
	 * 
	 * The part name grammar is defined as follows:
	 * 
	 * <i>part_name = 1*( "/" segment )
	 * 
	 * segment = 1*( pchar )</i>
	 * 
	 * 
	 * (pchar is defined in RFC 3986)
	 * 
	 * @param partUri
	 *            The URI to validate.
	 * @return <b>true</b> if the URI is valid to the OPC Specifications, else
	 *         <b>false</b>
	 * 
	 * @see #createPartName(URI)
	 */
	public static boolean isValidPartName(URI partUri) {
		if (partUri == null)
			throw new IllegalArgumentException("partUri");

		try {
			createPartName(partUri);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Decode a URI by converting all percent encoded character into a String
	 * character.
	 * 
	 * @param uri
	 *            The URI to decode.
	 * @return The specified URI in a String with converted percent encoded
	 *         characters.
	 */
	public static String decodeURI(URI uri) {
		StringBuffer retVal = new StringBuffer();
		String uriStr = uri.toASCIIString();
		char c;
		for (int i = 0; i < uriStr.length(); ++i) {
			c = uriStr.charAt(i);
			if (c == '%') {
				// We certainly found an encoded character, check for length
				// now ( '%' HEXDIGIT HEXDIGIT)
				if (((uriStr.length() - i) < 2)) {
					throw new IllegalArgumentException("The uri " + uriStr
							+ " contain invalid encoded character !");
				}

				// Decode the encoded character
				char decodedChar = (char) Integer.parseInt(uriStr.substring(
						i + 1, i + 3), 16);
				retVal.append(decodedChar);
				i += 2;
				continue;
			}
			retVal.append(c);
		}
		return retVal.toString();
	}

//	/**
//	 * Fully relativize the source part URI against the target part URI.
//	 * 
//	 * @param sourceURI
//	 *            The source part URI.
//	 * @param targetURI
//	 *            The target part URI.
//	 * @return A fully relativize part name URI ('word/media/image1.gif',
//	 *         '/word/document.xml' => 'media/image1.gif') else
//	 *         <code>null</code>.
//	 */
//	public static URI OLDrelativizeURI(URI sourceURI, URI targetURI) {
//		StringBuffer retVal = new StringBuffer();
//		String[] segmentsSource = sourceURI.getPath().split("/");
//		String[] segmentsTarget = targetURI.getPath().split("/");
//
//		// If the source URI is empty
//		if (segmentsSource.length == 0) {
//			return null;
//		}
//
//		// If target URI is empty
//		if (segmentsTarget.length == 0) {
//			if (sourceURI.getPath().startsWith(FORWARD_SLASH_STRING)) {
//				try {
//					return new URI(sourceURI.getPath().substring(1));
//				} catch (Exception e) {
//					return null;
//				}
//			} else
//				return sourceURI;
//		}
//
//		// Relativize the source URI against the target URI.
//		for (short i = 0, j = 0; i < segmentsSource.length
//				&& j < segmentsTarget.length; ++i, ++j) {
//			if (segmentsSource[i].equalsIgnoreCase(segmentsTarget[j])) {
//				if (i < segmentsSource.length - 1) {
//					continue;
//				} else {
//					// We add the last segment whatever it happens
//					retVal.append("/");
//					retVal.append(segmentsTarget[i]);
//					break;
//				}
//			} else {
//				for (; i < segmentsSource.length; ++i) {
//					retVal.append("/");
//					retVal.append(segmentsSource[i]);
//				}
//				break;
//			}
//		}
//		try {
//			PartName retPartName = new PartName(
//					retVal.toString(), true);
//			return new URI(retPartName.getURI().getPath().substring(1));
//		} catch (Exception e) {
//			return null;
//		}
//	}

	
	
}
