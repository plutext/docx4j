package org.docx4j.dml.chart;

import java.util.List;

/**
 * Get the list of Series from a generic graphic.
 */
public interface ListSer<T extends ListSer<T>> {

    public List<T> getSer();
}
