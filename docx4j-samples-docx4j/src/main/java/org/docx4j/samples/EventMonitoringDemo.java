/*
 *  Copyright 2014, Plutext Pty Ltd.
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

package org.docx4j.samples;


import java.io.File;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.listener.Handler;

import org.docx4j.Docx4J;
import org.docx4j.events.Docx4jEvent;
import org.docx4j.events.PackageIdentifierTransient;
import org.docx4j.events.StartEvent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;



/**
 * Simple example of monitoring docx4j events.
 * 
 * Uses the following library https://github.com/bennidi/mbassador 
 * to provide the event plumbing; see that page for more details about it.
 * 
 * Get the jar from http://search.maven.org/#artifactdetails%7Cnet.engio%7Cmbassador%7C1.1.10%7Cbundle
 * 
 * 
 * @author jharrop
 * @since 3.1.0
 */
public class EventMonitoringDemo extends AbstractSample {
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		}
		System.out.println(inputfilepath);	   
		
		// Creation of message bus
		MBassador<Docx4jEvent> bus = new MBassador<Docx4jEvent>(
				new BusConfiguration()
			     .addFeature(Feature.SyncPubSub.Default()) // configure the synchronous message publication
			     .addFeature(Feature.AsynchronousHandlerInvocation.Default()) // configure asynchronous invocation of handlers
			     .addFeature(Feature.AsynchronousMessageDispatch.Default()) // configure asyncronous message publication (fire&forget)
			     );
		Docx4jEvent.setEventNotifier(bus);
		
		//  and registration of listeners
		ListeningBean listener = new ListeningBean();
		bus.subscribe(listener);		
		
		
		// Approach 1: Load the docx from a file 
		// In this case the events get a name for the events related to this package from the filename
		//WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));

		// Approach 2: Load the docx from a file 
		// In this case the events get a name for the events related to this package from the filename
		PackageIdentifierTransient pkgIdentifier = new PackageIdentifierTransient("templateXYZ");
		WordprocessingMLPackage wordMLPackage = Docx4J.load(pkgIdentifier, new java.io.File(inputfilepath));
		
		// Approach 3: Load the docx from an input stream
		// In this case, without more, we can't identify which pkg the events belong to
		// so a unique identifier is automatically assigned
//		FileInputStream fis = new FileInputStream(new java.io.File(inputfilepath));
//		WordprocessingMLPackage wordMLPackage = Docx4J.load(fis);

		// Approach 4:  Load the docx from an input stream
		// assigning your identifier
//		FileInputStream fis = new FileInputStream(new java.io.File(inputfilepath));
//		PackageIdentifierTransient pkgIdentifier = new PackageIdentifierTransient("myID");
//		WordprocessingMLPackage wordMLPackage = Docx4J.load(pkgIdentifier, fis);
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_OpenAndSaveRoundTripTest.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
	}
		
	/**
	 * Implement your net.engio.mbassy.listener.Handler 
	 *
	 */
	static class ListeningBean {
		
		 // every message of type Docx4jEvent  will be delivered
	    // to this handler; NPEs etc in this handler will be silently ignored.
	    @Handler
	    public void handleMessage(Docx4jEvent message) {
	    	
	    	String state = (message instanceof StartEvent) ? "starting" : "finished";
	    	
	    	
	    	
	    	if (message.getPkgIdentifier()==null) {

	    		System.out.println("\n\n\n\n ****  " + message.getProcessStep() + " " + state + " ***** \n\n");
	    		
	    	} else {

	    		System.out.println("\n\n\n\n **** " + message.getPkgIdentifier().name() + ": " 
	    				+ message.getProcessStep() + " " + state + " ***** \n\n");
	    		
	    	}
	    		
	    }
		
	}	

}
