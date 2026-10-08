
package org.docx4j.org.etsi.uri.x01903.v13;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.org.etsi.uri.x01903.v13 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AttributeRevocationValues_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "AttributeRevocationValues");
    private final static QName _SignedSignatureProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignedSignatureProperties");
    private final static QName _DataObjectFormat_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "DataObjectFormat");
    private final static QName _SPURI_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SPURI");
    private final static QName _ReferenceInfo_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "ReferenceInfo");
    private final static QName _CertificateValues_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "CertificateValues");
    private final static QName _RevocationValues_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "RevocationValues");
    private final static QName _CommitmentTypeIndication_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "CommitmentTypeIndication");
    private final static QName _QualifyingProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "QualifyingProperties");
    private final static QName _CompleteCertificateRefs_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "CompleteCertificateRefs");
    private final static QName _CounterSignature_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "CounterSignature");
    private final static QName _SignatureTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignatureTimeStamp");
    private final static QName _AttributeRevocationRefs_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "AttributeRevocationRefs");
    private final static QName _SigAndRefsTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SigAndRefsTimeStamp");
    private final static QName _SignedProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignedProperties");
    private final static QName _SigningTime_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SigningTime");
    private final static QName _RefsOnlyTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "RefsOnlyTimeStamp");
    private final static QName _ObjectIdentifier_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "ObjectIdentifier");
    private final static QName _AttrAuthoritiesCertValues_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "AttrAuthoritiesCertValues");
    private final static QName _SignedDataObjectProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignedDataObjectProperties");
    private final static QName _UnsignedDataObjectProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "UnsignedDataObjectProperties");
    private final static QName _SignerRole_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignerRole");
    private final static QName _Any_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "Any");
    private final static QName _AllDataObjectsTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "AllDataObjectsTimeStamp");
    private final static QName _SignaturePolicyIdentifier_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignaturePolicyIdentifier");
    private final static QName _SPUserNotice_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SPUserNotice");
    private final static QName _EncapsulatedPKIData_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "EncapsulatedPKIData");
    private final static QName _XAdESTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "XAdESTimeStamp");
    private final static QName _IndividualDataObjectsTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "IndividualDataObjectsTimeStamp");
    private final static QName _UnsignedProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "UnsignedProperties");
    private final static QName _SignatureProductionPlace_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SignatureProductionPlace");
    private final static QName _AttributeCertificateRefs_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "AttributeCertificateRefs");
    private final static QName _ArchiveTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "ArchiveTimeStamp");
    private final static QName _UnsignedSignatureProperties_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "UnsignedSignatureProperties");
    private final static QName _CompleteRevocationRefs_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "CompleteRevocationRefs");
    private final static QName _SigningCertificate_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "SigningCertificate");
    private final static QName _OtherTimeStamp_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "OtherTimeStamp");
    private final static QName _Include_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "Include");
    private final static QName _QualifyingPropertiesReference_QNAME = new QName("http://uri.etsi.org/01903/v1.3.2#", "QualifyingPropertiesReference");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.org.etsi.uri.x01903.v13
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link XAdESTimeStampType }
     * 
     */
    public XAdESTimeStampType createXAdESTimeStampType() {
        return new XAdESTimeStampType();
    }

    /**
     * Create an instance of {@link SignedDataObjectPropertiesType }
     * 
     */
    public SignedDataObjectPropertiesType createSignedDataObjectPropertiesType() {
        return new SignedDataObjectPropertiesType();
    }

    /**
     * Create an instance of {@link OtherTimeStampType }
     * 
     */
    public OtherTimeStampType createOtherTimeStampType() {
        return new OtherTimeStampType();
    }

    /**
     * Create an instance of {@link CertIDListType }
     * 
     */
    public CertIDListType createCertIDListType() {
        return new CertIDListType();
    }

    /**
     * Create an instance of {@link UnsignedSignaturePropertiesType }
     * 
     */
    public UnsignedSignaturePropertiesType createUnsignedSignaturePropertiesType() {
        return new UnsignedSignaturePropertiesType();
    }

    /**
     * Create an instance of {@link CompleteRevocationRefsType }
     * 
     */
    public CompleteRevocationRefsType createCompleteRevocationRefsType() {
        return new CompleteRevocationRefsType();
    }

    /**
     * Create an instance of {@link IncludeType }
     * 
     */
    public IncludeType createIncludeType() {
        return new IncludeType();
    }

    /**
     * Create an instance of {@link QualifyingPropertiesReferenceType }
     * 
     */
    public QualifyingPropertiesReferenceType createQualifyingPropertiesReferenceType() {
        return new QualifyingPropertiesReferenceType();
    }

    /**
     * Create an instance of {@link AnyType }
     * 
     */
    public AnyType createAnyType() {
        return new AnyType();
    }

    /**
     * Create an instance of {@link UnsignedDataObjectPropertiesType }
     * 
     */
    public UnsignedDataObjectPropertiesType createUnsignedDataObjectPropertiesType() {
        return new UnsignedDataObjectPropertiesType();
    }

    /**
     * Create an instance of {@link SignerRoleType }
     * 
     */
    public SignerRoleType createSignerRoleType() {
        return new SignerRoleType();
    }

    /**
     * Create an instance of {@link SignedPropertiesType }
     * 
     */
    public SignedPropertiesType createSignedPropertiesType() {
        return new SignedPropertiesType();
    }

    /**
     * Create an instance of {@link CompleteCertificateRefsType }
     * 
     */
    public CompleteCertificateRefsType createCompleteCertificateRefsType() {
        return new CompleteCertificateRefsType();
    }

    /**
     * Create an instance of {@link CertificateValuesType }
     * 
     */
    public CertificateValuesType createCertificateValuesType() {
        return new CertificateValuesType();
    }

    /**
     * Create an instance of {@link ObjectIdentifierType }
     * 
     */
    public ObjectIdentifierType createObjectIdentifierType() {
        return new ObjectIdentifierType();
    }

    /**
     * Create an instance of {@link RevocationValuesType }
     * 
     */
    public RevocationValuesType createRevocationValuesType() {
        return new RevocationValuesType();
    }

    /**
     * Create an instance of {@link QualifyingPropertiesType }
     * 
     */
    public QualifyingPropertiesType createQualifyingPropertiesType() {
        return new QualifyingPropertiesType();
    }

    /**
     * Create an instance of {@link CommitmentTypeIndicationType }
     * 
     */
    public CommitmentTypeIndicationType createCommitmentTypeIndicationType() {
        return new CommitmentTypeIndicationType();
    }

    /**
     * Create an instance of {@link CounterSignatureType }
     * 
     */
    public CounterSignatureType createCounterSignatureType() {
        return new CounterSignatureType();
    }

    /**
     * Create an instance of {@link SignatureProductionPlaceType }
     * 
     */
    public SignatureProductionPlaceType createSignatureProductionPlaceType() {
        return new SignatureProductionPlaceType();
    }

    /**
     * Create an instance of {@link UnsignedPropertiesType }
     * 
     */
    public UnsignedPropertiesType createUnsignedPropertiesType() {
        return new UnsignedPropertiesType();
    }

    /**
     * Create an instance of {@link SignedSignaturePropertiesType }
     * 
     */
    public SignedSignaturePropertiesType createSignedSignaturePropertiesType() {
        return new SignedSignaturePropertiesType();
    }

    /**
     * Create an instance of {@link EncapsulatedPKIDataType }
     * 
     */
    public EncapsulatedPKIDataType createEncapsulatedPKIDataType() {
        return new EncapsulatedPKIDataType();
    }

    /**
     * Create an instance of {@link SPUserNoticeType }
     * 
     */
    public SPUserNoticeType createSPUserNoticeType() {
        return new SPUserNoticeType();
    }

    /**
     * Create an instance of {@link DataObjectFormatType }
     * 
     */
    public DataObjectFormatType createDataObjectFormatType() {
        return new DataObjectFormatType();
    }

    /**
     * Create an instance of {@link SignaturePolicyIdentifierType }
     * 
     */
    public SignaturePolicyIdentifierType createSignaturePolicyIdentifierType() {
        return new SignaturePolicyIdentifierType();
    }

    /**
     * Create an instance of {@link ReferenceInfoType }
     * 
     */
    public ReferenceInfoType createReferenceInfoType() {
        return new ReferenceInfoType();
    }

    /**
     * Create an instance of {@link DigestAlgAndValueType }
     * 
     */
    public DigestAlgAndValueType createDigestAlgAndValueType() {
        return new DigestAlgAndValueType();
    }

    /**
     * Create an instance of {@link DocumentationReferencesType }
     * 
     */
    public DocumentationReferencesType createDocumentationReferencesType() {
        return new DocumentationReferencesType();
    }

    /**
     * Create an instance of {@link CertifiedRolesListType }
     * 
     */
    public CertifiedRolesListType createCertifiedRolesListType() {
        return new CertifiedRolesListType();
    }

    /**
     * Create an instance of {@link CRLValuesType }
     * 
     */
    public CRLValuesType createCRLValuesType() {
        return new CRLValuesType();
    }

    /**
     * Create an instance of {@link CRLRefType }
     * 
     */
    public CRLRefType createCRLRefType() {
        return new CRLRefType();
    }

    /**
     * Create an instance of {@link IdentifierType }
     * 
     */
    public IdentifierType createIdentifierType() {
        return new IdentifierType();
    }

    /**
     * Create an instance of {@link OCSPRefsType }
     * 
     */
    public OCSPRefsType createOCSPRefsType() {
        return new OCSPRefsType();
    }

    /**
     * Create an instance of {@link OtherCertStatusValuesType }
     * 
     */
    public OtherCertStatusValuesType createOtherCertStatusValuesType() {
        return new OtherCertStatusValuesType();
    }

    /**
     * Create an instance of {@link SignaturePolicyIdType }
     * 
     */
    public SignaturePolicyIdType createSignaturePolicyIdType() {
        return new SignaturePolicyIdType();
    }

    /**
     * Create an instance of {@link SigPolicyQualifiersListType }
     * 
     */
    public SigPolicyQualifiersListType createSigPolicyQualifiersListType() {
        return new SigPolicyQualifiersListType();
    }

    /**
     * Create an instance of {@link CRLIdentifierType }
     * 
     */
    public CRLIdentifierType createCRLIdentifierType() {
        return new CRLIdentifierType();
    }

    /**
     * Create an instance of {@link CRLRefsType }
     * 
     */
    public CRLRefsType createCRLRefsType() {
        return new CRLRefsType();
    }

    /**
     * Create an instance of {@link OCSPRefType }
     * 
     */
    public OCSPRefType createOCSPRefType() {
        return new OCSPRefType();
    }

    /**
     * Create an instance of {@link NoticeReferenceType }
     * 
     */
    public NoticeReferenceType createNoticeReferenceType() {
        return new NoticeReferenceType();
    }

    /**
     * Create an instance of {@link OtherCertStatusRefsType }
     * 
     */
    public OtherCertStatusRefsType createOtherCertStatusRefsType() {
        return new OtherCertStatusRefsType();
    }

    /**
     * Create an instance of {@link ClaimedRolesListType }
     * 
     */
    public ClaimedRolesListType createClaimedRolesListType() {
        return new ClaimedRolesListType();
    }

    /**
     * Create an instance of {@link IntegerListType }
     * 
     */
    public IntegerListType createIntegerListType() {
        return new IntegerListType();
    }

    /**
     * Create an instance of {@link CertIDType }
     * 
     */
    public CertIDType createCertIDType() {
        return new CertIDType();
    }

    /**
     * Create an instance of {@link OCSPIdentifierType }
     * 
     */
    public OCSPIdentifierType createOCSPIdentifierType() {
        return new OCSPIdentifierType();
    }

    /**
     * Create an instance of {@link ResponderIDType }
     * 
     */
    public ResponderIDType createResponderIDType() {
        return new ResponderIDType();
    }

    /**
     * Create an instance of {@link CommitmentTypeQualifiersListType }
     * 
     */
    public CommitmentTypeQualifiersListType createCommitmentTypeQualifiersListType() {
        return new CommitmentTypeQualifiersListType();
    }

    /**
     * Create an instance of {@link OCSPValuesType }
     * 
     */
    public OCSPValuesType createOCSPValuesType() {
        return new OCSPValuesType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RevocationValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttributeRevocationValues")
    public JAXBElement<RevocationValuesType> createAttributeRevocationValues(RevocationValuesType value) {
        return new JAXBElement<RevocationValuesType>(_AttributeRevocationValues_QNAME, RevocationValuesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignedSignaturePropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignedSignatureProperties")
    public JAXBElement<SignedSignaturePropertiesType> createSignedSignatureProperties(SignedSignaturePropertiesType value) {
        return new JAXBElement<SignedSignaturePropertiesType>(_SignedSignatureProperties_QNAME, SignedSignaturePropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataObjectFormatType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "DataObjectFormat")
    public JAXBElement<DataObjectFormatType> createDataObjectFormat(DataObjectFormatType value) {
        return new JAXBElement<DataObjectFormatType>(_DataObjectFormat_QNAME, DataObjectFormatType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SPURI")
    public JAXBElement<String> createSPURI(String value) {
        return new JAXBElement<String>(_SPURI_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "ReferenceInfo")
    public JAXBElement<ReferenceInfoType> createReferenceInfo(ReferenceInfoType value) {
        return new JAXBElement<ReferenceInfoType>(_ReferenceInfo_QNAME, ReferenceInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CertificateValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CertificateValues")
    public JAXBElement<CertificateValuesType> createCertificateValues(CertificateValuesType value) {
        return new JAXBElement<CertificateValuesType>(_CertificateValues_QNAME, CertificateValuesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RevocationValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "RevocationValues")
    public JAXBElement<RevocationValuesType> createRevocationValues(RevocationValuesType value) {
        return new JAXBElement<RevocationValuesType>(_RevocationValues_QNAME, RevocationValuesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CommitmentTypeIndicationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CommitmentTypeIndication")
    public JAXBElement<CommitmentTypeIndicationType> createCommitmentTypeIndication(CommitmentTypeIndicationType value) {
        return new JAXBElement<CommitmentTypeIndicationType>(_CommitmentTypeIndication_QNAME, CommitmentTypeIndicationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QualifyingPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "QualifyingProperties")
    public JAXBElement<QualifyingPropertiesType> createQualifyingProperties(QualifyingPropertiesType value) {
        return new JAXBElement<QualifyingPropertiesType>(_QualifyingProperties_QNAME, QualifyingPropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteCertificateRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CompleteCertificateRefs")
    public JAXBElement<CompleteCertificateRefsType> createCompleteCertificateRefs(CompleteCertificateRefsType value) {
        return new JAXBElement<CompleteCertificateRefsType>(_CompleteCertificateRefs_QNAME, CompleteCertificateRefsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CounterSignatureType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CounterSignature")
    public JAXBElement<CounterSignatureType> createCounterSignature(CounterSignatureType value) {
        return new JAXBElement<CounterSignatureType>(_CounterSignature_QNAME, CounterSignatureType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignatureTimeStamp")
    public JAXBElement<XAdESTimeStampType> createSignatureTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_SignatureTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteRevocationRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttributeRevocationRefs")
    public JAXBElement<CompleteRevocationRefsType> createAttributeRevocationRefs(CompleteRevocationRefsType value) {
        return new JAXBElement<CompleteRevocationRefsType>(_AttributeRevocationRefs_QNAME, CompleteRevocationRefsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SigAndRefsTimeStamp")
    public JAXBElement<XAdESTimeStampType> createSigAndRefsTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_SigAndRefsTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignedPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignedProperties")
    public JAXBElement<SignedPropertiesType> createSignedProperties(SignedPropertiesType value) {
        return new JAXBElement<SignedPropertiesType>(_SignedProperties_QNAME, SignedPropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SigningTime")
    public JAXBElement<XMLGregorianCalendar> createSigningTime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_SigningTime_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "RefsOnlyTimeStamp")
    public JAXBElement<XAdESTimeStampType> createRefsOnlyTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_RefsOnlyTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "ObjectIdentifier")
    public JAXBElement<ObjectIdentifierType> createObjectIdentifier(ObjectIdentifierType value) {
        return new JAXBElement<ObjectIdentifierType>(_ObjectIdentifier_QNAME, ObjectIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CertificateValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttrAuthoritiesCertValues")
    public JAXBElement<CertificateValuesType> createAttrAuthoritiesCertValues(CertificateValuesType value) {
        return new JAXBElement<CertificateValuesType>(_AttrAuthoritiesCertValues_QNAME, CertificateValuesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignedDataObjectPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignedDataObjectProperties")
    public JAXBElement<SignedDataObjectPropertiesType> createSignedDataObjectProperties(SignedDataObjectPropertiesType value) {
        return new JAXBElement<SignedDataObjectPropertiesType>(_SignedDataObjectProperties_QNAME, SignedDataObjectPropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsignedDataObjectPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "UnsignedDataObjectProperties")
    public JAXBElement<UnsignedDataObjectPropertiesType> createUnsignedDataObjectProperties(UnsignedDataObjectPropertiesType value) {
        return new JAXBElement<UnsignedDataObjectPropertiesType>(_UnsignedDataObjectProperties_QNAME, UnsignedDataObjectPropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignerRoleType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignerRole")
    public JAXBElement<SignerRoleType> createSignerRole(SignerRoleType value) {
        return new JAXBElement<SignerRoleType>(_SignerRole_QNAME, SignerRoleType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AnyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "Any")
    public JAXBElement<AnyType> createAny(AnyType value) {
        return new JAXBElement<AnyType>(_Any_QNAME, AnyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AllDataObjectsTimeStamp")
    public JAXBElement<XAdESTimeStampType> createAllDataObjectsTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_AllDataObjectsTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignaturePolicyIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignaturePolicyIdentifier")
    public JAXBElement<SignaturePolicyIdentifierType> createSignaturePolicyIdentifier(SignaturePolicyIdentifierType value) {
        return new JAXBElement<SignaturePolicyIdentifierType>(_SignaturePolicyIdentifier_QNAME, SignaturePolicyIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SPUserNoticeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SPUserNotice")
    public JAXBElement<SPUserNoticeType> createSPUserNotice(SPUserNoticeType value) {
        return new JAXBElement<SPUserNoticeType>(_SPUserNotice_QNAME, SPUserNoticeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EncapsulatedPKIDataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "EncapsulatedPKIData")
    public JAXBElement<EncapsulatedPKIDataType> createEncapsulatedPKIData(EncapsulatedPKIDataType value) {
        return new JAXBElement<EncapsulatedPKIDataType>(_EncapsulatedPKIData_QNAME, EncapsulatedPKIDataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "XAdESTimeStamp")
    public JAXBElement<XAdESTimeStampType> createXAdESTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_XAdESTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "IndividualDataObjectsTimeStamp")
    public JAXBElement<XAdESTimeStampType> createIndividualDataObjectsTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_IndividualDataObjectsTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsignedPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "UnsignedProperties")
    public JAXBElement<UnsignedPropertiesType> createUnsignedProperties(UnsignedPropertiesType value) {
        return new JAXBElement<UnsignedPropertiesType>(_UnsignedProperties_QNAME, UnsignedPropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignatureProductionPlaceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignatureProductionPlace")
    public JAXBElement<SignatureProductionPlaceType> createSignatureProductionPlace(SignatureProductionPlaceType value) {
        return new JAXBElement<SignatureProductionPlaceType>(_SignatureProductionPlace_QNAME, SignatureProductionPlaceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteCertificateRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttributeCertificateRefs")
    public JAXBElement<CompleteCertificateRefsType> createAttributeCertificateRefs(CompleteCertificateRefsType value) {
        return new JAXBElement<CompleteCertificateRefsType>(_AttributeCertificateRefs_QNAME, CompleteCertificateRefsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "ArchiveTimeStamp")
    public JAXBElement<XAdESTimeStampType> createArchiveTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_ArchiveTimeStamp_QNAME, XAdESTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsignedSignaturePropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "UnsignedSignatureProperties")
    public JAXBElement<UnsignedSignaturePropertiesType> createUnsignedSignatureProperties(UnsignedSignaturePropertiesType value) {
        return new JAXBElement<UnsignedSignaturePropertiesType>(_UnsignedSignatureProperties_QNAME, UnsignedSignaturePropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteRevocationRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CompleteRevocationRefs")
    public JAXBElement<CompleteRevocationRefsType> createCompleteRevocationRefs(CompleteRevocationRefsType value) {
        return new JAXBElement<CompleteRevocationRefsType>(_CompleteRevocationRefs_QNAME, CompleteRevocationRefsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CertIDListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SigningCertificate")
    public JAXBElement<CertIDListType> createSigningCertificate(CertIDListType value) {
        return new JAXBElement<CertIDListType>(_SigningCertificate_QNAME, CertIDListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OtherTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "OtherTimeStamp")
    public JAXBElement<OtherTimeStampType> createOtherTimeStamp(OtherTimeStampType value) {
        return new JAXBElement<OtherTimeStampType>(_OtherTimeStamp_QNAME, OtherTimeStampType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IncludeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "Include")
    public JAXBElement<IncludeType> createInclude(IncludeType value) {
        return new JAXBElement<IncludeType>(_Include_QNAME, IncludeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QualifyingPropertiesReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "QualifyingPropertiesReference")
    public JAXBElement<QualifyingPropertiesReferenceType> createQualifyingPropertiesReference(QualifyingPropertiesReferenceType value) {
        return new JAXBElement<QualifyingPropertiesReferenceType>(_QualifyingPropertiesReference_QNAME, QualifyingPropertiesReferenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteRevocationRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttributeRevocationRefs", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CompleteRevocationRefsType> createUnsignedSignaturePropertiesTypeAttributeRevocationRefs(CompleteRevocationRefsType value) {
        return new JAXBElement<CompleteRevocationRefsType>(_AttributeRevocationRefs_QNAME, CompleteRevocationRefsType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SigAndRefsTimeStamp", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<XAdESTimeStampType> createUnsignedSignaturePropertiesTypeSigAndRefsTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_SigAndRefsTimeStamp_QNAME, XAdESTimeStampType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RevocationValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttributeRevocationValues", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<RevocationValuesType> createUnsignedSignaturePropertiesTypeAttributeRevocationValues(RevocationValuesType value) {
        return new JAXBElement<RevocationValuesType>(_AttributeRevocationValues_QNAME, RevocationValuesType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteCertificateRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttributeCertificateRefs", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CompleteCertificateRefsType> createUnsignedSignaturePropertiesTypeAttributeCertificateRefs(CompleteCertificateRefsType value) {
        return new JAXBElement<CompleteCertificateRefsType>(_AttributeCertificateRefs_QNAME, CompleteCertificateRefsType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "RefsOnlyTimeStamp", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<XAdESTimeStampType> createUnsignedSignaturePropertiesTypeRefsOnlyTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_RefsOnlyTimeStamp_QNAME, XAdESTimeStampType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CertificateValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CertificateValues", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CertificateValuesType> createUnsignedSignaturePropertiesTypeCertificateValues(CertificateValuesType value) {
        return new JAXBElement<CertificateValuesType>(_CertificateValues_QNAME, CertificateValuesType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CertificateValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "AttrAuthoritiesCertValues", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CertificateValuesType> createUnsignedSignaturePropertiesTypeAttrAuthoritiesCertValues(CertificateValuesType value) {
        return new JAXBElement<CertificateValuesType>(_AttrAuthoritiesCertValues_QNAME, CertificateValuesType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "ArchiveTimeStamp", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<XAdESTimeStampType> createUnsignedSignaturePropertiesTypeArchiveTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_ArchiveTimeStamp_QNAME, XAdESTimeStampType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RevocationValuesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "RevocationValues", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<RevocationValuesType> createUnsignedSignaturePropertiesTypeRevocationValues(RevocationValuesType value) {
        return new JAXBElement<RevocationValuesType>(_RevocationValues_QNAME, RevocationValuesType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteRevocationRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CompleteRevocationRefs", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CompleteRevocationRefsType> createUnsignedSignaturePropertiesTypeCompleteRevocationRefs(CompleteRevocationRefsType value) {
        return new JAXBElement<CompleteRevocationRefsType>(_CompleteRevocationRefs_QNAME, CompleteRevocationRefsType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteCertificateRefsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CompleteCertificateRefs", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CompleteCertificateRefsType> createUnsignedSignaturePropertiesTypeCompleteCertificateRefs(CompleteCertificateRefsType value) {
        return new JAXBElement<CompleteCertificateRefsType>(_CompleteCertificateRefs_QNAME, CompleteCertificateRefsType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CounterSignatureType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "CounterSignature", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<CounterSignatureType> createUnsignedSignaturePropertiesTypeCounterSignature(CounterSignatureType value) {
        return new JAXBElement<CounterSignatureType>(_CounterSignature_QNAME, CounterSignatureType.class, UnsignedSignaturePropertiesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XAdESTimeStampType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://uri.etsi.org/01903/v1.3.2#", name = "SignatureTimeStamp", scope = UnsignedSignaturePropertiesType.class)
    public JAXBElement<XAdESTimeStampType> createUnsignedSignaturePropertiesTypeSignatureTimeStamp(XAdESTimeStampType value) {
        return new JAXBElement<XAdESTimeStampType>(_SignatureTimeStamp_QNAME, XAdESTimeStampType.class, UnsignedSignaturePropertiesType.class, value);
    }

}
