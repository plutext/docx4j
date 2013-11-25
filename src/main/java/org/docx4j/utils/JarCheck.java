/*
 * [JarCheck.java]
 *
 * Summary: Ensures javac -target versions of the class files in a jar are as expected.
 *
 * Copyright: (c) 2006-2011 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.5+
 *
 * Created with: JetBrains IntelliJ IDEA IDE http://www.jetbrains.com/idea/
 *
 * Version History:
 *  1.0 2006-01-16 initial version
 *  1.1 2006-01-16
 *  1.2 2006-03-05 reformat with IntelliJ, add Javadoc
 *  1.3 2008-04-21 display version number of each class file checked.
 */
package org.docx4j.utils;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.System.err;
import static java.lang.System.out;
/*
TODO: check class minor version as well.
     */

/**
 * Ensures javac -target versions of the class files in a jar are as expected.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.3 2008-04-21 display version number of each class file checked.
 * @since 2006-01-16
 * 
 * Modified by Jason Harrop 2011 06 25
 * Note re Java 1.5, 1.6 behaviour.
 * In 1.5, you can't put @Override on something which merely
 * implements an interface
 * Eclipse will give an error if you do (and have source set to 1.5).
 * Maven and ant won't give an error, and if you have target=1.5
 * (as we do for docx4j), will correctly produce 1.5 code.
 * This class allows us to check that all our dependencies are
 * also 1.5.
 * An alternative way of doing it would be to use clirr.sourceforge.net
 * which can run as an ant task, or from a command line, but I haven't 
 * tried that.
 * 
 */
