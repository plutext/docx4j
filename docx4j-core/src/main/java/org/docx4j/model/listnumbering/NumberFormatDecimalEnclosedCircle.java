package org.docx4j.model.listnumbering;

public class NumberFormatDecimalEnclosedCircle extends NumberFormat {
    String[] DECIMAL_ENCLOSED_CIRCLE_CHARACTERS = {"①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩",
            "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳"};

    @Override
    public String format(int in) {
        if (in <= 0 || in > 20) {
            throw new NumberFormatException("Numbers must be in range 1-20");
        }
        return DECIMAL_ENCLOSED_CIRCLE_CHARACTERS[in - 1];
    }
}
