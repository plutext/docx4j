package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.R.CommentReference;
import org.jvnet.jaxb2_commons.ppp.Child;

public  class CommentFinder extends CallbackImpl {

    List<Child> commentElements = new ArrayList<Child>();

    /**
	 * @return the commentElements
	 */
	public List<Child> getCommentElements() {
		return commentElements;
	}

	@Override
    public List<Object> apply(Object o) {

        if (o instanceof javax.xml.bind.JAXBElement
                && (((JAXBElement)o).getName().getLocalPart().equals("commentReference")
                		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeStart")
                		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeEnd")	                            		
                		)) {
        		System.out.println(((JAXBElement)o).getName().getLocalPart());
                commentElements.add( (Child)XmlUtils.unwrap(o) );
            } else 
        if (o instanceof CommentReference || 
            o instanceof CommentRangeStart || 
            o instanceof CommentRangeEnd) {
    		System.out.println(o.getClass().getName());
            commentElements.add((Child)o);
        }
        return null;
    }

        @Override // to setParent
        public void walkJAXBElements(Object parent) {

            List children = getChildren(parent);
            if (children != null) {

                for (Object o : children) {

                    if (o instanceof javax.xml.bind.JAXBElement
                            && (((JAXBElement)o).getName().getLocalPart().equals("commentReference")
                            		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeStart")
                            		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeEnd")	                            		
                            		)) {
                    	
                    	((Child)((JAXBElement)o).getValue()).setParent(XmlUtils.unwrap(parent));
                    } else {                        
                        o = XmlUtils.unwrap(o);
	                    if (o instanceof Child) {
	                        ((Child)o).setParent(XmlUtils.unwrap(parent));
	                    }
                    }


                    this.apply(o);

                    if (this.shouldTraverse(o)) {
                        walkJAXBElements(o);
                    }

                }
            }
        }           
    }	
