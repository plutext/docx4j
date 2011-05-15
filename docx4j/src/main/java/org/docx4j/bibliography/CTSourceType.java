
package org.docx4j.bibliography;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SourceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="AbbreviatedCaseNumber" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="AlbumTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Author" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_AuthorType"/>
 *           &lt;element name="BookTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Broadcaster" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="BroadcastTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="CaseNumber" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="ChapterNumber" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="City" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Comments" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="ConferenceName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="CountryRegion" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Court" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Day" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="DayAccessed" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Department" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Distributor" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Edition" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Guid" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Institution" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="InternetSiteTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Issue" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="JournalName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="LCID" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Lang"/>
 *           &lt;element name="Medium" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Month" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="MonthAccessed" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="NumberVolumes" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Pages" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="PatentNumber" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="PeriodicalTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="ProductionCompany" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="PublicationTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Publisher" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="RecordingNumber" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="RefOrder" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Reporter" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="SourceType" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}ST_SourceType"/>
 *           &lt;element name="ShortTitle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="StandardNumber" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="StateProvince" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Station" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Tag" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Theater" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="ThesisType" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Title" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Type" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="URL" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Version" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Volume" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="Year" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *           &lt;element name="YearAccessed" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SourceType", propOrder = {
    "abbreviatedCaseNumberOrAlbumTitleOrAuthor"
})
public class CTSourceType {

    @XmlElementRefs({
        @XmlElementRef(name = "Volume", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "URL", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Medium", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "AlbumTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "StateProvince", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Issue", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Distributor", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "ThesisType", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Version", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Broadcaster", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Month", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "SourceType", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "BroadcastTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "ConferenceName", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Comments", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Type", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "AbbreviatedCaseNumber", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "CountryRegion", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "ProductionCompany", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Year", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Station", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Title", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Department", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "ShortTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "CaseNumber", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "LCID", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "PeriodicalTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "MonthAccessed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Tag", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Day", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "PublicationTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "JournalName", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Court", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "DayAccessed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Edition", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Guid", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "StandardNumber", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "City", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Reporter", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "RecordingNumber", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "BookTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Institution", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "RefOrder", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "ChapterNumber", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "NumberVolumes", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Pages", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "PatentNumber", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Publisher", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Theater", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "InternetSiteTitle", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Author", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "YearAccessed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> abbreviatedCaseNumberOrAlbumTitleOrAuthor;

    /**
     * Gets the value of the abbreviatedCaseNumberOrAlbumTitleOrAuthor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abbreviatedCaseNumberOrAlbumTitleOrAuthor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbbreviatedCaseNumberOrAlbumTitleOrAuthor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link STSourceType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAuthorType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getAbbreviatedCaseNumberOrAlbumTitleOrAuthor() {
        if (abbreviatedCaseNumberOrAlbumTitleOrAuthor == null) {
            abbreviatedCaseNumberOrAlbumTitleOrAuthor = new ArrayList<JAXBElement<?>>();
        }
        return this.abbreviatedCaseNumberOrAlbumTitleOrAuthor;
    }

}
