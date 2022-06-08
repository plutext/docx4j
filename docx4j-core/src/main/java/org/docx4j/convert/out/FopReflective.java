package org.docx4j.convert.out;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FopReflective {
	
	public static void invokeFORendererApacheFOP(FOSettings settings) 
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, 
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		/*
		 *  FopFactoryBuilder fopFactoryBuilder = FORendererApacheFOP.getFopFactoryBuilder(foSettings) ;
		 */
		Class foRendererApacheFOPClass = Class.forName("org.docx4j.convert.out.fo.renderers.FORendererApacheFOP");
		Method method = foRendererApacheFOPClass.getMethod("getFopFactoryBuilder", FOSettings.class );

		Object fopFactoryBuilder = method.invoke(null, settings);
		
		/*
		 * FopFactory fopFactory = fopFactoryBuilder.build();
		 */
		Class fopFactoryBuilderClass = Class.forName("org.apache.fop.apps.FopFactoryBuilder");
		method = fopFactoryBuilderClass.getDeclaredMethod("build", new Class[0] );						
		Object fopFactory = method.invoke(fopFactoryBuilder);
		
		/*
		 * FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, fopFactory); 			
		 */
		Method getFOUserAgent = foRendererApacheFOPClass.getMethod("getFOUserAgent", FOSettings.class, 
				Class.forName("org.apache.fop.apps.FopFactory") );
		getFOUserAgent.invoke(null, settings, fopFactory);
		
	}

}
