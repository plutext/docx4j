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

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.docx4j.XmlUtils;
import org.docx4j.utils.ResourceUtils;


public class JaxbValidationEventHandler implements 
ValidationEventHandler{
    
	private static Logger log = LoggerFactory.getLogger(JaxbValidationEventHandler.class);		
	
	private boolean shouldContinue = false; // fail by default
	public void setContinue(boolean val) {
		shouldContinue = val;
	}
	
	public final static String UNEXPECTED_MC_ALTERNATE_CONTENT = "unexpected element (uri:\"http://schemas.openxmlformats.org/markup-compatibility/2006\", local:\"AlternateContent\")";
	
	static Templates mcPreprocessorXslt;	
	
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
	    	   
	    	   // Workaround for issue with recent non-MOXy JAXB (eg RI 2.2.11)
	    	   if (ve.getMessage().startsWith("Errors limit exceeded")) {
	
	    		   try {
		     		    log.warn("Resetting error counter to work around https://github.com/gf-metro/jaxb/issues/22");
		     		    	// As at 2017 06 29 that repo has disappeared!
		     		    	// See instead https://github.com/javaee/jaxb-v2/
		     		    
		   				Field field = null;
		   				if (Context.getJaxbImplementation() == JAXBImplementation.ORACLE_JRE) {
							field = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext").getDeclaredField("errorsCounter");
		   				} else if (Context.getJaxbImplementation() == JAXBImplementation.IBM_WEBSPHERE_XLXP) {
		   					// Its IBM's implementation eg Websphere 7+ where an IBM Unmarshaller is being used
		   					// (controlled by com.ibm.xml.xlxp.jaxb.opti.level)
			     		    log.warn("with IBM unmarshaller");
		   					field = Class.forName("com.ibm.jtc.jax.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext").getDeclaredField("errorsCounter");

		   				} else {
							try {
								field = Class.forName("com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext").getDeclaredField("errorsCounter");
							} catch (Exception e) {
				   				log.error("Trying to reset error counter, but not using JAXB RI:- ");		   					
				   				log.error(e.getMessage());
				   			}
						}
		   				
		   				if (field==null) {
			   				log.error("Unable to reset error counter. See https://github.com/plutext/docx4j/issues/164");		   					
		   				} else {
			   		        field.setAccessible(true);
			   		        field.set(null, 10);
			   		        log.warn(".. reset successful");		   					
		   				}
		   			} catch (Exception e) {
		   				log.error(e.getMessage());
		   				log.error("Unable to reset error counter. See https://github.com/plutext/docx4j/issues/164");
		   			} 
	    		   
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
  
 }
