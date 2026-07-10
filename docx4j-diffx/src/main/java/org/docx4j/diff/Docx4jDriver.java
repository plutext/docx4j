/*
 *  Copyright 2009, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package org.docx4j.diff;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.EventSequenceComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.pageseeder.diffx.DiffException;
import org.pageseeder.diffx.algorithm.MatrixXMLAlgorithm;
import org.pageseeder.diffx.api.DiffHandler;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.config.DiffConfig;
import org.pageseeder.diffx.core.DefaultXMLProcessor;
import org.pageseeder.diffx.load.DOMLoader;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.impl.XMLComment;
import org.pageseeder.diffx.xml.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * docx4j uses the Diff-X library (org.pageseeder.diffx, previously a
 * bundled fork of its com.topologi.diffx ancestor) to determine the
 * difference between two bits of WordML.  (an xslt is then used to
 * convert the diffx output to WordML with the changes tracked)
 *
 * Historically, Diff-X's matrix-based LCS algorithms slowed down
 * dramatically as the token sequences grew (minutes for a few thousand
 * tokens), so this class divides up the problem.  The strategy is to
 * look at the children of the nodes passed in, hoping to find an LCS
 * amongst those.  If we have that LCS, then (at least in the default
 * case) we don't need to diff the things in the LCS, just the things
 * between the LCS entries.  I say 'default case' because in that case
 * the LCS entries are each the hashcode of the diffx Sequences.  (But
 * if you were operating on sdts, you might make them the sdt id.)
 *
 * This approach might work on the children of w:body (paragraphs,
 * for example), or the children of an sdt:content.
 *
 * We use the eclipse.compare package for the coarse grained
 * divide+conquer.
 *
 * @author jason
 *
 */
public class Docx4jDriver {

	protected static Logger log = LoggerFactory.getLogger(Docx4jDriver.class);

	/**
	 * The DiffConfig equivalent to the legacy fork's defaults with
	 * setIgnoreWhiteSpace(false) + setPreserveWhiteSpace(true):
	 * namespace-aware, whitespace compared, word granularity
	 * (spaces as separate tokens).
	 */
	public static DiffConfig legacyConfig() {
		return DiffConfig.legacyDefault();
	}

	/**
	 * Diff two nodes, writing the Diff-X markup (legacy topologi
	 * namespaces, dfx:ins / dfx:del wrapped text) to out.
	 *
	 * This replaces the legacy com.topologi.diffx.Main.diff(Node, Node,
	 * Writer, DiffXConfig), which used the (patched) SafeXMLFormatter.
	 */
	public static void diff(Node xml1, Node xml2, Writer out, DiffConfig config)
			throws DiffException, IOException {

		DOMLoader loader = new DOMLoader();
		if (config != null) {
			loader.setConfig(config);
		}
		Sequence seq1 = loader.load(xml1);
		Sequence seq2 = loader.load(xml2);

		LegacyDiffOutput output = new LegacyDiffOutput(out, true);
		output.addNamespaces(seq1.getNamespaces());
		output.addNamespaces(seq2.getNamespaces());

		DefaultXMLProcessor processor = new DefaultXMLProcessor();
		processor.diff(seq1.tokens(), seq2.tokens(), flipped(output));
	}

	/**
	 * Send this entire token sequence to the formatter, as unchanged
	 * content.
	 */
	public static void formatTokens(List<XMLToken> tokens, LegacyDiffOutput formatter) {
		for (XMLToken token : tokens) {
			formatter.handle(Operator.MATCH, token);
		}
	}

	public static void mainDiff(Sequence seq1, Sequence seq2, LegacyDiffOutput formatter) {
		formatter.addNamespaces(seq1.getNamespaces());
		formatter.addNamespaces(seq2.getNamespaces());

		// slices common start/end internally (as the legacy
		// SequenceSlicer + DiffXFitopsy combination did)
		MatrixXMLAlgorithm algorithm = new MatrixXMLAlgorithm();
		algorithm.diff(seq1.tokens(), seq2.tokens(), flipped(formatter));
	}

	/**
	 * The legacy algorithms treated their first sequence as the newer one
	 * (its unique tokens are insertions, and at a replacement point they
	 * were formatted before the older sequence's deletions).  The new API
	 * computes the edit script from -&gt; to, so with the newer sequence
	 * as "from" the operators are the reverse of what we want, but the
	 * ordering is right: flip each operator on its way to the output.
	 */
	private static DiffHandler<XMLToken> flipped(DiffHandler<XMLToken> handler) {
		return new DiffHandler<XMLToken>() {
			@Override
			public void start() {
				handler.start();
			}
			@Override
			public void handle(Operator operator, XMLToken token) {
				handler.handle(operator.flip(), token);
			}
			@Override
			public void end() {
				handler.end();
			}
		};
	}

