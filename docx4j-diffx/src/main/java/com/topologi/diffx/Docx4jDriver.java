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

package com.topologi.diffx;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.docx4j.XmlUtils;
import org.eclipse.compare.EventSequenceComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.topologi.diffx.algorithm.DiffXAlgorithm;
import com.topologi.diffx.algorithm.DiffXFitopsy;
import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.impl.CommentEvent;
import com.topologi.diffx.format.DiffXFormatter;
import com.topologi.diffx.format.SmartXMLFormatter;
import com.topologi.diffx.load.DOMRecorder;
import com.topologi.diffx.sequence.EventSequence;
import com.topologi.diffx.sequence.PrefixMapping;
import com.topologi.diffx.sequence.SequenceSlicer;
import com.topologi.diffx.util.Constants;

/**
 * docx4j uses topologi's diffx project to determine the difference
 * between two bits of WordML.  (an xslt is then used to convert
 * the diffx output to WordML with the changes tracked)
 *
 * If the two things being compared start or end with the same
 * XML, diffx slices that off.
 *
 * After that, you are left with EventSequences representing the
 * two things being compared (an event for the start and end of
 * each element and attributes, and for each word of text).
 *
 * The problem is that performance drops off rapidly.  For example,
 * if each event sequence is:
 *
 * + under say 500 entries, time is negligible
 *
 * + 1800 entries long, calculating the LCS length to fill the matrix
 *   may take 17 seconds (on a 2.4GHZ Core 2 Duo, running from within
 *   Eclipse)
 *
 * + 3000 entries, about 95 seconds (under IKVM)
 *
 * + 3500 entries, about 120 seconds
 *
 * + 5500 entries, about 550 seconds (under IKVM)
 *
 * Ultimately, we should migrate to / develop a library which doesn't have
 * this problem, and supports:
 *
 * - word level diff (diffx does, but Fuego doesn't but could)
 * - 3 way merge
 * - move (though why, can OpenXML represent a move?)
 *
 * An intermediate step might be to add an implementation of the Lindholm
 * heuristically guided greedy matcher to the com.topologi.diffx.algorithm
 * package.  See the Fuego Core XML Diff and Patch tool project
 * (which as at 19 June 2009, was offline). Could be relatively straightforward,
 * since it also uses an event sequence concept.
 *
 * But in the meantime this class attempts to divide up the problem.  The strategy
 * is to look at the children of the nodes passed in, hoping to find
 * an LCS amongst those.  If we have that LCS, then
 * (at least in the default case) we don't need
 * to diff the things in the LCS, just the things between the
 * LCS entries.  I say 'default case' because in that case
 * the LCS entries are each the hashcode of the diffx EventSequences.
 * (But if you
 *  were operating on sdts, you might make them the sdt id.)
 *
 * This approach might work on the children of w:body (paragraphs,
 * for example), or the children of an sdt:content.
 *
 * It could also help if you run it on two w:body, where
 * all the w:p are inside w:sdts, provided you make use of the
 * sdt id's, *and* the sliced event sequences inside the sdt's aren't
 * too long.
 *
 * We use the eclipse.compare package for the coarse grained divide+conquer.
 *
 * TODO If any of the diffx sliced event sequence pairs are each > 2000
 * entries long, this will log a warning, and just return
 * left tree deleted, right tree inserted.  Or try to carve them up somehow?
 *
 * The classes in src/diffx do not import any of org.docx4j proper;
 * keep it this way so that this package can be made into a dll
 * using IKVM, and used in a .net application, without extra
 * dependencies (though we do use commons-lang, for help in
 * creating good hashcodes).
 *
 * @author jason
 *
 */
public class Docx4jDriver {
	
	protected static Logger log = LoggerFactory.getLogger(Docx4jDriver.class);



  /**
   * Send this entire EventSequence to the formatter.
   * Used by Docx4jDriver
   *
   * @param eventSequence
   * @param formatter
   * @throws NullPointerException
   * @throws IOException
   */
  public static void formatEventSequence(EventSequence eventSequence, DiffXFormatter formatter) throws NullPointerException, IOException {
	List<DiffXEvent> sequence = eventSequence.events();
    DiffXEvent x = null;
	for (int i = 0; i < sequence.size(); i++) {
      x = (DiffXEvent) sequence.get(i);
      formatter.format(x);
    }
  }


  public static void addToPrefixMapping(PrefixMapping to, PrefixMapping others) throws NullPointerException {
		String key;
		for (Enumeration<String> e = others.getURIs(); e.hasMoreElements();) {
			key = (String) e.nextElement();
			to.add(key, others.getPrefix(key));
		}
	}

