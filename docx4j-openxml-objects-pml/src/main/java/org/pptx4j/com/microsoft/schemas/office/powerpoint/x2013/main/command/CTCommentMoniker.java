
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CommentMoniker complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommentMoniker"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="authorId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="idx" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_Index" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommentMoniker")
public class CTCommentMoniker implements Child
{

    @XmlAttribute(name = "authorId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long authorId;
    @XmlAttribute(name = "idx", required = true)
    protected long idx;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the authorId property.
     * 
     */
    public long getAuthorId() {
        return authorId;
    }

    /**
     * Sets the value of the authorId property.
     * 
     */
    public void setAuthorId(long value) {
        this.authorId = value;
    }

    /**
     * Gets the value of the idx property.
     * 
     */
    public long getIdx() {
        return idx;
    }

    /**
     * Sets the value of the idx property.
     * 
     */
    public void setIdx(long value) {
        this.idx = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
