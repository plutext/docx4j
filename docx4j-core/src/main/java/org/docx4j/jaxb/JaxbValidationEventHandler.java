/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.jaxb;

import java.io.IOException;
import java.lang.reflect.Field;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEventLocator;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.apache.xalan.extensions.ExpressionContext;
import org.docx4j.utils.ResourceUtils;


public class JaxbValidationEventHandler implements ValidationEventHandler {
    
	private static Logger log = LoggerFactory.getLogger(JaxbValidationEventHandler.class);		
	
	private boolean shouldContinue = false; // fail by default, but this is often changed!
	/**
	 * Note that docx4j manipulates this setting in various places. 
	 * @param val
	 */
	public void setContinue(boolean val) {
		shouldContinue = val;
	}
	
	public final static String UNEXPECTED_MC_ALTERNATE_CONTENT = "unexpected element (uri:\"http://schemas.openxmlformats.org/markup-compatibility/2006\", local:\"AlternateContent\")";
	
	static Templates mcPreprocessorXslt;	
	static Templates pptxPreprocessorXslt;	
	static Templates xlsxPreprocessorXslt;	

	
	/**
	 * @param pkg
	 * @return
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 * @since 11.5.9
	 */
	public static Templates getPreprocessor(OpcPackage pkg) throws IOException, TransformerConfigurationException {
		
		if (pkg instanceof WordprocessingMLPackage) {
			return getMcPreprocessor();
		}
		
		if (pkg instanceof PresentationMLPackage) {
				
			if (pptxPreprocessorXslt==null) {
				
				Source xsltSource  = new StreamSource(
						ResourceUtils.getResourceViaProperty("pptx4j.jaxb.JaxbValidationEventHandler.pptx", 
								"org/pptx4j/jaxb/pptx-preprocessor.xslt")
						);
				pptxPreprocessorXslt = XmlUtils.getTransformerTemplate(xsltSource);
			}			
			return pptxPreprocessorXslt;
		}
		
		if (pkg instanceof SpreadsheetMLPackage) {
			
			if (xlsxPreprocessorXslt==null) {
				
				Source xsltSource  = new StreamSource(
						ResourceUtils.getResourceViaProperty("xlsx4j.jaxb.JaxbValidationEventHandler.xlsx", 
								"org/xlsx4j/jaxb/xlsx-preprocessor.xslt")
						);
				xlsxPreprocessorXslt = XmlUtils.getTransformerTemplate(xsltSource);
			}
			return xlsxPreprocessorXslt;
		}
		log.warn("Unexpected package " + pkg.getClass().getName());
		return null;
	}
	
	public static Templates getMcPreprocessor() throws IOException, TransformerConfigurationException {
		
		if (mcPreprocessorXslt==null) {
			
			Source xsltSource  = new StreamSource(
					ResourceUtils.getResourceViaProperty("docx4j.jaxb.JaxbValidationEventHandler", 
							"org/docx4j/jaxb/mc-preprocessor.xslt")
					);
			mcPreprocessorXslt = XmlUtils.getTransformerTemplate(xsltSource);
		}
		
		return mcPreprocessorXslt;
		
	}
	
