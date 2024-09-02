package org.docx4j.model.listnumbering;

public class NumberFormatChineseUpper extends NumberFormatChineseAbstract {
    NumberFormatChineseUpper() {
        CHINESE_NUMBER_CHARACTERS = new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        CHINESE_NUMBER_UNITS = new String[]{"", "拾", "佰", "仟"};
        CHINESE_NUMBER_BIG_UNITS = new String[]{"", "万", "亿"};
    }
}
