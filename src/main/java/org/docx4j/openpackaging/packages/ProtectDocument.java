/**
 * 
 */
package org.docx4j.openpackaging.packages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.utils.CompoundTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STCryptProv;
import org.docx4j.wml.STDocProtect;
import org.docx4j.wml.Tbl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jharrop
 *
 */
public class ProtectDocument extends ProtectionSettings {
	
	protected static Logger log = LoggerFactory.getLogger(ProtectDocument.class);
	
	
	public ProtectDocument(WordprocessingMLPackage pkg) {
		super(pkg);
	}
	
	private WordprocessingMLPackage getPkg() {
		return (WordprocessingMLPackage)pkg;
	}
	
	/**
	 * Restrict allowed formatting to specified styles, no password.
	 * 
	 * @param allowedStyleNames
	 * @param removedNotAllowedFormatting whether existing usages of styles which aren't allowed are removed
	 * @param autoFormatOverride
	 * @param styleLockTheme
	 * @param styleLockQFSet
	 * @throws Docx4JException
	 * @since 3.3.0
	 */
	public void restrictFormatting(List<String> allowedStyleNames, boolean removedNotAllowedFormatting,
			boolean autoFormatOverride, boolean styleLockTheme, boolean styleLockQFSet) throws Docx4JException {
		
		restrictFormatting(allowedStyleNames, removedNotAllowedFormatting,
				autoFormatOverride, styleLockTheme, styleLockQFSet,
				null, null);
	}

	/**
	 * Restrict allowed formatting to specified styles, password protected.  Defaults to sha1.
	 * 
	 * @param allowedStyleNames
	 * @param removedNotAllowedFormatting whether existing usages of styles which aren't allowed are removed
	 * @param autoFormatOverride
	 * @param styleLockTheme
	 * @param styleLockQFSet
	 * @param password
	 * @throws Docx4JException
	 * @since 3.3.0
	 */
	public void restrictFormatting(List<String> allowedStyleNames, boolean removedNotAllowedFormatting,
			boolean autoFormatOverride, boolean styleLockTheme, boolean styleLockQFSet,
			String password) throws Docx4JException {
		
		restrictFormatting(allowedStyleNames, removedNotAllowedFormatting,
				autoFormatOverride, styleLockTheme, styleLockQFSet,
				password, HashAlgorithm.sha1);	
		
	}
	
	/**
	 * Restrict allowed formatting to specified styles. Specify password and HashAlgorithm 
	 * 
	 * @param allowedStyleNames
	 * @param removedNotAllowedFormatting whether existing usages of styles which aren't allowed are removed
	 * @param autoFormatOverride
	 * @param styleLockTheme
	 * @param styleLockQFSet
	 * @param password
	 * @param hashAlgo
	 * @throws Docx4JException
	 * @since 3.3.0
	 */
	public void restrictFormatting(List<String> allowedStyleNames, boolean removedNotAllowedFormatting,
			boolean autoFormatOverride, boolean styleLockTheme, boolean styleLockQFSet,
			String password, HashAlgorithm hashAlgo) throws Docx4JException {

		if (getPkg().getMainDocumentPart()==null) 
			throw new Docx4JException("No MainDocumentPart in this WordprocessingMLPackage");
		
		if (getPkg().getMainDocumentPart().getStyleDefinitionsPart()==null)  
			throw new Docx4JException("No StyleDefinitionsPart in this WordprocessingMLPackage");

		Set<String> stylesInUse = getPkg().getMainDocumentPart().getStylesInUse();
		
		// The styles part
		StyleDefinitionsPart sdp = getPkg().getMainDocumentPart().getStyleDefinitionsPart();
		sdp.protectRestrictFormatting(allowedStyleNames, removedNotAllowedFormatting, stylesInUse);
		
			// TODO: do the same to stylesWithEffects.xml

		
		// The main document part (and other content parts)
		if (removedNotAllowedFormatting) {
			
			List<TraversalUtilVisitor> visitors = new ArrayList<TraversalUtilVisitor>();	
			visitors.add(new VisitorRemovePFormatting(sdp, allowedStyleNames));
			visitors.add(new VisitorRemoveRFormatting(sdp, allowedStyleNames));
			visitors.add(new VisitorRemoveTableFormatting(sdp, allowedStyleNames));
			
			CompoundTraversalUtilVisitorCallback compound = new CompoundTraversalUtilVisitorCallback(visitors);
			
			for( Part p : getPkg().getParts().getParts().values()) {
				
				if (p instanceof ContentAccessor) {
					compound.walkJAXBElements(((ContentAccessor)p).getContent());								
				}
			}
			
		}
		
		// The document settings part
		DocumentSettingsPart documentSettingsPart = getPkg().getMainDocumentPart().getDocumentSettingsPart(true);
		documentSettingsPart.protectRestrictFormatting(autoFormatOverride, styleLockTheme, styleLockQFSet, password, hashAlgo);
		
		// app.xml DocSecurity
		if (pkg.getDocPropsExtendedPart()==null) 
		{
			pkg.addDocPropsExtendedPart();
		}		
		this.setDocSecurity(0); // same as Word 2013
	}
	
