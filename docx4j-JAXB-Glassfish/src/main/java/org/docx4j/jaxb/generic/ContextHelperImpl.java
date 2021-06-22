package org.docx4j.jaxb.generic;

import com.sun.xml.bind.v2.ContextFactory;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class ContextHelperImpl extends org.docx4j.jaxb.generic.ContextHelper.ContextFactory {


  @Override
  JAXBContext createContext(String contextPath, ClassLoader classLoader,
      Map<String, Object> properties) throws JAXBException {
    return ContextFactory.createContext(contextPath, classLoader, properties);
  }
}
