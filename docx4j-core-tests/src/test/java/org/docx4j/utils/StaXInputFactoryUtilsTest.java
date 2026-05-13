package org.docx4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.XMLConstants;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class StaXInputFactoryUtilsTest {

	@Test
	public void accessExternalDtdWarningDoesNotIncludeStackTrace() throws Exception {
		resetAccessExternalDtdWarning();

		Logger logger = (Logger)LoggerFactory.getLogger(StaXInputFactoryUtils.class);
		Level previousLevel = logger.getLevel();
		boolean previousAdditive = logger.isAdditive();
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		logger.setLevel(Level.DEBUG);
		logger.setAdditive(false);

		try {
			invokeTryToSetStaxProperty(new UnsupportedPropertyXmlInputFactory(), XMLConstants.ACCESS_EXTERNAL_DTD, "");

			assertEquals(2, appender.list.size());
			assertEquals(Level.WARN, appender.list.get(0).getLevel());
			assertNull(appender.list.get(0).getThrowableProxy());
			assertEquals(Level.DEBUG, appender.list.get(1).getLevel());
			assertNotNull(appender.list.get(1).getThrowableProxy());
		} finally {
			logger.detachAppender(appender);
			logger.setLevel(previousLevel);
			logger.setAdditive(previousAdditive);
			resetAccessExternalDtdWarning();
		}
	}

	private static void resetAccessExternalDtdWarning() throws Exception {
		Field field = StaXInputFactoryUtils.class.getDeclaredField("HAS_WARNED_ACCESS_EXTERNAL_DTD");
		field.setAccessible(true);
		((AtomicBoolean)field.get(null)).set(false);
	}

	private static void invokeTryToSetStaxProperty(XMLInputFactory factory, String key, String value) throws Exception {
		Method method = StaXInputFactoryUtils.class.getDeclaredMethod(
				"tryToSetStaxProperty", XMLInputFactory.class, String.class, String.class);
		method.setAccessible(true);
		method.invoke(null, factory, key, value);
	}

	private static class UnsupportedPropertyXmlInputFactory extends XMLInputFactory {

		@Override
		public void setProperty(String name, Object value) {
			throw new IllegalArgumentException("Unrecognized property '" + name + "'");
		}

		@Override
		public Object getProperty(String name) {
			return null;
		}

		@Override
		public boolean isPropertySupported(String name) {
			return false;
		}

		@Override
		public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(InputStream stream, String encoding) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createXMLEventReader(String systemId, InputStream stream) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public XMLResolver getXMLResolver() {
			return null;
		}

		@Override
		public void setXMLResolver(XMLResolver resolver) {
		}

		@Override
		public XMLReporter getXMLReporter() {
			return null;
		}

		@Override
		public void setXMLReporter(XMLReporter reporter) {
		}

		@Override
		public void setEventAllocator(XMLEventAllocator allocator) {
		}

		@Override
		public XMLEventAllocator getEventAllocator() {
			return null;
		}
	}
}
