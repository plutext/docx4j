package org.docx4j.services.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 8.3.1
 */
public class JsonUtil {
	
	private static Logger log = LoggerFactory.getLogger(JsonUtil.class);	
	
    public static Map<String, Integer> bytesToMap(byte[] json) throws Exception {
    	
		/* DocumentServices 2.0-x
		 * {"bookmarks":{"_Toc299924107":"68","_Toc318272803":"1"}}
		 */
    	
		Map<String,Integer> map = new HashMap<String,Integer>();
		ObjectMapper m = new ObjectMapper();
		
		JsonNode rootNode = m.readTree(json);
		JsonNode bookmarksNode = rootNode.path("bookmarks");
		
		Iterator<Map.Entry<String, JsonNode>> bookmarksValueObj = bookmarksNode.fields();
		while (bookmarksValueObj.hasNext()) {
			Map.Entry<String, JsonNode> entry = bookmarksValueObj.next();
			if (entry.getValue()==null) {
				log.warn("Null page number computed for bookmark " + entry.getKey());
			}
			map.put(entry.getKey(), new Integer(entry.getValue().asInt()));
		}		
			
		return map;
    	
    }
	

}
