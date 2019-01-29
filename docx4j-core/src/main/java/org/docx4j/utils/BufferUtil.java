package org.docx4j.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BufferUtil {

	private static Logger log = LoggerFactory.getLogger(BufferUtil.class);		
	
	
    private static final int BUFFER_SIZE = 1024;

    /**
     * Fully reads the given InputStream, returning its contents as a ByteBuffer
     *
     * @param in an <code>InputStream</code> value
     * @return a <code>ByteBuffer</code> value
     * @exception IOException if an error occurs
     */
    public static ByteBuffer readInputStream(InputStream in) throws IOException {
    	/**
    	 * Some useful methods for reading URLs or streams and returning the data as Direct Buffers
    	 *
    	 * @author Ivan Z. Ganza
    	 * @author Robert Schuster
    	 * @author Bart LEBOEUF
    	 * 
    	 * Originally from com.lilly.chorus.eSignatures.utils.BufferUtil
    	 */
    	
    	/*
		 * Converts the InputStream into a channels which allows us to read into
		 * a ByteBuffer.
		 */
		ReadableByteChannel ch = Channels.newChannel(in);

		// Creates a list that stores the intermediate buffers.
		List list = new LinkedList();

		// A variable storing the accumulated size of the data.
		int sum = 0, read = 0;

		/*
		 * Reads all the bytes from the channel and stores them in various
		 * buffer objects.
		 */
		do {
			ByteBuffer b = createByteBuffer(BUFFER_SIZE);
			read = ch.read(b);

			if (read > 0) {
				((Buffer)b).flip(); // make ready for reading later
				list.add(b);
				sum += read;
			}
		} while (read != -1);

		/*
		 * If there is only one buffer used we do not need to merge the data.
		 */
		if (list.size() == 1) {
			return (ByteBuffer) list.get(0);
		}

		ByteBuffer bb = createByteBuffer(sum);

		/* Merges all buffers into the Buffer bb */
		Iterator ite = list.iterator();
		while (ite.hasNext()) {
			bb.put((ByteBuffer) ite.next());
		}

		list.clear();

		return bb;
	}
    
    
    public static ByteBuffer createByteBuffer(int capacity) {
    	return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
    
    /**
     * Returns an input stream for a ByteBuffer.
     * The read() methods use the relative ByteBuffer get() methods.
     */
    public static InputStream newInputStream(final ByteBuffer buf)
    {
    	
    // From http://docs.jboss.org/jbossas/javadoc/4.0.2/org/jboss/media/util/ByteBufferUtils.java.html
    	
    	// Not working properly?  Make sure any method being called is implemented
    	
       return new InputStream()
       {
          public synchronized int read() throws IOException
          {
             if (!buf.hasRemaining())
             {
            	 log.debug("done");
                return -1;
             }
             log.debug("#");
             return buf.get();
          }

          public synchronized int read(byte[] bytes) throws IOException
          {
             if (!buf.hasRemaining())
             {
            	 log.debug("done");
                return -1;
             }
             log.debug("#");
                                       
             int len = Math.min(bytes.length, buf.remaining());
             buf.get(bytes, 0, len );
             return len;
          }          
          
          public synchronized int read(byte[] bytes, int off, int len)
             throws IOException
          {
             // Read only what's left
             len = Math.min(len, buf.remaining());
             buf.get(bytes, off, len);
             return len;
          }
       };
    }
    
	public static byte[] getBytesFromInputStream(InputStream is)
			throws IOException {

		BufferedInputStream bufIn = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		int c = bufIn.read();
		while (c != -1) {
			bos.write(c);
			c = bufIn.read();
		}
		bos.flush();
		baos.flush();
		bufIn.close();
		bos.close();
		return baos.toByteArray();
	} 		
    
    
}
