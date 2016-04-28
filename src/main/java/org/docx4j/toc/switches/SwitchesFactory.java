/*
 *  Copyright 2013, Plutext Pty Ltd.
 *   
 *  This file is part of the Commercial Edition of docx4j,
 *  which is licensed under the Plutext Commercial License (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  
 *  In particular, this source code is CONFIDENTIAL, and you must ensure it
 *  stays that way. 
 *
 *  You may obtain a copy of the License at 
 *
 *      http://www.plutext.com/license/Plutext_Commercial_License.pdf 
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package org.docx4j.toc.switches;

public class SwitchesFactory {

    public static SwitchInterface getSwitch(String id){
        if(id.equals(HSwitch.ID)){
            return new HSwitch();
        } else if(id.equals(NSwitch.ID)){
            return new NSwitch();
        } else if(id.equals(OSwitch.ID)){
            return new OSwitch();
        } else if(id.equals(TSwitch.ID)){
            return new TSwitch();
        } else if(id.equals(USwitch.ID)){
            return new USwitch();
        }
        return null;
    }
}