	public static void diff(Node xml1, Node xml2, Writer out) // swapped,
			throws DiffException, IOException {

		try {
			DiffConfig diffxConfig = legacyConfig();

			log.debug(xml1.getNodeName());
			log.debug(""+ xml1.getChildNodes().getLength());
			log.debug(xml2.getNodeName());
			log.debug(""+ xml2.getChildNodes().getLength());

			// Root nodes must be the same to do divide+conquer.
			// Even then, only do it if there
			// are more than 3 children.  (If there are 3 children
			// and the first and last are the same, then diffx slice
			// would detect that anyway).
			if (!xml1.getNodeName().equals(xml2.getNodeName())
				|| (
					(xml1.getChildNodes().getLength() <= 3)
					&& (xml2.getChildNodes().getLength() <= 3))) {
				// Don't bother with anything tricky
				// (In due course, could try doing it on their
				// children?)

				// .. just normal diffx
				log.debug("Skipping top level LCS");
				diff(xml1, xml2, out, diffxConfig);

				out.close();
				return;
			}

			// Divide and conquer

			DOMLoader loader = new DOMLoader();
			loader.setConfig(diffxConfig);

			log.debug("top level LCS - creating Sequences...");
			List<Sequence> leftES = new ArrayList<Sequence>();
			for (int i = 0 ; i < xml1.getChildNodes().getLength(); i++ ) {

				// A text node at this level is assumed to be pretty printing
				if (xml1.getChildNodes().item(i).getNodeType()!=3) {
					declareCommonNamespaces((Element)xml1.getChildNodes().item(i));
					leftES.add(loader.load( xml1.getChildNodes().item(i) ));
				}
			}
			EventSequenceComparator leftESC = new EventSequenceComparator(leftES);

			List<Sequence> rightES = new ArrayList<Sequence>();
			for (int i = 0 ; i < xml2.getChildNodes().getLength(); i++ ) {
				if (xml2.getChildNodes().item(i).getNodeType()!=3) {
					declareCommonNamespaces((Element)xml2.getChildNodes().item(i));
					rightES.add(loader.load( xml2.getChildNodes().item(i) ));
				}
			}
			EventSequenceComparator rightESC = new EventSequenceComparator(rightES);

			log.debug("top level LCS - determining top level LCS...");
			RangeDifference[] rd = RangeDifferencer.findDifferences(leftESC, rightESC);

			LegacyDiffOutput formatter = new LegacyDiffOutput(out, false);

			String rootNodeName = xml1.getNodeName();
			openResult(rootNodeName, out);

			if (rd.length==0) {
				log.debug("top level LCS done; there are no differences!");
				addComment("No differences", formatter);
				// Note that our hashcode acts like a canonicaliser
				// - attribute order doesn't matter.

				// So just feed the leftESC into the formatter and return
				for(Sequence es : leftES) {
					formatter.addNamespaces(es.getNamespaces());
					formatTokens(es.tokens(), formatter);
				}
				closeResult(rootNodeName, out);
				return;
			}

			// Debug: Raw output
			if (log.isDebugEnabled()) {
				for (int i=0; i<rd.length; i++ ) {
					RangeDifference rdi = rd[i];
					log.debug( rdi.kindString() + " left " + rdi.leftStart() + "," + rdi.leftLength()
							+ " right " + rdi.rightStart() + "," + rdi.rightLength() );
				}
			}

			log.debug("top level LCS done; now performing child actions ...");


			int leftIdx = 0;
			for (int i=0; i<rd.length; i++ ) {

				RangeDifference rdi = rd[i];

				// No change
				if (rdi.leftStart() > leftIdx) {

					for (int k = leftIdx ; k< rdi.leftStart() ; k++) {
						// This just goes straight into the output,
						// since it is the same on the left and the right.
						// Since it is the same on both side, we handle
						// it here (on the left side), and
						// ignore it on the right
						addComment("Adding same", formatter);
						formatter.addNamespaces(leftESC.getItem(k).getNamespaces());
						formatTokens(leftESC.getItem(k).tokens(), formatter);
						addComment(".. Adding same done ", formatter);

						// If we wanted to difference sdt's which
						// were treated the as the same (via their id)
						// this is where we'd have to change
						// (in addition to changing Sequence for
						//  such things so that hashcode returned their
						//  id!)
					}
					leftIdx = rdi.leftStart();
				}

				Sequence seq1 = new Sequence();

				for (int k = rdi.leftStart() ; k< rdi.leftEnd() ; k++) {

					if (rdi.kind()==RangeDifference.CHANGE) {
						log.debug("\n left rdi.CHANGE, handling prefixes");
						// This we need to diff
						// (addSequence also merges the namespaces)
						seq1.addSequence(leftESC.getItem(k));
					} else {
						log.debug("left else, handling prefixes");
						// Does this happen?
						// This just goes straight into the output,
						formatter.addNamespaces(leftESC.getItem(k).getNamespaces());
						addComment("Adding same II", formatter);
						formatTokens(leftESC.getItem(k).tokens(), formatter);
						addComment(".. Adding same done", formatter);
					}
				}


				Sequence seq2 = new Sequence();
				for (int k = rdi.rightStart() ; k< rdi.rightEnd() ; k++) {
					if (rdi.kind()==RangeDifference.CHANGE) {
						log.debug("\n right rdi.CHANGE, handling prefixes");
						// This is the RHS of the diff
						seq2.addSequence(rightESC.getItem(k));
					} else {
						log.debug("right else, doing nothing");
					}
				}

				leftIdx = rdi.leftEnd();

				// ok, now perform this diff
				addComment("Differencing", formatter);

				if (seq1.size() + seq2.size() < 5000) {
				  mainDiff(seq1, seq2, formatter);
				} else {
					formatter.addNamespaces(seq1.getNamespaces());
					for (int i1=0; i1 < seq1.size(); i1++) {
						formatter.handle(Operator.DEL, seq1.getToken(i1));
					}
					formatter.addNamespaces(seq2.getNamespaces()); // probably need this as well?
					for (int i2=0; i2 < seq2.size(); i2++) {
						formatter.handle(Operator.INS, seq2.getToken(i2));
					}
				}

				addComment(".. Differencing done", formatter);

			}
			// Tail, if any, goes straight into output

			addComment("Adding tail", formatter);
			if (rd.length>0) {
				for (int k = rd[rd.length-1].leftEnd(); k < leftESC.getRangeCount(); k++ ) {
					formatTokens(leftESC.getItem(k).tokens(), formatter);
				}
			}
			// write out parent close element
			// .. hope all our formatter output is there $
			closeResult(rootNodeName, out);

		} catch (IndexOutOfBoundsException e) {
			throw new DiffException(e.getMessage(), e);
		}
	  }