    public boolean handleEvent(ValidationEvent ve) {  
    	
    	if (log.isDebugEnabled()) {
			log.debug("handleEvent called", new Throwable());
		}
    	    	
	    if (ve.getSeverity()==ValidationEvent.FATAL_ERROR 
	    		  || ve.getSeverity()==ValidationEvent.ERROR){
	    	  
	          ValidationEventLocator  locator = ve.getLocator();
	          
	          //print message from validation event
	          if (log.isDebugEnabled() || ve.getMessage().length() < 120 ) {
	        	  log.warn( printSeverity(ve) + ": " + ve.getMessage() );
	          } else {
	        	  /* These messages are long, for example:
	        	   * 
	        	   *    unexpected element (uri:"http://schemas.openxmlformats.org/wordprocessingml/2006/main", local:"style"). 
	        	   *    Expected elements are <{http://schemas.openxmlformats.org/wordprocessingml/2006/main}annotationRef>,
	        	   *    ....
	        	   *  
	        	   * so truncate it.
	        	   */
	        	  log.warn( printSeverity(ve) + ": " + ve.getMessage().substring(0, 120));        	  
	          }
	          
	          if (ve.getLinkedException()!=null && log.isDebugEnabled() ) {
	              ve.getLinkedException().printStackTrace();        	   
	          }
	          
	          // MOXy doesn't throw an exception unless we return false
//	          if (Context.jaxbImplementation==JAXBImplementation.ECLIPSELINK_MOXy 
//	        		  && ve.getMessage().contains(UNEXPECTED_MC_ALTERNATE_CONTENT)) {
//	        	  shouldContinue = false;
//	          }
	          
	          //output line and column number
	          if (locator.getColumnNumber()>-1) {
		          log.warn("Column is " + 
		                locator.getColumnNumber() + 
		                " at line number " + locator.getLineNumber());
	          } else if (locator.getLineNumber() > -1) {
	        	  // Doesn't currently happen in practice
		          log.warn("Line number " + locator.getLineNumber());	        	  
	          }
	          
	          // We get good results from XmlUtils.unmarshal(node)  :-)
	          if (locator.getNode()!=null) {
	        	  Node node = locator.getNode();
	        	  log.warn("troublesome node: " + XmlUtils.w3CDomNodeToString(node));
	        	  if (node.getParentNode()!=null) {
		        	  log.warn("in parent node: " + XmlUtils.w3CDomNodeToString(node.getParentNode()));
		        	  Node parent = node.getParentNode();
		        	  String path = "";
		        	  while (parent!=null) {
		        		  path = getLocalName(parent) + "/" + path;
		        		  parent = parent.getParentNode();
		        	  }
		        	  log.warn(path + getLocalName(node));
	        	  }
	          }

	          if (locator.getOffset()>-1) {
		          log.warn("At offset " + 
			                locator.getOffset());
	        	  
	          }
	          
	          if (locator.getObject()!=null ) {
	        	  log.warn(locator.getObject().getClass().getName());
	          }
	          
	     } else if (ve.getSeverity()==ve.WARNING) {
	    	 
	    	   log.warn(printSeverity(ve) + "Message is " + ve.getMessage());  
	    	   
	    	   // JAXB RI stops reporting errors after ten (issue 164)
	    	   if (ve.getMessage().startsWith("Errors limit exceeded")) {
	    		   resetErrorsCounter();
	    	   }
	    	   
	     }
	      // JAXB provider should attempt to continue its current operation. 
	      // (Marshalling, Unmarshalling, Validating)
	    if (shouldContinue) {
	    	log.info("continuing (with possible element/attribute loss)");
	    } else {
	    	if (log.isDebugEnabled()) {
	    		log.debug("shouldContinue is set to false", new Throwable());
	    	} else {
		    	log.info("shouldContinue is set to false");	    		
	    	}
	    }
	     return shouldContinue;
             
     }
    
	/**
	 * JAXB RI counts the errors it reports in a static field, UnmarshallingContext.errorsCounter,
	 * and after ten it reports no more: an element it could skip is then skipped unreported.  RI 2
	 * never reset the count, so a JVM's eleventh error went unreported for good (issue 164); RI 3
	 * and 4 reset it as each unmarshaller is made, so the limit is one part's now, and the count is
	 * shared by every thread unmarshalling at the time.  So when JAXB says the limit is reached,
	 * the count goes back to ten.  Until 17.3.6 this looked for RI 2's class only, so with
	 * docx4j-JAXB-ReferenceImpl (RI 4) the reset failed, logging three ERRORs.
	 */
	private static void resetErrorsCounter() {
		Field field = errorsCounterField();
		try {
			if (field != null) {
				field.setInt(null, 10);
				log.warn("JAXB's error count reset, so that it reports further errors (https://github.com/plutext/docx4j/issues/164)");
				return;
			}
		} catch (IllegalAccessException | RuntimeException e) {
			log.warn(e.getMessage());
		}
		log.warn("JAXB's error count can't be reset, so further errors in this part may go unreported (https://github.com/plutext/docx4j/issues/164)");
	}

