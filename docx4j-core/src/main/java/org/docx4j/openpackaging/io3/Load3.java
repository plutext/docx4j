/*
 *  Copyright 2007-2012, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.io3;



import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.jaxb.McLoadMode;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.packages.LoadFinding;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.PackageRelsUtil;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.io.ExternalResourceUtils;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.DocPropsCoverPagePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.QuestionsPart;
import org.docx4j.openpackaging.parts.opendope.StandardisedAnswersPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Create a Package object using a PartLoader.
 * 
 * This class doesn't care how the parts are physically
 * stored (that is PartLoader's problem).  
 * 
 * What this class knows how to do is to traverse the
 * opc, via its relationships. It would be possible/interesting
 * to have a design which lazily followed the rels, but
 * we don't do that. All rel parts are unmarshalled
 * and traversed when invoked.
 * 
 * @author jharrop
 * 
 */
public class Load3 extends Load {
		
	private static Logger log = LoggerFactory.getLogger(Load3.class);


	private PartStore partStore;	
	
	public Load3(PartStore partLoader) {
		this.partStore = partLoader;
	}

	private McLoadMode mcLoadMode;

	/**
	 * What this load does with an mc:AlternateContent at block, row or cell level (CR-051 section
	 * 3.2): set on the package before any part is unmarshalled, so that it governs every part.
	 * Null (the default) leaves the package to the property {@value McLoadMode#PROPERTY}.
	 *
	 * @since 17.3.4 (CR-051 phase 1)
	 */
	public void setMcLoadMode(McLoadMode mode) {
		this.mcLoadMode = mode;
	}

	private RepairMode repairMode;

	/**
	 * What this load does with a package Word repairs (CR-075 D1); null (the default) follows
	 * {@value RepairMode#PROPERTY}.
	 *
	 * @since 17.3.6 (CR-075 phase 3)
	 */
	public void setRepairMode(RepairMode mode) {
		this.repairMode = mode;
	}

	public Load3() {
		throw new RuntimeException();
	}
	
//	public OpcPackage get(String filepath) throws Docx4JException {
//		return get(new File(filepath));
//	}
//	
//	
//	public OpcPackage get(File f) throws Docx4JException {
//		log.info("Filepath = " + f.getPath() );
//		
//		partLoader = new ZipPartLoader(f);
//		
//		return process();
//	}
//
//	public OpcPackage get(InputStream is) throws Docx4JException {
//
//		partLoader = new ZipPartLoader(is);
//	            
//		// At this point, we're finished with the zip input stream
//        // TODO, so many of the below methods could be renamed.
//        // If performance is ok, LoadFromJCR could be refactored to
//        // work the same way
//		
//		return process();
//	}
	
	/**
	 * By setting this, you can load the contents into 
	 * this existing package (ie instead of returning a new one).
	 * 
	 * @param existingPkg
	 * @since 3.3.7
	 */
	public void reuseExistingOpcPackage(OpcPackage existingPkg) {
		this.existingPkg = existingPkg;
	}	
	private OpcPackage existingPkg = null;
	