  public static void mainDiff(EventSequence seq1, EventSequence seq2, SmartXMLFormatter formatter , DiffXConfig config)
  	throws DiffXException, IOException {
	    formatter.declarePrefixMapping(seq1.getPrefixMapping());
	    formatter.declarePrefixMapping(seq2.getPrefixMapping());

	    if (config != null) formatter.setConfig(config);
	    SequenceSlicer slicer = new SequenceSlicer(seq1, seq2);
	    slicer.slice();
	    slicer.formatStart(formatter);
	    DiffXAlgorithm df = new DiffXFitopsy(seq1, seq2);
	    df.process(formatter);
	    slicer.formatEnd(formatter);
  }

	public static void diff(Node xml1, Node xml2, Writer out) // swapped,
			throws DiffXException, IOException {

		try {
			DiffXConfig diffxConfig = new DiffXConfig();
			diffxConfig.setIgnoreWhiteSpace(false);
			diffxConfig.setPreserveWhiteSpace(true);

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
				Main.diff(xml1, xml2, out, diffxConfig);
					// The signature which takes Reader objects appears to be broken

				out.close();
				return;
			}

			// Divide and conquer

			DOMRecorder loader = new DOMRecorder();
			loader.setConfig(diffxConfig);

			log.debug("top level LCS - creating EventSequences...");
			List<EventSequence> leftES = new ArrayList<EventSequence>();
			for (int i = 0 ; i < xml1.getChildNodes().getLength(); i++ ) {
				//log( Integer.toString(xml1.getChildNodes().item(i).getNodeType()));

				// A text node at this level is assumed to be pretty printing
				if (xml1.getChildNodes().item(i).getNodeType()!=3) {
					//log("Adding " + xml1.getChildNodes().item(i).getNodeName() );
					Element e = (Element)xml1.getChildNodes().item(i);
					
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
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpc", "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" );
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" );
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpi", "http://schemas.microsoft.com/office/word/2010/wordprocessingInk" );
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wps", "http://schemas.microsoft.com/office/word/2010/wordprocessingShape" );
					
					leftES.add(loader.process( e ));
					//log("" + leftES.get( leftES.size()-1 ).hashCode() );
				}
			}
			EventSequenceComparator leftESC = new EventSequenceComparator(leftES);


			//log("\n\n right");
			List<EventSequence> rightES = new ArrayList<EventSequence>();
			for (int i = 0 ; i < xml2.getChildNodes().getLength(); i++ ) {
				if (xml2.getChildNodes().item(i).getNodeType()!=3) {
					Element e = (Element)xml2.getChildNodes().item(i);
					
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
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpc", "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" );
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" );
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wpi", "http://schemas.microsoft.com/office/word/2010/wordprocessingInk" );
					e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wps", "http://schemas.microsoft.com/office/word/2010/wordprocessingShape" );
					
					rightES.add(loader.process( e ));
					//log("" + rightES.get( rightES.size()-1 ).hashCode() );
				}
			}
			EventSequenceComparator rightESC = new EventSequenceComparator(rightES);

			log.debug("top level LCS - determining top level LCS...");
			RangeDifference[] rd = RangeDifferencer.findDifferences(leftESC, rightESC);

			SmartXMLFormatter formatter = new SmartXMLFormatter(out);
			formatter.setConfig(diffxConfig);

			String rootNodeName = xml1.getNodeName();
			openResult(rootNodeName, out);

			if (rd.length==0) {
				log.debug("top level LCS done; there are no differences!");
				addComment("No differences", formatter);
				// Note that our hashcode acts like a canonicaliser
				// - attribute order doesn't matter.

				// So just feed the leftESC into the formatter and return
				for(EventSequence es : leftES) {
				    formatter.declarePrefixMapping(es.getPrefixMapping());
					formatEventSequence(es,formatter);
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
						//out.append("\n<!-- Adding same -->\n");
						addComment("Adding same", formatter);
					    formatter.declarePrefixMapping(leftESC.getItem(k).getPrefixMapping());
						formatEventSequence(leftESC.getItem(k), formatter);
						//out.append("\n<!-- .. Adding same done -->");
						addComment(".. Adding same done ", formatter);

						// If we wanted to difference sdt's which
						// were treated the as the same (via their id)
						// this is where we'd have to change
						// (in addition to changing EventSequence for
						//  such things so that hashcode returned their
						//  id!)
					}
					leftIdx = rdi.leftStart();
				}

				EventSequence seq1 = new EventSequence();
				// Evil hack - doesn't work
				// seq1.mapPrefix("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");

				for (int k = rdi.leftStart() ; k< rdi.leftEnd() ; k++) {

					if (rdi.kind()==rdi.CHANGE) {
						// This we need to diff
						//leftReport.append( "#" );
						seq1.addSequence(leftESC.getItem(k));

						// Don't forget our existing prefix mappings!
						PrefixMapping existingPM = leftESC.getItem(k).getPrefixMapping();
						addToPrefixMapping(seq1.getPrefixMapping(), existingPM);
					} else {
						// Does this happen?
						// This just goes straight into the output,
					    formatter.declarePrefixMapping(leftESC.getItem(k).getPrefixMapping());
						//out.append("\n<!-- Adding same II -->\n");
						addComment("Adding same II", formatter);
						formatEventSequence(leftESC.getItem(k), formatter);
						//out.append("\n<!-- .. Adding same done -->");
						addComment(".. Adding same done", formatter);
					}
				}


				EventSequence seq2 = new EventSequence();
				// Evil hack - doesn't work
				//seq2.mapPrefix("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
				for (int k = rdi.rightStart() ; k< rdi.rightEnd() ; k++) {
					if (rdi.kind()==rdi.CHANGE) {
						// This is the RHS of the diff
						//rightReport.append( "#" );
						seq2.addSequence(rightESC.getItem(k));

						// Don't forget our existing prefix mappings!
						PrefixMapping existingPM = rightESC.getItem(k).getPrefixMapping();
						addToPrefixMapping(seq2.getPrefixMapping(), existingPM);

					}
				}

				leftIdx = rdi.leftEnd();

				// ok, now perform this diff
				//log("performing diff");
				//out.append("\n<!-- Differencing -->\n");
				addComment("Differencing", formatter);


				if (seq1.size() + seq2.size() < 5000) {
				  mainDiff(seq1, seq2, formatter, diffxConfig);
				} else {
					for (int i1=0; i1 < seq1.size(); i1++) {
						formatter.delete(seq1.getEvent(i1));
					}
					for (int i2=0; i2 < seq2.size(); i2++) {
						formatter.insert(seq2.getEvent(i2));
					}
				}

				//out.append("\n<!-- .. Differencing done -->");
				addComment(".. Differencing done", formatter);

			}
			// Tail, if any, goes straight into output

