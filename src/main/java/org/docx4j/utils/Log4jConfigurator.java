package org.docx4j.utils;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.docx4j.Docx4jProperties;

/* From http://wiki.apache.org/logging-log4j/UsefulCode
 * 
 * This will be used if log4.xml config file is
 * not found on your path, unless 
 * Property("docx4j.Log4j.Configurator.disabled") is set 
 * to true.
 * 
 * There is a log4j config file in src/main/resources;
 * if that is on your path, that is where your log4j
 * config will come from.
 * 
 */
public class Log4jConfigurator {

    public synchronized static void configure() {
    	
		boolean disabled = Boolean.parseBoolean(
				Docx4jProperties.getProperties().getProperty("docx4j.Log4j.Configurator.disabled", "false"));   			
		if (disabled) return;
		
        if (!isConfigured()) {
        	
            BasicConfigurator.configure();
        
	        // So now we should have a ConsoleAppender
	        // we don't want debug level logging.
	        Logger.getRootLogger().setLevel(Level.INFO);

	        PatternLayout layout = new PatternLayout("%p %c %x.%M line %L - %m%n");
	    	((Appender)Logger.getRootLogger().getAllAppenders().nextElement()).setLayout(layout);
	        
	        Logger log = Logger.getLogger(Log4jConfigurator.class);
	        log.info("Since your log4j configuration (if any) was not found, docx4j has configured log4j automatically.");
	        
	        try {
	        	org.docx4j.convert.out.pdf.viaXSLFO.Conversion.log.setLevel(Level.DEBUG);
	        } catch (NoClassDefFoundError n) {
	        	// If FOP jar is not available
	        }
        }
    }

    /**
    Returns true if it appears that log4j have been previously configured. This code
    checks to see if there are any appenders defined for log4j which is the
    definitive way to tell if log4j is already initialized */
	private static boolean isConfigured() {
	    Enumeration appenders = Logger.getRootLogger().getAllAppenders();
	    if (appenders.hasMoreElements()) {
	        return true;
	    }
	    else {
	        Enumeration loggers = LogManager.getCurrentLoggers() ;
	        while (loggers.hasMoreElements()) {
	            Logger c = (Logger) loggers.nextElement();
	            if (c.getAllAppenders().hasMoreElements())
	                return true;
	        }
	    }
	    return false;
	}

}