	public OpcPackage get() throws Docx4JException {
		
		long startTime = System.currentTimeMillis();				

		// 1. Get [Content_Types].xml
		ContentTypeManager ctm = new ContentTypeManager();
		InputStream is = null;
		try {
			is = partStore.loadPart("[Content_Types].xml");		
			ctm.parseContentTypesFile(is);
		} catch (Docx4JException e) {
			throw new Docx4JException("Couldn't get [Content_Types].xml from ZipFile", e);
		} catch (NullPointerException e) {
			throw new Docx4JException("Couldn't get [Content_Types].xml from ZipFile", e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		// .. now find the name of the main part
		RelationshipsPart rp = RelationshipsPart.createPackageRels();
		LoadFinding packageRelsNotRead = populatePackageRels(rp);
		LoadFinding multipleMainParts = oneMainPart(rp);
		
		String mainPartName = PackageRelsUtil.getNameOfMainPart(rp);
		PartName mainPartNameObj;
		if (mainPartName.startsWith("/")) {
			// OpenXML SDK 2.0 writes Target="/word/document.xml" (note leading "/")
			mainPartNameObj = new PartName(mainPartName);
		} else { 
			// Microsoft Word, docx4j etc write Target="word/document.xml" 
			mainPartNameObj = new PartName("/" + mainPartName);
		}
		String pkgContentType = ctm.getContentType(mainPartNameObj);

		// 2. Create a new Package; this'll return the appropriate subclass
		OpcPackage p = ctm.createPackage(pkgContentType);
		if (existingPkg==null) {
			p = ctm.createPackage(pkgContentType);
		} else {
			if (existingPkg.getClass()!=p.getClass()) {
				throw new Docx4JException("Can't fill " + existingPkg.getClass().getName() 
						+ " with " + p.getClass().getName() + " contents." );
			}
			log.info("loading into (re-using) existing package object " + existingPkg.hashCode()); 
			existingPkg.reset();
			existingPkg.setPartName(new PartName("/", false));
			existingPkg.setContentTypeManager(ctm);
			p = existingPkg;
		}
		log.info("Instantiated package of type " + p.getClass().getName() );
		p.setSourcePartStore(partStore);
		this.loading = p;
		p.setLoadFindings(partStore instanceof ZipPartStore ? ((ZipPartStore) partStore).getLoadFindings() : null);
		if (packageRelsNotRead != null) p.addLoadFinding(packageRelsNotRead);
		if (multipleMainParts != null) p.addLoadFinding(multipleMainParts);
		p.setRepairMode(effectiveRepairMode());
		if (mcLoadMode != null) {
			// before any part is unmarshalled (CR-051 section 3.2), as setWasStrict below
			p.setMcLoadMode(mcLoadMode);
		}

		p.setRelationships(rp);
		rp.setSourceP(p); //
		
		// @since 11.5.9
		if (PackageRelsUtil.isStrict(rp)) {
			log.info("Loading Strict pkg");
			p.setWasStrict(true);
			rp.importStrict();
		}
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(p, rp, ctm );

		// 6.
		registerCustomXmlDataStorageParts(p);
		passOverUnreadableStores(p);
		
//		partStore.finishLoad();
		
		existingPkg = null;
		
		long endTime = System.currentTimeMillis();
		log.info("package read;  elapsed time: " + Math.round((endTime-startTime)) + " ms" );
		 
		 return p;
	}

	/** The package this load is filling, for what it finds. */
	private OpcPackage loading;

	/** The deepest message of an exception, which is the parser's, not a wrapper's. */
	private static String deepestMessage(Throwable t) {
		String message = null;
		for (Throwable c = t; c != null; c = c.getCause() == c ? null : c.getCause()) {
			if (c instanceof jakarta.xml.bind.JAXBException && ((jakarta.xml.bind.JAXBException) c).getLinkedException() != null
					&& c.getCause() == null) {
				c = ((jakarta.xml.bind.JAXBException) c).getLinkedException();
			}
			if (c.getMessage() != null) message = c.getMessage();
		}
		return message == null ? t.toString() : message;
	}

	/**
	 * More than one officeDocument relationship (WD REQ-206's NOTE; CR-075 D4): refused, naming the targets, since Word reads
	 * such a package only through a repair, and which part it reads changes with each repaired open in a session. Under
	 * {@link RepairMode#WORD} the last relationship stands, as a fresh Word reads it, and the others go, so a save writes one
	 * main part as Word's does.
	 *
	 * @return the finding, or null for a package with one main part
	 */
	private LoadFinding oneMainPart(RelationshipsPart rp) throws Docx4JException {
		List<Relationship> mains = new ArrayList<Relationship>();
		Relationship external = null;
		for (Relationship r : rp.getRelationships().getRelationship()) {
			if (!PackageRelsUtil.isMainPart(r)) continue;
			if (!"External".equals(r.getTargetMode())) mains.add(r);
			else if (external == null) external = r;
		}
		if (external != null && !mains.isEmpty()) {
			// Word refuses this even through its repair (x-ext-*), so RepairMode.WORD has no reading to give
			throw new Docx4JException("Refused: the package has an External main part relationship, to " + external.getTarget()
					+ ", beside one to " + mains.get(0).getTarget() + ". Word refuses such a package, even through a repair.");
		}
		if (mains.size() < 2) return null;
		if (effectiveRepairMode() != RepairMode.WORD) {
			// counted by relationship: Word repairs two to one target too (round one-target)
			StringBuilder targets = new StringBuilder();
			for (Relationship r : mains) targets.append(targets.length() == 0 ? "" : ", ").append(r.getTarget());
			throw new Docx4JException("Refused: the package has " + mains.size() + " main part relationships, to " + targets
					+ ". Word reads such a package only through a repair; load with RepairMode.WORD to read the last relationship's target ("
					+ RepairMode.PROPERTY + "=word).");
		}
		Relationship kept = mains.get(mains.size() - 1);
		for (Relationship r : mains) {
			if (r != kept) rp.removeRelationship(r);
		}
		String target = kept.getTarget();
		LoadFinding finding = new LoadFinding(LoadFinding.MULTIPLE_MAIN_PARTS, target.startsWith("/") ? target : "/" + target, String.valueOf(mains.size()));
		log.warn(finding.toString());
		return finding;
	}

	/** A relationships part in another namespace: Word reads it only through a repair, so refused unless that was asked for (the round rels-namespace). */
	private void refuseAnotherNamespace(RelationshipsPart rp, String name) throws Docx4JException {
		if (rp.isInAnotherNamespace() && effectiveRepairMode() != RepairMode.WORD) {
			throw new Docx4JException("Refused: " + name + " isn't in the relationships namespace. Word reads such a package only through a repair; "
					+ "load with RepairMode.WORD to read every relationship in it (" + RepairMode.PROPERTY + "=word).");
		}
	}

	private RepairMode effectiveRepairMode() {
		return repairMode != null ? repairMode : RepairMode.fromProperty();
	}

	/** A custom XML store that isn't well formed, kept as bytes (CR-075 D7). */
	private static final class UnreadableStore {
		final Part part;
		final Relationship rel;
		final RelationshipsPart rp;
		UnreadableStore(Part part, Relationship rel, RelationshipsPart rp) {
			this.part = part;
			this.rel = rel;
			this.rp = rp;
		}
	}

	private final List<UnreadableStore> unreadableStores = new ArrayList<UnreadableStore>();

	/**
	 * A store with no JAXB model: as a DOM, or where it isn't well formed, read as absent, its bytes kept, which is no
	 * reason to refuse the package (CR-075 D7). The parse reports to no one: the failure is told once, here.
	 */
	private Part storeOrUnreadable(Part part, InputStream is, ContentTypeManager ctm, String resolvedPartUri, Relationship rel, RelationshipsPart rp)
			throws Exception {
		org.w3c.dom.Document document;
		try {
			javax.xml.parsers.DocumentBuilder builder = XmlUtils.getNewDocumentBuilder(); // this also guards against XXE
			builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
			document = builder.parse(is);
		} catch (org.xml.sax.SAXException | java.io.IOException notWellFormed) {
			log.warn("/" + resolvedPartUri + " could not be read, so it is read as absent: " + deepestMessage(notWellFormed));
			Part kept = getBinaryPart(ctm, resolvedPartUri);
			unreadableStores.add(new UnreadableStore(kept, rel, rp));
			return kept;
		}
		CustomXmlDataStorage data = getCustomXmlDataStorageClass().factory();
		data.setDocument(document); // Not necessarily JAXB, that's just our method name
		((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) part).setData(data);
		return part;
	}

	/**
	 * D7: each store that isn't well formed, read as absent. It is a {@link BinaryPart}, so no store lookup finds it and
	 * an untouched save writes its bytes; its item id is noted, so a binding naming it can say so (WD 0.69 (c)). Under
	 * {@link RepairMode#WORD} it goes, as Word's save drops it.
	 */
	private void passOverUnreadableStores(OpcPackage p) {
		for (UnreadableStore s : unreadableStores) {
			String itemId = null;
			RelationshipsPart own = s.part.getRelationshipsPart();
			Relationship props = own == null ? null : own.getRelationshipByType(Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES);
			Part target = props == null ? null : own.getPart(props);
			if (target instanceof CustomXmlDataStoragePropertiesPart) itemId = ((CustomXmlDataStoragePropertiesPart) target).getItemId();
			if (itemId != null) p.addUnreadableStore(itemId, s.part.getPartName().getName());
			if (effectiveRepairMode() == RepairMode.WORD) {
				for (Relationship r : new ArrayList<Relationship>(s.part.getSourceRelationships())) {
					Object owner = r.getParent() instanceof org.docx4j.relationships.Relationships
							? ((org.docx4j.relationships.Relationships) r.getParent()).getParent() : null;
					(owner instanceof RelationshipsPart ? (RelationshipsPart) owner : s.rp).removeRelationship(r);
				}
				p.getParts().remove(s.part.getPartName());
				if (target != null) p.getParts().remove(target.getPartName());
			}
		}
		unreadableStores.clear();
	}

	/** The package's relationships: on a part that isn't well formed, those before the break, where a main part is among them (CR-075 D6). */
	private LoadFinding populatePackageRels(RelationshipsPart rp) 
			throws Docx4JException {
		
		InputStream is = null;
		try {
			is =  partStore.loadPart( "_rels/.rels");
			if (is==null) {
				throw new Docx4JException("_rels/.rels appears to be missing from this package!");
			}
			rp.unmarshal(is);
			if (rp.getReadFailure() == null) return null;
			int kept;
			try (InputStream again = partStore.loadPart("_rels/.rels")) {
				kept = rp.readRelationshipsBeforeTheBreak(again);
			}
			refuseAnotherNamespace(rp, "/_rels/.rels");
			boolean main = false;
			for (Relationship r : rp.getRelationships().getRelationship()) main |= PackageRelsUtil.isMainPart(r);
			if (!main) throw rp.getReadFailure();
			LoadFinding finding = new LoadFinding(rp.isInAnotherNamespace() ? LoadFinding.RELATIONSHIPS_NAMESPACE : LoadFinding.PART_NOT_READ,
					"/_rels/.rels", String.valueOf(kept));
			log.warn(finding + ": " + deepestMessage(rp.getReadFailure()));
			return finding;
			
		} catch (Docx4JException e) {
			throw e;
		} catch (Exception e) {
			// the part and the parse error, not what the missing relationships then broke (CR-075 D6)
			throw new Docx4JException("The package's relationships part, _rels/.rels, could not be read: " + deepestMessage(e), e);
			
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private RelationshipsPart getRelationshipsPartFromZip(Base p, String partName) 
			throws Docx4JException {
		
		
		RelationshipsPart rp = null;
		
		InputStream is = null;
		try {
			is =  partStore.loadPart( partName);
			if (is==null) {
				return null; // that's ok
			}
			rp = p.getRelationshipsPart(true);
			rp.unmarshal(is);
			if (rp.getReadFailure() != null && loading != null) {
				// the relationships before the break, as Word's repair keeps them (CR-075 D5); what only a lost one named reads as absent
				int kept;
				try (InputStream again = partStore.loadPart(partName)) {
					kept = rp.readRelationshipsBeforeTheBreak(again);
				}
				refuseAnotherNamespace(rp, rp.getPartName().getName());
				LoadFinding finding = new LoadFinding(rp.isInAnotherNamespace() ? LoadFinding.RELATIONSHIPS_NAMESPACE : LoadFinding.PART_NOT_READ,
						rp.getPartName().getName(), String.valueOf(kept));
				log.warn(finding + ": " + deepestMessage(rp.getReadFailure()));
				loading.addLoadFinding(finding);
			}
			
		} catch (Docx4JException e) {
			throw e;
		} catch (Exception e) {
			throw new Docx4JException("Error getting document from Zipped Part:" + partName, e);
			
		} finally {
			IOUtils.closeQuietly(is);
		}
		return rp;
	}
	
		
	/* recursively 
	(i) create new Parts for each thing listed
	in the relationships
	(ii) add the new Part to the package
	(iii) cross the PartName off unusedZipEntries
	*/
	private void addPartsFromRelationships( 
			Base source, RelationshipsPart rp, ContentTypeManager ctm)
		throws Docx4JException {
		
		OpcPackage pkg = source.getPackage();				
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.debug("\n For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget()
					+ ", type: " + r.getType() );
					
				// This is usually the first logged comment for
				// a part, so start with a line break.
			try {				
				getPart(pkg, rp, r, ctm);
			} catch (Docx4JException e) {
				throw e;
			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships", e);
			}
		}
		
		
	}

	/**
	 * Get a Part (except a relationships part), and all its related parts.  
	 * This can be called directly from outside the library, in which case 
	 * the Part will not be owned by a Package until the calling code makes it so.  
	 * 
	 * @param zf
	 * @param source
	 * @param unusedZipEntries
	 * @param pkg
	 * @param r
	 * @param resolvedPartUri
	 * @throws Docx4JException
	 * @throws InvalidFormatException
	 */
	protected void getPart(OpcPackage pkg, RelationshipsPart rp, 
			Relationship r, ContentTypeManager ctm)
			throws Docx4JException, InvalidFormatException, URISyntaxException {
		
		Base source = null;
		String resolvedPartUri = null;
		
		if (r.getType().equals(Namespaces.HYPERLINK)) {
			// Could be Internal or External
			// Example of Internal is w:drawing/wp:inline/wp:docPr/a:hlinkClick
			log.debug("Encountered (but not loading) hyperlink " + r.getTarget()  );				
			return;			
		} else 
			if (r.getTargetMode() == null
				|| !r.getTargetMode().equals("External") ) {
			
			// Usual case
			
			source = rp.getSourceP();
			resolvedPartUri = URIHelper.resolvePartUri(rp.getSourceURI(), new URI(r.getTarget() ) ).toString();		

			// Now drop leading "/'
			resolvedPartUri = resolvedPartUri.substring(1);				

			// Now normalise it .. ie abc/def/../ghi
			// becomes abc/ghi
			// Maybe this isn't necessary with a zip file,
			// - ZipFile class may be smart enough to do it.
			// But it is certainly necessary in the JCR case.
//			resolvedPartUri = (new java.net.URI(resolvedPartUri)).normalize().toString();
//			log.info("Normalised, it is " + resolvedPartUri );				
			
		} else {			
			// EXTERNAL			
			if (loadExternalTargets && 
					r.getType().equals( Namespaces.IMAGE ) ) {
					// It could instead be, for example, of type hyperlink,
					// and we don't want to try to fetch that
				log.debug("Loading external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				BinaryPart bp = ExternalResourceUtils.getExternalResource(r.getTarget());
				pkg.getExternalResources().put(bp.getExternalTarget(), bp);			
			} else {				
				log.debug("Encountered (but not loading) external resource " + r.getTarget() 
						   + " of type " + r.getType() );				
			}						
			return;
		}
		
		
		String relationshipType = r.getType();		
		Part part;
		
		if (pkg.handled.get(resolvedPartUri)!=null) {
			
			if (log.isDebugEnabled()) {
				log.debug("Already encountered " + resolvedPartUri );
			}
			
			// The source Part (or Package) might have a convenience
			// method for this
			part = pkg.getParts().getParts().get(new PartName("/" + resolvedPartUri));
			if (source.setPartShortcut(part, relationshipType ) ) {
				log.debug("Convenience method established from " + source.getPartName() 
						+ " to " + part.getPartName());
			}
			
			// v3.2.1: also note this additional source rel 
			part.getSourceRelationships().add(r);  
			
			return;
		}
		
		part = getRawPart(ctm, resolvedPartUri, r, rp); // will throw exception if null

		// The source Part (or Package) might have a convenience
		// method for this
		if (source.setPartShortcut(part, relationshipType ) ) {
			log.debug("Convenience method established from " + source.getPartName() 
					+ " to " + part.getPartName());
		}

		
		if (part instanceof BinaryPart
				|| part instanceof DefaultXmlPart) {
			// The constructors of other parts should take care of this...
			part.setRelationshipType(relationshipType);
		}
		rp.loadPart(part, r);
		 // That loads a pre-existing target part into the package
		 // (but does not load its contents as such; that is
		 //  done elsewhere).
		
		pkg.handled.put(resolvedPartUri, resolvedPartUri);

		
//		unusedZipEntries.put(resolvedPartUri, new Boolean(false));
		
		RelationshipsPart rrp = getRelationshipsPart(part);
		if (rrp!=null) {
			
			// @since 11.5.9
			if (pkg.isWasStrict()) {
				rrp.importStrict();
			}
			
			// recurse via this parts relationships, if it has any
			addPartsFromRelationships(part, rrp, ctm );
//			String relPart = PartName.getRelationshipsPartName(
//					part.getPartName().getName().substring(1) );
//			unusedZipEntries.put(relPart, new Boolean(false));					
		}
	}

	/**
	 * Get the Relationships Part (if there is one) for a given Part.  
	 * Otherwise return null.
	 * 
	 * @param zf
	 * @param part
	 * @return
	 * @throws InvalidFormatException
	 */
	public RelationshipsPart getRelationshipsPart(Part part)
	throws Docx4JException, InvalidFormatException {
		
		RelationshipsPart rrp = null;
		// recurse via this parts relationships, if it has any
		//String relPart = PartName.getRelationshipsPartName(target);
		String relPart = PartName.getRelationshipsPartName(
				part.getPartName().getName().substring(1) );

		rrp = getRelationshipsPartFromZip(part,  relPart);
		part.setRelationships(rrp);
		
//		if (partStore.partExists(relPart)) {
//		//if (partByteArrays.get(relPart) !=null ) {
//			log.debug("Found relationships " + relPart );
//			rrp = getRelationshipsPartFromZip(part,  relPart);
//			part.setRelationships(rrp);
//		} else {
//			log.debug("No relationships " + relPart );	
//			return null;
//		}
		return rrp;
	}
	
	

	/**
	 * Get a Part (except a relationships part), but not its relationships part
	 * or related parts.  Useful if you need quick access to just this part.
	 * This can be called directly from outside the library, in which case 
	 * the Part will not be owned by a Package until the calling code makes it so.  
	 * @see  To get a Part and all its related parts, and add all to a package, use
	 * getPart.
	 * @param partByteArrays
	 * @param ctm
	 * @param resolvedPartUri
	 * @param rel
	 * @return
	 * @throws Docx4JException including if result is null
	 */
	public Part getRawPart(
			ContentTypeManager ctm, String resolvedPartUri, Relationship rel, RelationshipsPart rp /* for error logging */)	
			throws Docx4JException {
		
		Part part = null;
		
		InputStream is = null;
		try {
			try {
				log.debug("resolved uri: " + resolvedPartUri);
				
				// Get a subclass of Part appropriate for this content type	
				// This will throw UnrecognisedPartException in the absence of
				// specific knowledge. Hence it is important to get the InputStream
				// first, as we do above.
				part = ctm.getPart("/" + resolvedPartUri, rel);				

				if (log.isDebugEnabled() && part !=null) {
					log.debug("ctm returned " + part.getClass().getName() );
				}
				
				if (part instanceof org.docx4j.openpackaging.parts.ThemePart
						|| part instanceof org.docx4j.openpackaging.parts.DocPropsCorePart
						|| part instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart
						|| part instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart
						|| part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart
						|| part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {
					
					// Nothing to do here
					
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) {
					
					log.debug("Detected BinaryPart " + part.getClass().getName() );
					
					// Note that this is done lazily, since the below lines are commented out
					
//					is = partStore.loadPart( resolvedPartUri);
//					((BinaryPart)part).setBinaryData(is);

				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart ) {
					// ContentTypeManager initially detects them as CustomXmlDataStoragePart;
					// the below changes as necessary 
					
					// Is it a part we know?
					is = partStore.loadPart( resolvedPartUri);
					try {
						
				        XMLInputFactory xif = org.docx4j.utils.XmlParserLimits.apply(XMLInputFactory.newInstance());
				        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
				        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
				        XMLStreamReader xsr = xif.createXMLStreamReader(is);									
						
						Unmarshaller u = Context.jc.createUnmarshaller();
						Object o = u.unmarshal( xsr );						
						log.debug(o.getClass().getName());
						
						PartName name = part.getPartName();
						if (o instanceof CoverPageProperties) {
							
							part = new DocPropsCoverPagePart(name);	
							((DocPropsCoverPagePart)part).setJaxbElement(
									(CoverPageProperties)o);
							
						} else 						
						if (o instanceof org.opendope.conditions.Conditions) {
							
							part = new ConditionsPart(name);
							((ConditionsPart)part).setJaxbElement(
									(org.opendope.conditions.Conditions)o);
							
							
						} else if (o instanceof org.opendope.xpaths.Xpaths) {
							
							part = new XPathsPart(name);
							((XPathsPart)part).setJaxbElement(
									(org.opendope.xpaths.Xpaths)o);
														
						} else if (o instanceof org.opendope.questions.Questionnaire) {
							
							part = new QuestionsPart(name);
							((QuestionsPart)part).setJaxbElement(
									(org.opendope.questions.Questionnaire)o);
							
						} else if (o instanceof org.opendope.answers.Answers) {
							
							part = new StandardisedAnswersPart(name);
							((StandardisedAnswersPart)part).setJaxbElement(
									(org.opendope.answers.Answers)o);
							
						} else if (o instanceof org.opendope.components.Components) {
							
							part = new ComponentsPart(name);
							((ComponentsPart)part).setJaxbElement(
									(org.opendope.components.Components)o);

						} else if (o instanceof JAXBElement<?> 
								&& XmlUtils.unwrap(o) instanceof org.docx4j.bibliography.CTSources) {
							part = new BibliographyPart(name);
							((BibliographyPart)part).setJaxbElement(
									(JAXBElement<org.docx4j.bibliography.CTSources>)o);
														
						} else {
							
							log.error("TODO: handle known CustomXmlPart part  " + o.getClass().getName());

							CustomXmlDataStorage data = getCustomXmlDataStorageClass().factory();					
							is.reset();
							data.setDocument(is); // Not necessarily JAXB, that's just our method name
							((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).setData(data);						
							
						}
						
					} catch (jakarta.xml.bind.UnmarshalException ue) {

						log.info("No JAXB model for this CustomXmlDataStorage part; " + ue.getMessage()  );
						is.reset();
						part = storeOrUnreadable(part, is, ctm, resolvedPartUri, rel, rp);
					} catch (javax.xml.stream.XMLStreamException notEvenStarted) {
						// a declaration the reader refuses, an encoding it doesn't know: the parse below says
						log.info("Couldn't read this CustomXmlDataStorage part as a stream; " + notEvenStarted.getMessage());
						is.reset();
						part = storeOrUnreadable(part, is, ctm, resolvedPartUri, rel, rp);
					}					

				} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart ) {

					is = partStore.loadPart( resolvedPartUri);
					
//					try {
						((XmlPart)part).setDocument(is);
						
					// Experimental 22/6/2011; don't fall back to binary (which we used to) 
						
//					} catch (Docx4JException d) {
//						// This isn't an XML part after all,
//						// even though ContentTypeManager detected it as such
//						// So get it as a binary part
//						part = getBinaryPart(partByteArrays, ctm, resolvedPartUri);
//						log.warn("Could not parse as XML, so using BinaryPart for " 
//								+ resolvedPartUri);						
//						((BinaryPart)part).setBinaryData(is);
//					}
					
				} else {
					// Generally shouldn't happen, since ContentTypeManagerImpl's
					// newPartForContentType should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + resolvedPartUri);
					part = null;					
				}
			
			} catch (PartUnrecognisedException e) {
				log.error("PartUnrecognisedException shouldn't happen anymore!", e);
				// Try to get it as a binary part
				part = getBinaryPart(ctm, resolvedPartUri);
				log.warn("Using BinaryPart for " + resolvedPartUri);
				
//				is = partStore.loadPart( resolvedPartUri);
//				((BinaryPart)part).setBinaryData(is);
			}
		} catch (Exception ex) {
			// IOException, URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);			
			
		} finally {
			IOUtils.closeQuietly(is);
		}
		
        if (part == null) {
            throw new Docx4JException("For source " + rp.getSourceP().getPartName() +  ", cannot find part " + resolvedPartUri + " from rel "+ rel.getId() + "=" + rel.getTarget());
        }
		
		return part;
	}
	
	public Part getBinaryPart(
			ContentTypeManager ctm, String resolvedPartUri)	
			throws Docx4JException {

		Part part = null;
		InputStream is = null;					
		try {
			is = partStore.loadPart(resolvedPartUri);
			//in = partByteArrays.get(resolvedPartUri).getInputStream();
			part = new BinaryPart( new PartName("/" + resolvedPartUri));
			
			// Set content type
			part.setContentType(
					new ContentType(
							ctm.getContentType(new PartName("/" + resolvedPartUri)) ) );
			
			((BinaryPart)part).setBinaryData(is);
			log.info("Stored as BinaryData" );
			
		} catch (Exception ioe) {
			ioe.printStackTrace() ;
		} finally {
			IOUtils.closeQuietly(is);
		}
		return part;
	}



//	// Testing
//	public static void main(String[] args) throws Exception {
//		String filepath = System.getProperty("user.dir") + "/sample-docs/word/FontEmbedded.docx";
//		log.info("Path: " + filepath );
//		ZipPartStore partLoader = new ZipPartStore(new File(filepath));
//		Load3 loader = new Load3(partLoader);
//		loader.get();		
//	}
	
}