	private static class VisitorRemovePFormatting extends TraversalUtilVisitor<P> {
		
		StyleDefinitionsPart sdp;		
		List<String> allowedStyleNames;
		
		VisitorRemovePFormatting(StyleDefinitionsPart sdp, List<String> allowedStyleNames) {
			this.sdp = sdp;
			this.allowedStyleNames = allowedStyleNames;
		}
		
		@Override
		public void apply(P p, Object parent, List<Object> siblings) {
			
			if (p.getPPr()!=null && p.getPPr().getPStyle()!=null && 
					!allowedStyleNames.contains(sdp.getNameForStyleID(p.getPPr().getPStyle().getVal()))) {
				p.getPPr().setPStyle(null);
			}
		}	
	}	

	private static class VisitorRemoveRFormatting extends TraversalUtilVisitor<R> {
		
		StyleDefinitionsPart sdp;		
		List<String> allowedStyleNames;
		
		VisitorRemoveRFormatting(StyleDefinitionsPart sdp, List<String> allowedStyleNames) {
			this.sdp = sdp;
			this.allowedStyleNames = allowedStyleNames;
		}
		
		@Override
		public void apply(R r, Object parent, List<Object> siblings) {
			
			if (r.getRPr()!=null && r.getRPr().getRStyle()!=null && 
					!allowedStyleNames.contains(sdp.getNameForStyleID(r.getRPr().getRStyle().getVal()))) {
				r.getRPr().setRStyle(null);
			}
		}	
	}	

	private static class VisitorRemoveTableFormatting extends TraversalUtilVisitor<Tbl> {
		
		StyleDefinitionsPart sdp;		
		List<String> allowedStyleNames;
		
		VisitorRemoveTableFormatting(StyleDefinitionsPart sdp, List<String> allowedStyleNames) {
			this.sdp = sdp;
			this.allowedStyleNames = allowedStyleNames;
		}
		
		@Override
		public void apply(Tbl table, Object parent, List<Object> siblings) {
			
			if (table.getTblPr()!=null && table.getTblPr().getTblStyle()!=null && 
					!allowedStyleNames.contains(sdp.getNameForStyleID(table.getTblPr().getTblStyle().getVal()))) {
				table.getTblPr().setTblStyle(null);
			}
		}	
	}
	
    /**
     * Verifies the documentProtection tag inside settings.xml file 
     * if the protection is enforced (w:enforcement="1") 
     * and if the kind of protection equals to passed (STDocProtect.Enum editValue) 
     *
     * @return true if documentProtection is enforced with option readOnly
	 * @since 3.3.0
     */
    public boolean isRestrictEditingWith(STDocProtect editValue) {
    	
		DocumentSettingsPart documentSettingsPart = getPkg().getMainDocumentPart().getDocumentSettingsPart();
		
		if (documentSettingsPart == null) {
			return false;
		} else {
			return isRestrictEditingWith( editValue);
		}
    	
    }
    

    /**
     * Enforces the protection with the option specified by passed editValue.<br/>
     * <br/>
     * In the documentProtection tag inside settings.xml file <br/>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br/>
     * and the value of edit to the passed editValue (w:edit="[passed editValue]")<br/>
     * <br/>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;[passed editValue]&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
	 * @since 3.3.0
     */
    public void restrictEditing(org.docx4j.wml.STDocProtect editValue) {
    	
    	restrictEditing(editValue, null, null);
    }