	private static volatile Field errorsCounter;
	private static volatile boolean errorsCounterSought;

	/**
	 * The JAXB runtime's error count, made accessible; null where the runtime has none, or where
	 * docx4j may not reach it (on the module path, jaxb-runtime does not open the package to
	 * docx4j).
	 *
	 * @since 17.3.6
	 */
	static Field errorsCounterField() {
		if (!errorsCounterSought) {
			errorsCounter = findErrorsCounterField();
			errorsCounterSought = true;
		}
		return errorsCounter;
	}

	private static Field findErrorsCounterField() {
		String[] classes = Context.getJaxbImplementation() == JAXBImplementation.IBM_WEBSPHERE_XLXP
				// an IBM unmarshaller, eg WebSphere 7+ (com.ibm.xml.xlxp.jaxb.opti.level)
				? new String[] { "com.ibm.jtc.jax.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext" }
				: new String[] {
						"org.glassfish.jaxb.runtime.v2.runtime.unmarshaller.UnmarshallingContext", // RI 3 and 4
						"com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext", // RI 2
						"com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext" }; // the JDK's, before Java 11
		for (String c : classes) {
			try {
				Field f = Class.forName(c).getDeclaredField("errorsCounter");
				f.setAccessible(true);
				return f;
			} catch (ClassNotFoundException | NoSuchFieldException e) {
				// not this runtime
			} catch (RuntimeException e) {
				// InaccessibleObjectException: the package is not opened to docx4j
				log.warn(c + ".errorsCounter can't be reached (" + e.getMessage() + "); on the module path, "
						+ "--add-opens org.glassfish.jaxb.runtime/org.glassfish.jaxb.runtime.v2.runtime.unmarshaller=org.docx4j.core");
				return null;
			}
		}
		return null;
	}

    private String getLocalName(Node sourceNode) {
    	
    	if (sourceNode.getLocalName()==null) {
    		// eg element was created using createElement() 
    		return sourceNode.getNodeName();
    	
    	} else {
    		return sourceNode.getLocalName();
    	}
    	
    }    
    
    public String printSeverity(ValidationEvent ve) {
    	
    	String errorLevel;
    	
    	switch (ve.getSeverity()) {
    		case ValidationEvent.FATAL_ERROR: { 
              	// FATAL_ERROR is usually not actually fatal! 
    			errorLevel="(non)FATAL_ERROR"; 
    			break; } 
    		case ValidationEvent.ERROR: { errorLevel="ERROR"; break; }
    		case ValidationEvent.WARNING: { errorLevel="WARNING"; break; }
    		default: errorLevel = new Integer (ve.getSeverity()).toString() ;
    	}
    	
    	return "[" + errorLevel + "] ";
    	
    }
    
	public static void log(ExpressionContext expressionContext, String message ) {
		
		//log.info( com.sun.org.apache.xalan.internal.lib.NodeInfo.lineNumber(expressionContext ) + "  " +  message);
			// com.sun.org.apache hell 
		// but that only gives line number of input XML anyway, whereas more useful is
		// currently executing line number of XSLT.  ErrorListener seems to know this?  Explore some time...
		
		log.info( "[String] " + message);
	}
	
	public static void logXml(NodeIterator nodeIterator ) {
		// Has different method, to prevent Xalan preferring the String log method
		Node n = nodeIterator.nextNode();		
		log.info(XmlUtils.w3CDomNodeToString(n));
	}    
  
 }