public final class JarCheck
    {
    // ------------------------------ CONSTANTS ------------------------------

    /**
     * how many bytes at beginning of class file we read<br /> 4=ca-fe-ba-be + 2=minor + 2=major
     */
    private static final int chunkLength = 8;

    /**
     * undisplayed copyright notice
     */
    public static final String EMBEDDED_COPYRIGHT =
            "Copyright: (c) 2006-2011 Roedy Green, Canadian Mind Products, http://mindprod.com";

    private static final String RELEASE_DATE = "2008-04-21";

    /**
     * embedded version string.
     */
    public static final String VERSION_STRING = "1.3";

    /**
     * translate class file major version number to human JVM version
     */
    private static final HashMap<Integer, String> convertMachineToHuman =
            new HashMap<Integer, String>( 23 );

    /**
     * translate from human JDK version to class file major version number
     */
    private static final HashMap<String, Integer> convertHumanToMachine =
            new HashMap<String, Integer>( 23 );

    /**
     * expected first 4 bytes of a class file
     */
    private static final byte[] expectedMagicNumber =
            { ( byte ) 0xca, ( byte ) 0xfe, ( byte ) 0xba, ( byte ) 0xbe };

    // -------------------------- STATIC METHODS --------------------------

    static
        {
        convertHumanToMachine.put( "1.0", 44 );
        convertHumanToMachine.put( "1.1", 45 );
        convertHumanToMachine.put( "1.2", 46 );
        convertHumanToMachine.put( "1.3", 47 );
        convertHumanToMachine.put( "1.4", 48 );
        convertHumanToMachine.put( "1.5", 49 );
        convertHumanToMachine.put( "1.6", 50 );
        convertHumanToMachine.put( "1.7", 51 );
        }

    static
        {
        convertMachineToHuman.put( 44, "1.0" );
        convertMachineToHuman.put( 45, "1.1" );
        convertMachineToHuman.put( 46, "1.2" );
        convertMachineToHuman.put( 47, "1.3" );
        convertMachineToHuman.put( 48, "1.4" );
        convertMachineToHuman.put( 49, "1.5" );
        convertMachineToHuman.put( 50, "1.6" );
        convertMachineToHuman.put( 51, "1.7" );
        }

    /**
     * check one jar to make sure all class files have compatible versions.
     *
     * @param jarFilename name of jar file whose classes are to be tested.
     * @param low         low bound for major version e.g. 44
     * @param high        high bound for major version. e.g. 50
     *
     * @return true if all is ok. False if not, with long on System.err of problems.
     */
    private static boolean checkJar( String jarFilename, int low, int high )
        {
        out.println( "Checking jar " + jarFilename );
        boolean success = true;
        FileInputStream fis;
        ZipInputStream zip = null;
        
        int lowest = 1000;
        int highest = 0;
        
        try
            {
            try
                {
                fis = new FileInputStream( jarFilename );
                zip = new ZipInputStream( fis );

                // loop for each jar entry
                entryLoop:
                	
                	
                while ( true )
                    {
                    ZipEntry entry = zip.getNextEntry();
                    if ( entry == null )
                        {
                        break;
                        }
                    // relative name with slashes to separate dirnames.
                    String elementName = entry.getName();
                    //System.out.println(elementName);
                    if ( !elementName.endsWith( ".class" ) )
                        {
                        // ignore anything but a .final class file
                        continue;
                        }
                    byte[] chunk = new byte[ chunkLength ];
                    int bytesRead = zip.read( chunk, 0, chunkLength );
                    zip.closeEntry();
                    if ( bytesRead != chunkLength )
                        {
                        err.println( ">> Corrupt class file: "
                                     + elementName );
                        success = false;
                        continue;
                        }
                    // make sure magic number signature is as expected.
                    for ( int i = 0; i < expectedMagicNumber.length; i++ )
                        {
                        if ( chunk[ i ] != expectedMagicNumber[ i ] )
                            {
                            err.println( ">> Bad magic number in "
                                         + elementName );
                            success = false;
                            continue entryLoop;
                            }
                        }
                    /*
                    * pick out big-endian ushort major version in last two
                    * bytes of chunk
                    */
                    int major =
                            ( ( chunk[ chunkLength - 2 ] & 0xff ) << 8 ) + (
                                    chunk[ chunkLength - 1 ]
                                    & 0xff );
                    /* F I N A L L Y. All this has been leading up to this TEST */
                    
                    if (major>highest) highest=major;
                    if (major<lowest) {
                    	lowest=major;
                    }
                    
                    if ( low <= major && major <= high )
                        {
//                        out.print( "   OK " );
//                        out.println( convertMachineToHuman.get( major )
//                                     + " ("
//                                     + major
//                                     + ") "
//                                     + elementName );
                        // leave success set as previously
                        }
                    else if (  major > high ) {
                    	
                      err.println( ">> Wrong Version " );
                      err.println( convertMachineToHuman.get( major )
                                   + " ("
                                   + major
                                   + ") "
                                   + elementName );
                      success = false;
                    	                      
                    } else
                        {
                    	// Suppress low version error
                    	
//                        err.println( ">> Wrong Version " );
//                        err.println( convertMachineToHuman.get( major )
//                                     + " ("
//                                     + major
//                                     + ") "
//                                     + elementName );
//                        success = false;
                        }
                    }
                // end while
                }
            catch ( EOFException e )
                {
                // normal exit
            	
                }
            zip.close();
            
        	if (lowest==highest) {
        		System.out.println( convertMachineToHuman.get( highest ));
        	} else {
        		System.out.println( convertMachineToHuman.get( lowest ) + "-" + convertMachineToHuman.get( highest ));
        		
        	}
            
            return success;
            }
        catch ( IOException e )
            {
            err.println( ">> Problem reading jar file." );
            return false;
            }
        }

    // --------------------------- main() method ---------------------------

    /**
     * Main command line jarfileName lowVersion highVersion e.g. myjar.jar 1.0 1.6
     *
     * @param args rot used
     */
    public static void main( String[] args )
        {
//            if ( args.length != 3 )
//                {
//                err.println( "usage: java -ea -jar jarcheck.jar jarFileName.jar 1.1 1.6" );
//                System.exit( 2 );
//                }
//            String jarFilename = args[ 0 ];

//          int low = convertHumanToMachine.get( args[ 1 ] );
//          int high = convertHumanToMachine.get( args[ 2 ] );
          
        int low  = convertHumanToMachine.get( "1.3" );
        int high = convertHumanToMachine.get( "1.5" );
            
            String dirPath = System.getProperty("user.dir") + "/dist/";
            
            File dir = new File(dirPath);
            
            if (dir.isDirectory() ) {
            	
            	File[] files = dir.listFiles();
            	for (int i=0 ; i<files.length; i++) {
            		
            		if (files[i].isFile() && files[i].getName().endsWith("jar")) {
            			
                        System.out.println(files[i].getName() );
                        boolean success = checkJar( files[i].getAbsolutePath(), low, high );
                        System.out.println(success);
                        
                    }            		
            	}
            }
}
        }