    /**
     * Enforces the protection with the option specified by passed editValue and password,
     * using rsaFull (sha1) (like Word 2010).
     * 
     * WARNING: this functionality may give a false sense of security, since it only affects
     * the behaviour of Word's user interface. A mischevious user could still edit the document
     * in some other program, and subsequent users would *not* be warned it has been tampered with. 
     *
     * @param editValue the protection type
     * @param password  the plaintext password, if null no password will be applied
     * @param hashAlgo  the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                  if null, it will default default to sha512 (like Word 2013)
	 * @since 3.3.0
     */
    public void restrictEditing(org.docx4j.wml.STDocProtect editValue,
                                        String password) {

    	restrictEditing(editValue, password, HashAlgorithm.sha512);
    	
		/* Word 2013
		 * 
                        w:cryptProviderType="rsaAES" w:cryptAlgorithmClass="hash" w:cryptAlgorithmType="typeAny" 
                        w:cryptAlgorithmSid="14" w:cryptSpinCount="100000"
                        
           corresponds to sha512
           
                case sha256:
                    providerType = STCryptProv.RSA_AES;
                    sid = 12;
                    break;
                case sha384:
                    providerType = STCryptProv.RSA_AES;
                    sid = 13;
                    break;
                case sha512:
                    providerType = STCryptProv.RSA_AES;
                    sid = 14;
                                            
          */
    	
    }

    /**
     * Enforces the protection with the option specified by passed editValue, password, and 
     * HashAlgorithm for the password.

     *
     * @param editValue the protection type
     * @param password  the plaintext password, if null no password will be applied
     * @param hashAlgo  the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                  if null, it will default default to sha512 (like Word 2013)
	 * @since 3.3.0
     */
    public void restrictEditing(org.docx4j.wml.STDocProtect editValue,
                                        String password, HashAlgorithm hashAlgo) {
    	
		DocumentSettingsPart documentSettingsPart = null;
		try {
			documentSettingsPart = getPkg().getMainDocumentPart().getDocumentSettingsPart(true);
		} catch (InvalidFormatException e) {
			log.warn(e.getMessage(), e);
		}
		
		documentSettingsPart.protectRestrictEditing(editValue, password, hashAlgo);
		
		// app.xml DocSecurity
		if (pkg.getDocPropsExtendedPart()==null) 
		{
			pkg.addDocPropsExtendedPart();
		}	
		
		// same as Word 2013:	
		if (editValue==STDocProtect.COMMENTS) {
			this.setDocSecurity(8); // verified		
		} else if (editValue==STDocProtect.FORMS) {
			this.setDocSecurity(0); // I think		
		}  else if (editValue==STDocProtect.NONE) {
			this.setDocSecurity(0); // I guess	
		} else if (editValue==STDocProtect.READ_ONLY) {
			this.setDocSecurity(8); // verified			
		} else if (editValue==STDocProtect.TRACKED_CHANGES) {
			this.setDocSecurity(0); 	
		}
		
		

    }
    

    /**
     * Validates the existing password
     *
     * @param password
     * @return true, only if password was set and equals, false otherwise
	 * @since 3.3.0
     */
    public boolean validateProtectionPassword(String password) {
    	
		DocumentSettingsPart documentSettingsPart = getPkg().getMainDocumentPart().getDocumentSettingsPart();
		
		if (documentSettingsPart == null) {
			return false;
		} else {
			return documentSettingsPart.validateProtectionPassword(password);
		}
    }

    /**
     * Removes protection enforcement.<br/>
     * In the documentProtection tag inside settings.xml file <br/>
     * it sets the value of enforcement to "0" (w:enforcement="0") <br/>
	 * @since 3.3.0
     */
    public void removeEnforcement() {
    	
		DocumentSettingsPart documentSettingsPart = getPkg().getMainDocumentPart().getDocumentSettingsPart();
		
		if (documentSettingsPart == null) {
			return;
		} else {
			documentSettingsPart.removeEnforcement();
		}
    }	
	

}
