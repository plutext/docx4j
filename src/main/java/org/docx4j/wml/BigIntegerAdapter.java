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
