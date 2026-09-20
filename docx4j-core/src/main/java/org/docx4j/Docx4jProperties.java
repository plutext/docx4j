package org.docx4j;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.utils.ResourceUtils;

public class Docx4jProperties {
	
	protected static Logger log = LoggerFactory.getLogger(Docx4jProperties.class);
	
	private static Properties properties;

	private static void init() {

		properties = new Properties();
		defaultTheme = null;
		try (
				InputStream is = ResourceUtils.getResource("docx4j.properties");
			) {
			properties.load(is);
		} catch (Exception e) {
			log.warn("Couldn't find/read docx4j.properties; " + e.getMessage());
		}
	}

	/**
	 * Which Office theme docx4j supplies where a document asks for a theme font and its
	 * package has <b>no theme part</b>, and which theme part
	 * {@code WordprocessingMLPackage.createPackage} puts in a new package.
	 *
	 * <p>Word supplies its own default theme to such a document, and which faces that names
	 * has changed twice: measured on a Word 365 rendering of a package with no theme part
	 * (CR-001 batch 49), the body face is <b>Aptos</b> and the heading face
	 * <b>Aptos Display</b>, where Office 2013 to 2022 answered Calibri and Calibri Light and
	 * Office 2007 to 2010 answered Calibri and Cambria.  So this is the version of Word a
	 * themeless document should be read as having come from - not a docx4j preference - and
	 * the default is the current one.</p>
	 *
	 * <p>Values: {@code 2023} (the default), {@code 2013}, {@code 2007};
	 * see {@link DefaultTheme}.  Anything else logs a warning and falls back to the default.</p>
	 *
	 * @since 17.2.0
	 */
	public static final String DEFAULT_THEME = "docx4j.fonts.defaultTheme";

	/**
	 * The three answers {@link Docx4jProperties#DEFAULT_THEME} takes, each with the Latin
	 * faces its font scheme names and the theme part docx4j bundles for it.
	 *
	 * @since 17.2.0
	 */
	public enum DefaultTheme {

		/** Word 365's Office theme: Aptos Display / Aptos. The default. */
		THEME_2023("2023", "Aptos Display", "Aptos"),
		/** Office 2013 to 2022: Calibri Light / Calibri. */
		THEME_2013("2013", "Calibri Light", "Calibri"),
		/** Office 2007 to 2010: Cambria / Calibri; docx4j's answer up to 17.1.0. */
		THEME_2007("2007", "Cambria", "Calibri");

		private final String value;
		private final String majorLatin;
		private final String minorLatin;

		private DefaultTheme(String value, String majorLatin, String minorLatin) {
			this.value = value;
			this.majorLatin = majorLatin;
			this.minorLatin = minorLatin;
		}

		/** The property value which selects this theme. */
		public String value() {
			return value;
		}

		/** The face its font scheme names for the major (heading) Latin slot. */
		public String majorLatin() {
			return majorLatin;
		}

		/** The face its font scheme names for the minor (body) Latin slot. */
		public String minorLatin() {
			return minorLatin;
		}

		/** The classpath resource holding this theme's own theme1.xml. */
		public String resource() {
			return "org/docx4j/openpackaging/parts/WordprocessingML/theme-" + value + ".xml";
		}

		/** The theme this property value names, or null where it names none. */
		public static DefaultTheme of(String value) {
			if (value == null) return null;
			String v = value.trim();
			for (DefaultTheme t : values()) {
				if (t.value.equalsIgnoreCase(v)) return t;
			}
			return null;
		}
	}

	/** Read once (see {@link #getDefaultTheme()}); cleared by {@link #setProperty}. */
	private static DefaultTheme defaultTheme;

	/**
	 * {@link #DEFAULT_THEME}, parsed; never null.
	 *
	 * <p>Read once and remembered, because it is asked for per run of a themeless document.
	 * {@code setProperty} clears it, so a unit test can change it.</p>
	 *
	 * @since 17.2.0
	 */
	public static DefaultTheme getDefaultTheme() {

		DefaultTheme t = defaultTheme;
		if (t != null) return t;

		String value = getProperty(DEFAULT_THEME);
		t = DefaultTheme.of(value);
		if (t == null) {
			if (value != null && value.trim().length() > 0) {
				log.warn(DEFAULT_THEME + "=" + value + " is not one of 2023, 2013 or 2007; using "
						+ DefaultTheme.THEME_2023.value());
			}
			t = DefaultTheme.THEME_2023;
		}
		defaultTheme = t;
		return t;
	}
	
	public static String getProperty(String key) {
		
		if (properties==null) {init();}
				
		return properties.getProperty(key);		
	}

	
	/**
	 * @since 2.7.2
	 */
	public static String getProperty(String key, String defaultValue) {
		
		if (properties==null) {init();}
				
		return properties.getProperty(key, defaultValue);		
	}
	
	/**
	 * @since 3.3.0
	 */
	public static boolean getProperty(String key, boolean defaultValue) {
		
		if (properties==null) {init();}
		String result = properties.getProperty(key, Boolean.toString(defaultValue));
		return Boolean.parseBoolean(result);
	}

	public static int getProperty(String key, int defaultValue) {
		
		if (properties==null) {init();}
		String val = properties.getProperty(key);
		if (val==null) return defaultValue;
		
		try {
			return (Integer.parseInt(val));
		} catch (NumberFormatException e) {
			log.info(e.getMessage(),e);
			return defaultValue;
		}
	}
	/**
	 * @since 6.0.0
	 */
	public static long getPropertyLong(String key, long defaultValue) {
		
		if (properties==null) {init();}
		String val = properties.getProperty(key);
		if (val==null) return defaultValue;
		
		try {
			return (Long.parseLong(val));
		} catch (NumberFormatException e) {
			log.info(e.getMessage(),e);
			return defaultValue;
		}
	}

	
	public static Properties getProperties() {
		
		if (properties==null) {init();}
		return properties;		
	}
	
	/**
	 * Useful if a unit test requires a certain property value.
	 * 
	 * @since 3.0.0
	 */
	public static void setProperty(String key, Boolean value) {
		if (properties==null) {init();}
		properties.setProperty(key, value.toString());
		if (DEFAULT_THEME.equals(key)) defaultTheme = null;
	}

	/**
	 * Useful if a unit test requires a certain property value.
	 * 
	 * @since 3.0.0
	 */
	public static void setProperty(String key, String value) {
		if (properties==null) {init();}
		properties.setProperty(key, value);
		if (DEFAULT_THEME.equals(key)) defaultTheme = null;
	}
	/**
	 * @since 6.0.0
	 */
	public static void setPropertyLong(String key, long value) {
		
		if (properties==null) {init();}
		properties.setProperty(key, Long.toString(value));		
	}
	
}
