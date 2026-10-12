package org.docx4j.openpackaging.parts.WordprocessingML;
// Ideally this part would have been created in the parts 
// package itself, since it is also used when a 
// chart is embedded in a pptx.  ie there is nothing
// about it which is specific to WML.

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.BufferUtil;

public class EmbeddedPackagePart extends BinaryPart { // maybe should extend EmbeddedPackagePart

	private static Logger log = LoggerFactory.getLogger(EmbeddedPackagePart.class);		
	
	public EmbeddedPackagePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public EmbeddedPackagePart() throws InvalidFormatException {
		super( new PartName("/word/embeddings/foo") ); // eg Microsoft_Office_Powerpoint_Presentation.pptx
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
//		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
//				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATION));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.EMBEDDED_PKG);
	}
	
	/**
	 * @return
	 * @throws Docx4JException
	 * @since 3.0.0
	 */
	public OpcPackage extractPackage() throws Docx4JException {
		
		return OpcPackage.load(
				BufferUtil.newInputStream(getBuffer()));
		
	}

	// ------------------------------------------------------------------ the live package (CR-036)

	private OpcPackage embedded;

	/**
	 * The embedded package, kept: loaded from this part's bytes the first time, and from then on
	 * the package this part is (its bytes are written from it whenever they are asked for, so a
	 * save of the host package writes its changes).  {@link #extractPackage()} by contrast loads
	 * a copy each time.
	 *
	 * @since 17.3.3 (CR-036)
	 */
	public OpcPackage getEmbeddedPackage() throws Docx4JException {
		if (embedded == null) {
			ByteBuffer bb = super.getBuffer();
			if (bb == null) throw new Docx4JException(getPartName() + " has no content");
			embedded = OpcPackage.load(BufferUtil.newInputStream(bb));
		}
		return embedded;
	}

	/**
	 * The package this part is from now on (its bytes written from it as they are asked for);
	 * null lets a live package go without writing it, the part keeping the bytes it had
	 *
	 * @since 17.3.3 (CR-036)
	 */
	public void setEmbeddedPackage(OpcPackage pkg) {
		this.embedded = pkg;
	}

	/**
	 * Whether the part holds a live package ({@link #getEmbeddedPackage()} has been asked for, or
	 * {@link #setEmbeddedPackage(OpcPackage)} given one): a reader that only reads uses it if so,
	 * and a copy if not, so that reading doesn't make a save rewrite the part
	 *
	 * @since 17.3.3 (CR-036)
	 */
	public boolean hasEmbeddedPackage() {
		return embedded != null;
	}

	/**
	 * The live package written into this part's bytes now, and kept
	 *
	 * @since 17.3.3 (CR-036)
	 */
	public void writeEmbeddedPackage() throws Docx4JException {
		if (embedded == null) return;
		super.setBinaryData(serialise(embedded));
	}

	/**
	 * The live package written into this part's bytes, and let go: later readers load the bytes
	 *
	 * @since 17.3.3 (CR-036)
	 */
	public void releaseEmbeddedPackage() throws Docx4JException {
		writeEmbeddedPackage();
		embedded = null;
	}

	private static byte[] serialise(OpcPackage pkg) throws Docx4JException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		pkg.save(os);
		return os.toByteArray();
	}

	/** With a live package, its bytes as they are now */
	@Override
	public ByteBuffer getBuffer() {
		if (embedded != null) {
			try {
				return ByteBuffer.wrap(serialise(embedded));
			} catch (Docx4JException e) {
				log.error("Couldn't write the embedded package of " + getPartName(), e);
				// the bytes as they were, rather than none
			}
		}
		return super.getBuffer();
	}

	@Override
	public boolean isLoaded() {
		return embedded != null || super.isLoaded();
	}

	/** Bytes set explicitly replace a live package */
	@Override
	public void setBinaryData(InputStream binaryData) {
		embedded = null;
		super.setBinaryData(binaryData);
	}

	@Override
	public void setBinaryData(byte[] bytes) {
		embedded = null;
		super.setBinaryData(bytes);
	}

	@Override
	public void setBinaryData(ByteBuffer bb) {
		embedded = null;
		super.setBinaryData(bb);
	}


}
