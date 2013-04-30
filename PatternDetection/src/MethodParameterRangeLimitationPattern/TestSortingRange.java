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
		Range interval2= new Range(Integer.class, new Integer(6), new Integer(8));
		Range interval3= new Range(Integer.class, new Integer(3), new Integer(4));
		
		Range emptyRange  = new Range(Integer.class,1, true, 0, true);
		
		Range interval4  = new Range(Integer.class,0, true, 1, false);
		Range interval5  = new Range(Integer.class,5, true, 6, false);
		Range interval6  = new Range(Integer.class,10, true, 12, true);
		
		System.out.println("emptyRange "+emptyRange.toString() );
		
		
		dlr.add(interval1);
		dlr.add(interval2);
		dlr.add(interval3);
		dlr.add(emptyRange );
		dlr.add(interval4);
		dlr.add(interval5);
		dlr.add(interval6);
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
	
		
		Range rangeToAdd = new Range(Integer.class, 4,true, 6,false);
		
		System.out.println("max"+rangeToAdd.getMaxValue());
		dlr.addAndMaintainDisjunction(rangeToAdd);
		
		
		
		System.out.println("add "+rangeToAdd.toString());
		System.out.println("result ");
		
		DisjointedRangeList sorteddlr = dlr.sort();
		
		
		
		Iterator<Range> RIth3= sorteddlr.iterator();
		
		while (RIth3.hasNext()) {
			Range range = (Range) RIth3.next();
			System.out.println(range.toString());
			
			
		}
		
		System.out.println("eliminateDisjointionPoint");	
		DisjointedRangeList joineddlr = dlr.eliminateDisjointionPoint();
		
		
		
		Iterator<Range> RIth4= joineddlr.iterator();
		
		while (RIth4.hasNext()) {
			Range range = (Range) RIth4.next();
			System.out.println(range.toString());
			
			
		}
		
		
		
	}

}
