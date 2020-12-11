/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.toc.switches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSwitch implements SwitchInterface {
	
	private static Logger log = LoggerFactory.getLogger(AbstractSwitch.class);

    public static final String EMPTY = "";
    
    private static final String BACKSLASH_QUOTE_REGEX = "\\\\\"";
    private static final String BACKSLASHES_REGEX = "\\\\\\\\";
    private static final String BACKSLASHES = "\\\\";
    private static final String NUMBERS_REGEX = "[0-9]+";
    private static final String QUOTE = "\"";
    
    public static final String ERROR_NOT_VALID_HEADING_LEVEL = "Error! Not a valid heading level range.";
    
    String tSwitchSeparator = ",";

    String fieldArgument;

    int startLevel = -1;
    int endLevel = -1;

    Map<String, Integer> styleLevelMap = null;

    public String parseFieldArgument(String fieldArgument){
        this.fieldArgument = fieldArgument;
        return EMPTY;
    }

    public boolean isStyleSwitch(){
        return false;
    }

    public int getStartLevel(){
        if(startLevel == -1){
            if(fieldArgument != null){
                parseStartEndLevel();
            }
        }
        return startLevel;
    }

    public int getEndLevel(){
        return endLevel;
    }

    private void parseStartEndLevel(){
        String field = prepareArgument(fieldArgument);
        if(field.isEmpty()){
            return;
        }
        
        List<Integer> levels = new ArrayList<Integer>();
        Pattern p = Pattern.compile(NUMBERS_REGEX);
        Matcher m = p.matcher(field);
        while (m.find()) {
            int n = Integer.parseInt(m.group());
            levels.add(n);
        }
        
        if(levels.size() != 2){
            return;
        }
        
        startLevel = levels.get(0);
        endLevel = levels.get(1);
    }

    public Map<String, Integer> getStyleLevelMap() {
        if(styleLevelMap == null){
            styleLevelMap = new HashMap<String, Integer>();
        } else {
            return styleLevelMap;
        }
        if(fieldArgument == null){
            return styleLevelMap;
        }

        String field = prepareArgument(fieldArgument);
        if(field.isEmpty()){
            return styleLevelMap;
        }
        
        // a set of comma-separated doublets, 
        // with each doublet being a comma-separated set of style name and table of content level
        String[] styleLevels = field.split(tSwitchSeparator);
        int level = -1;
        for(int i = 0; i < styleLevels.length; i++){
            if(i + 1 < styleLevels.length){
                try{
                    level = Integer.parseInt(styleLevels[i + 1].trim());
                } catch(NumberFormatException ex){
                	log.error("TOC \t switch has invalid doublet containing '" + styleLevels[i + 1] + "'");
                    //next is probably style too, so just put with level 1
                    styleLevelMap.put(styleLevels[i].trim(), 1);
                    continue;
                }
            } else {
                styleLevelMap.put(styleLevels[i].trim(), 1);
                break;
            }
            if(level < 1 || level > 9){
                level = 1;
            }
            styleLevelMap.put(styleLevels[i].trim(), level);
        	log.debug("Added " + styleLevels[i] );
            i++;
        }

        return styleLevelMap;
    }
    
    /**
     * Rules applied: 
     * 1. if argument has quote and this quote is not the beginning of string - return empty
     * 2. if argument starts with quote but has no ending quote - return empty
     * @param fieldArgument
     * @return empty string in case field argument can not be parsed correctly
     */
    private String prepareArgument(String fieldArgument){
    	
        String tmp = fieldArgument;
        
        int firstQuote = fieldArgument.indexOf(QUOTE);
        int lastQuote = fieldArgument.lastIndexOf(QUOTE);
        
        if(firstQuote == 0){
            //check last quote: index is positive, it is not first quote index
            if(lastQuote > 0 && lastQuote != firstQuote){
                tmp = fieldArgument.substring(1, lastQuote);
            } else{
                return EMPTY;
            }
        } else if(firstQuote > 0){
            return EMPTY;
        }
        
        tmp = tmp.replaceAll(BACKSLASH_QUOTE_REGEX, QUOTE);
        tmp = tmp.replaceAll(BACKSLASHES_REGEX, BACKSLASHES);

    	log.debug(fieldArgument + " -->  " + tmp );
                
        return tmp;
    }
}
