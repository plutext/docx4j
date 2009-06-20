
package org.eclipse.compare;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.internal.LCSSettings;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;

/**
 * A comparator that compares strings.
 */
public class StringComparator implements IRangeComparator {

    private List<String> leafs = new ArrayList<String>();

    public StringComparator(String paragraphContent) {
    	
    	String[] result = paragraphContent.split("\\s");
        for (int x=0; x<result.length; x++)    
        	leafs.add(result[x]);
    	
    }

    public int getRangeCount() {
        return leafs.size();
    }

    public boolean rangesEqual(int owni, IRangeComparator otherComp, int otheri) {
        StringComparator other;
        try {
            other = (StringComparator) otherComp;
        } catch (ClassCastException e) {
            return false;
        }

        return getLeaf(owni).equals(other.getLeaf(otheri));
    }

    public String getLeaf(int owni) {
        return leafs.get(owni);
    }

    public boolean skipRangeComparison(int arg0, int arg1, IRangeComparator arg2) {
        return false;
    }

    public double getMatchRatio(StringComparator other) {
        LCSSettings settings = new LCSSettings();
        settings.setUseGreedyMethod(true);
        settings.setPowLimit(1.5);
        settings.setTooLong(150 * 150);

        RangeDifference[] differences = RangeDifferencer.findDifferences(
                settings, other, this);
        int distanceOther = 0;
        for (RangeDifference d : differences) {
            distanceOther += d.leftLength();
        }

        int distanceThis = 0;
        for (RangeDifference d : differences) {
            distanceThis += d.rightLength();
        }

        return ((0.0 + distanceOther) / other.getRangeCount() + (0.0 + distanceThis)
                / getRangeCount()) / 2;
    }
}