	/**
	 * The diff operates on the top level children one by one, so each
	 * needs the common OOXML namespaces declared on it.
	 */
	private static void declareCommonNamespaces(Element e) {

		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpc", "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a", "http://schemas.openxmlformats.org/drawingml/2006/main");
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a14", "http://schemas.microsoft.com/office/drawing/2010/main");
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:m", "http://schemas.openxmlformats.org/officeDocument/2006/math" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:mc", "http://schemas.openxmlformats.org/markup-compatibility/2006" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:o", "urn:schemas-microsoft-com:office:office" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:pic", "http://schemas.openxmlformats.org/drawingml/2006/picture");
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:v", "urn:schemas-microsoft-com:vml" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:w10", "urn:schemas-microsoft-com:office:word" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:w14", "http://schemas.microsoft.com/office/word/2010/wordml" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:w15", "http://schemas.microsoft.com/office/word/2012/wordml" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wne", "http://schemas.microsoft.com/office/word/2006/wordml" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wp14", "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpi", "http://schemas.microsoft.com/office/word/2010/wordprocessingInk" );
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wps", "http://schemas.microsoft.com/office/word/2010/wordprocessingShape" );
	}

	// <w:sdtContent
	//	<!-- Adding same -->
	//    >
	// ie dangerous to use writer directly!!
	// so do this...
	public static void addComment(String message, LegacyDiffOutput formatter ) {
		formatter.handle(Operator.MATCH, new XMLComment(message));
	}

	public static void openResult(String nodename,  Writer out) throws IOException {
		// In general, we need to avoid writing directly to Writer out...
		// since it can happen before formatter output gets there
		out.append("<" + nodename
				+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""  // w: namespace
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:w10=\"urn:schemas-microsoft-com:office:word\""
				+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"" // 	workaround for case where RHS only contains w14
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:dfx=\"" + LegacyDiffOutput.BASE_NS_URI + "\""  // Add these, since the formatter only writes them on the first fragment
				+ " xmlns:del=\"" + LegacyDiffOutput.DELETE_NS_URI + "\""
				+ " xmlns:ins=\"" + LegacyDiffOutput.BASE_NS_URI + "\""
						+ " >" );
	}
	public static void closeResult(String nodename,  Writer out) throws IOException {
		out.append("</" + nodename + ">" );
	}

}
