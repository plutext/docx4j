package org.docx4j.samples.graph_convert;

import org.plutext.msgraph.convert.AuthConfig;

/**
 * For what to do here, please see https://medium.com/medialesson/convert-files-to-pdf-using-microsoft-graph-azure-functions-20bc84d2adc4 
 * 
 * The following may also help:

- https://docs.microsoft.com/en-us/graph/tutorials/java
- https://docs.microsoft.com/en-us/graph/auth-register-app-v2

 * @author jharrop
 *
 */
public class AuthConfigImpl implements  AuthConfig {
		
	/**
	 * Application (client) ID
	 */
	public String apiKey() {
		return "d57a3b70-b8a7-47d5-98db-191b86bc01f5";
	}

	/**
	 * Client secret
	 */
	public String apiSecret() {
		return "M2X-B7aS_pjg2x2Ze5-19SE30yMAR_.2bL";
	}

	/**
	 * Directory (tenant) ID
	 */
	public String tenant() {
		return "7e1d462f-b880-4b8b-8578-60caf8f5abc0";
	}

	@Override
	public String site() {
		return "yoursite.sharepoint.com,uuid1,uuid2";
	}


}

