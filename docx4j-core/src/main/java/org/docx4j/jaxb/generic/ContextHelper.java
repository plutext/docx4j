package org.docx4j.jaxb.generic;

import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Helper to make a lookup for a {@link JAXBContext}
 */
public class ContextHelper {

  public static JAXBContext createContext(String contextPath, ClassLoader loader, Map<String,Object> properties ) throws JAXBException {
    try {
     ContextHelper.ContextFactory factory = (ContextFactory) Class.forName("org.docx4j.jaxb.generic.ContextHelperImpl").newInstance();
      return factory.createContext(contextPath, loader, properties);
    } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
      throw new JAXBException("Include exactly one of the libraries docx4j-JAXB-[Internal|MOXy|ReferenceImpl|Websphere]", e);
    }
  }

  /**
   * Extend this class for a new way to instantiate the context.
   */
  public static abstract class ContextFactory {
    abstract JAXBContext createContext(String contextPath, ClassLoader classLoader,
        Map<String, Object> properties) throws JAXBException;
  }
}
