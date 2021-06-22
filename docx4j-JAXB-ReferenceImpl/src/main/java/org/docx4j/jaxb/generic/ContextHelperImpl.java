package org.docx4j.jaxb.generic;

import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.docx4j.jaxb.generic.ContextHelper.ContextFactory;

public class ContextHelperImpl extends ContextFactory {

  @Override
  JAXBContext createContext(String contextPath, ClassLoader classLoader,
      Map<String, Object> properties) throws JAXBException {
    return JAXBContext.newInstance(contextPath, classLoader, properties);
  }
}
