package org.docx4j.model.listnumbering;

public class NumberFormatChineseLower extends NumberFormatChineseAbstract {
    NumberFormatChineseLower() {
        CHINESE_NUMBER_CHARACTERS = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        CHINESE_NUMBER_UNITS = new String[]{"", "十", "百", "千"};
        CHINESE_NUMBER_BIG_UNITS = new String[]{"", "万", "亿"};
    }
}
