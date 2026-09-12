package org.docx4j.model.listnumbering;

/**
 * The old name of {@link LabelFormatter}, kept for one release so that
 * subclasses outside docx4j still compile; it clashes with
 * {@link org.docx4j.wml.NumberFormat}.
 *
 * @deprecated since 17.1.1, extend {@link LabelFormatter}.  Removal no earlier than 17.2.
 */
@Deprecated
public abstract class NumberFormat extends LabelFormatter {

}
