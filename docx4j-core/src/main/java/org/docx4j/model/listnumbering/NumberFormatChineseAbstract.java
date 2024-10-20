package org.docx4j.model.listnumbering;

public class NumberFormatChineseAbstract extends NumberFormat {

    String[] CHINESE_NUMBER_CHARACTERS;
    String[] CHINESE_NUMBER_UNITS;
    String[] CHINESE_NUMBER_BIG_UNITS;

    @Override
    public String format(int in) {
        if (in == 0) {
            return CHINESE_NUMBER_CHARACTERS[0];
        }

        StringBuilder chineseStr = new StringBuilder();
        int bigUnitPosition = 0;
        boolean lastIsZero = false;

        while (in > 0) {
            // Process four digits each time
            int part = in % 10000;
            if (part != 0) {
                String partStr = convertFourDigit(part);
                if (bigUnitPosition > 0) {
                    chineseStr.insert(0, CHINESE_NUMBER_BIG_UNITS[bigUnitPosition]);
                }
                chineseStr.insert(0, partStr);
                lastIsZero = false;
            } else if (!lastIsZero && chineseStr.length() > 0) {
                chineseStr.insert(0, CHINESE_NUMBER_CHARACTERS[0]);
                lastIsZero = true;
            }

            in /= 10000;
            bigUnitPosition++;
        }

        return chineseStr.toString();
    }

    /**
     * Convert parts below four digits
     */
    private String convertFourDigit(int num) {
        StringBuilder partStr = new StringBuilder();
        int unitPosition = 0;
        boolean lastIsZero = false;

        while (num > 0) {
            int digit = num % 10;
            if (digit != 0) {
                partStr.insert(0, CHINESE_NUMBER_UNITS[unitPosition]);
                partStr.insert(0, CHINESE_NUMBER_CHARACTERS[digit]);
                lastIsZero = false;
            } else if (!lastIsZero) {
                partStr.insert(0, CHINESE_NUMBER_CHARACTERS[0]);
                lastIsZero = true;
            }
            num /= 10;
            unitPosition++;
        }

        // Remove the last "零"
        if (partStr.charAt(partStr.length() - 1) == '零') {
            partStr.deleteCharAt(partStr.length() - 1);
        }

        return partStr.toString();
    }
}
