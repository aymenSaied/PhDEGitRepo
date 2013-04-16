package MethodParameterRangeLimitationPattern;

import java.util.Collections;
import java.util.Iterator;

import MethodParameterRangeLimitationPattern.Range;

public class TestSortingRange {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		DisjointedRangeList dlr = new DisjointedRangeList();
		
		Range interval1= new Range(Integer.class, new Integer(1), new Integer(2));
		Range interval2= new Range(Integer.class, new Integer(5), new Integer(6));
		Range interval3= new Range(Integer.class, new Integer(3), new Integer(4));
		
		dlr.add(interval1);
		dlr.add(interval2);
		dlr.add(interval3);

		Iterator<Range> RIth= dlr.iterator();
		System.out.println("non sorted ");
		while (RIth.hasNext()) {
			Range range = (Range) RIth.next();
			System.out.println(range.toString());
			
			
		}
		
		Collections.sort(dlr);
		
		Iterator<Range> RIth2= dlr.iterator();
		
		System.out.println("sorted ");
		while (RIth2.hasNext()) {
			Range range = (Range) RIth2.next();
			System.out.println(range.toString());
			
			
		}
		
	}

}
