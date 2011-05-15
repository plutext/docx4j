
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
 * <p>Java class for CT_AuthorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AuthorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="Artist" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Author" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameOrCorporateType"/>
 *           &lt;element name="BookAuthor" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Compiler" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Composer" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Conductor" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Counsel" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Director" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Editor" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Interviewee" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Interviewer" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Inventor" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Performer" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameOrCorporateType"/>
 *           &lt;element name="ProducerName" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Translator" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
 *           &lt;element name="Writer" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameType"/>
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
@XmlType(name = "CT_AuthorType", propOrder = {
    "artistOrAuthorOrBookAuthor"
})
public class CTAuthorType {

    @XmlElementRefs({
        @XmlElementRef(name = "Counsel", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Director", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Composer", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Performer", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Conductor", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "BookAuthor", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Interviewer", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Editor", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "ProducerName", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Translator", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Author", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Artist", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Interviewee", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Compiler", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Writer", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class),
        @XmlElementRef(name = "Inventor", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/bibliography", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> artistOrAuthorOrBookAuthor;

    /**
     * Gets the value of the artistOrAuthorOrBookAuthor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the artistOrAuthorOrBookAuthor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArtistOrAuthorOrBookAuthor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameOrCorporateType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameOrCorporateType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNameType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getArtistOrAuthorOrBookAuthor() {
        if (artistOrAuthorOrBookAuthor == null) {
            artistOrAuthorOrBookAuthor = new ArrayList<JAXBElement<?>>();
        }
        return this.artistOrAuthorOrBookAuthor;
    }

}
