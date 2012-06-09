/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.wml;

import java.math.BigInteger;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BigIntegerAdapter extends XmlAdapter<String,BigInteger> {

        @Override
        public String marshal(BigInteger bigDecimal) throws Exception {
                if (bigDecimal != null){
                        return bigDecimal.toString();
                }
                else {
                        return null;
                }
        }

        @Override
        public BigInteger unmarshal(String s) throws Exception {
                try {
                        return new BigInteger(s);
                } catch (NumberFormatException e) {
                        return null;
                }
        }
}
