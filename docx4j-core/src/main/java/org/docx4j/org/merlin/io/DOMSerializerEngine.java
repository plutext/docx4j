package org.docx4j.org.merlin.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * An output engine that serializes a DOM tree using a specified
 * character encoding to the target OutputStream.
 *
 * <p>This requires Apache Xerces 2.0.1 or better.
 *
 * @author Copyright (c) 2002 Merlin Hughes <merlin@merlin.org>
 *
 * org.merlin.io was released by its author under the Apache License, 
 * Version 2.0 (the "License") on 9 April 2008; 
 * you may not use this file except in compliance with the License. 
 *
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */

public class DOMSerializerEngine implements OutputEngine {
  private NodeIterator iterator;
  private String encoding;
  private OutputStreamWriter writer;

  public DOMSerializerEngine (Node root) {
    this (root, "UTF-8");
  }

  public DOMSerializerEngine (Node root, String encoding) {
    this (getIterator (root), encoding);
  }

  private static NodeIterator getIterator (Node node) {
    DocumentTraversal dt = (DocumentTraversal)
      ((node instanceof DocumentTraversal) ? node : node.getOwnerDocument ());
    return dt.createNodeIterator (node, NodeFilter.SHOW_ALL, null, false);
  }  

  public DOMSerializerEngine (NodeIterator iterator, String encoding) {
    this.iterator = iterator;
    this.encoding = encoding;
  }

  public void initialize (OutputStream out) throws IOException {
    if (writer != null) {
      throw new IOException ("Already initialized");
    } else {
      writer = new OutputStreamWriter (out, encoding);
    }
  }

  public void execute () throws IOException {
    if (writer == null) {
      throw new IOException ("Not yet initialized");
    } else {
      Node node = iterator.nextNode ();
      closeElements (node);
      if (node == null) {
        writer.close ();
      } else {
        writeNode (node);
        writer.flush ();
      }
    }
  }

  public void finish () throws IOException {
  }
  
  private boolean elementOpened;
  private LinkedList elements = new LinkedList ();

  private void closeElements (Node node) throws IOException {
    Node prev = (node == null) ? null : node.getPreviousSibling (), next;
    if (((node == null) && !elements.isEmpty ()) ||
        ((prev != null) && (prev.getNodeType () == Node.ELEMENT_NODE))) {
      Iterator i = elements.iterator ();
      do {
        next = (Node) i.next ();
        i.remove ();
        if (elementOpened) {
          writer.write (" />");
          elementOpened = false;
          } else {
            writer.write ("</" + next.getNodeName () + ">");
          }
      } while (i.hasNext () && (next != prev));
    } else if (elementOpened && (node.getNodeType () != Node.ATTRIBUTE_NODE)) {
      writer.write (">");
      elementOpened = false;
    }
  }

  private void writeNode (Node node) throws IOException {
    switch (node.getNodeType ()) {
      case Node.DOCUMENT_NODE:
        String javaEncoding = writer.getEncoding ();
        
        String ianaEncoding = org.docx4j.org.apache.xerces.util.EncodingMap.getJava2IANAMapping (javaEncoding);
        writer.write ("<?xml version=\"1.0\" encoding=\"" + ianaEncoding + "\"?>\n");
        break;
      case Node.DOCUMENT_FRAGMENT_NODE:
        break;
      case Node.DOCUMENT_TYPE_NODE:
        DocumentType dt = (DocumentType) node;
        writer.write ("<!DOCTYPE " + dt.getName ());
        String systemID = dt.getSystemId ();
        if (systemID != null) {
          String publicID = dt.getPublicId ();
          if (publicID != null) {
            writer.write (" PUBLIC '" + publicID + "' '");
          } else {
            writer.write (" SYSTEM '");
          }
          writer.write (systemID + "'");
        }
        String internalSubset = dt.getInternalSubset ();
        if (internalSubset != null)
          writer.write (" [\n" + internalSubset + "]");
        writer.write (">\n");
        break;
      case Node.ELEMENT_NODE:
        writer.write ("<" + node.getNodeName ());
        NamedNodeMap attributes = node.getAttributes ();
        for (int i = 0; i < attributes.getLength (); ++ i)
          writeNode (attributes.item (i));
        elementOpened = true;
        elements.addFirst (node);
        break;
      case Node.ATTRIBUTE_NODE:
        writer.write (" " + node.getNodeName () + "=\"" + escape (node.getNodeValue ()) + "\"");
        break;
      case Node.TEXT_NODE:
        writer.write (escape (node.getNodeValue ()));
        break;
      case Node.CDATA_SECTION_NODE:
        writer.write ("<![CDATA[" + node.getNodeValue () + "]]>");
        break;
      case Node.COMMENT_NODE:
        writer.write ("<!--" + escape (node.getNodeValue ()) + "-->");
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        writer.write ("<?" + node.getNodeName ());
        String value = node.getNodeValue ();
        if ((value != null) && (value.length () > 0))
          writer.write (" " + value);
        writer.write ("?>");
        break;
      case Node.ENTITY_REFERENCE_NODE:
        writer.write ("&" + node.getNodeName () + ";");
        break;
      case Node.ENTITY_NODE:
        throw new IOException ("Cannot serialize entity node");
      case Node.NOTATION_NODE:
        throw new IOException ("Cannot serialize notation node");
    }
  }

  private static String escape (String value) {
    StringBuffer buffer = new StringBuffer ();
    for (int i = 0; i < value.length (); ++ i) {
      char c = value.charAt (i);
      switch (c) {
        case '<':
          buffer.append ("&lt;");
          break;
        case '>':
          buffer.append ("&gt;");
          break;
        case '&':
          buffer.append ("&amp;");
          break;
        case '"':
          buffer.append ("&quot;");
          break;
        default:
          buffer.append (c);
          break;
      }
    }
    return buffer.toString ();
  }
}