			//out.append("\n<!-- Adding tail -->\n");
			addComment("Adding tail", formatter);
			if (rd.length>0) {
				for (int k = rd[rd.length-1].leftEnd(); k < leftESC.getRangeCount(); k++ ) {
					//leftReport.append( left.getItem(k) );
					formatEventSequence(leftESC.getItem(k), formatter);
				}
			}
			// write out parent close element
			// .. hope all our formatter output is there $
			closeResult(rootNodeName, out);

		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DiffXException(e);
		}
	  }

	// <w:sdtContent
	//	<!-- Adding same -->
	//    >
	// ie dangerous to use writer directly!!
	// so do this...
	public static void addComment(String message, SmartXMLFormatter formatter ) throws IOException {
		CommentEvent ce = new CommentEvent(message);
		formatter.format(ce);
	}

	public static void openResult(String nodename,  Writer out) throws IOException {
		// In general, we need to avoid writing directly to Writer out...
		// since it can happen before formatter output gets there

		// namespaces not properly declared:
		// 4 options:
		// 1:
		// OpenElementEvent containerOpen = new OpenElementEventNSImpl(xml1.getNamespaceURI(), rootNodeName);
		// formatter.format(containerOpen);
		// // AttributeEvent wNS = new AttributeEventNSImpl("http://www.w3.org/2000/xmlns/" , "w",
		// //		"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		// // formatter.format(wNS);
		// but AttributeEvent is too late in the process to set the mapping.
		// so you can comment that out.
		// But you still have to add w: and the other namespaces in
		// SmartXMLFormatter constructor. So may as well do 2.:
		// 2: stick all known namespaces on our root element above
		// 3: fix SmartXMLFormatter
		// Go with option 2 .. since this is clear
		out.append("<" + nodename
				+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""  // w: namespace
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:w10=\"urn:schemas-microsoft-com:office:word\""
				+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"" // 	workaround for case where RHS only contains w14		    
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:dfx=\"" + Constants.BASE_NS_URI + "\""  // Add these, since SmartXMLFormatter only writes them on the first fragment
				+ " xmlns:del=\"" + Constants.DELETE_NS_URI + "\""
				+ " xmlns:ins=\"" + Constants.BASE_NS_URI + "\""
						+ " >" );
	}
	public static void closeResult(String nodename,  Writer out) throws IOException {
		out.append("</" + nodename + ">" );
	}

	public static void main(String[] args) throws Exception {

		// Result format
		Writer diffxResult = new StringWriter();

		// Run the diff
		try {
			long startTime = System.currentTimeMillis();
			diff( getDocument(new File("1L.xml") ).getDocumentElement(),
					getDocument(new File("1R.xml") ).getDocumentElement(),
					   diffxResult);
				// The signature which takes Reader objects appears to be broken
			diffxResult.close();
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			//System.out.println(diffxResult.toString());
			System.out.println(duration + "ms");
			System.out.println(diffxResult.toString() );
		} catch (Exception exc) {
			exc.printStackTrace();
			diffxResult = null;
		}
	}

	private static Document getDocument(File f) throws Exception {

			DocumentBuilder db = XmlUtils.getNewDocumentBuilder();
			return db.parse(f);
	}
}
