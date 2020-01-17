
package org.docx4j.w16cid;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CommentsIds complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommentsIds"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="commentId" type="{http://schemas.microsoft.com/office/word/2016/wordml/cid}CT_CommentId" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * @since 3.3.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommentsIds", propOrder = {
    "commentId"
})
@XmlRootElement(name="commentsIds")
public class CTCommentsIds implements Child
{
	
    @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String ignorable;

    protected List<CTCommentId> commentId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the commentId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the commentId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommentId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCommentId }
     * 
     * 
     */
    public List<CTCommentId> getCommentId() {
        if (commentId == null) {
            commentId = new ArrayList<CTCommentId>();
        }
        return this.commentId;
    }

    /**
     * Gets the value of the ignorable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIgnorable() {
        return ignorable;
    }

    /**
     * Sets the value of the ignorable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIgnorable(String value) {
        this.ignorable = value;
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
