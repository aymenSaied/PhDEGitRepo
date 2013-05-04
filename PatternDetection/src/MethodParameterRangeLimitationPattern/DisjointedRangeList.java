package MethodParameterRangeLimitationPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.util.ArraySet;

public class DisjointedRangeList extends ArrayList<Range> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5609692440160425495L;

	public void addAndMaintainDisjunction(Range rangeToAdd) {

		/*
		 * TODO normalement cette methode permet de ajouter un range à une liste
		 * de range masi dois verifier si cette elemet intersecct avec un des
		 * ranges dans le set si oui alor remplacer ce dernier par la reunion
		 * des deux et conserver lordre des range et leur disjunction dans le
		 * set
		 */

		Range resultingRange = rangeToAdd;
	
		Boolean weFindIntersection = false;

		Iterator<Range> rangeIterator = this.iterator();

		while ((rangeIterator.hasNext()) && (!weFindIntersection)) {
			Range range = (Range) rangeIterator.next();

			if ((rangeToAdd.intersects(range))||(range.isEmpty())||(rangeToAdd.isEmpty())) {

				weFindIntersection = true;
				resultingRange = rangeToAdd.union(range);
				this.remove(range);
				this.addAndMaintainDisjunction(resultingRange);

			}

		}

		if (!weFindIntersection) {

			this.add(rangeToAdd);
		}

	}

	public DisjointedRangeList union(DisjointedRangeList rangeList) {

		DisjointedRangeList resultingDisjointedRangeList = new DisjointedRangeList();

		if (this.size() == 0 && rangeList.size() == 0) {
			try {
				throw new Exception("range list size is 0");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			
			Iterator<Range>	rangeIterator1 =this.iterator();

			while (rangeIterator1.hasNext()) {
				Range range = (Range) rangeIterator1.next();
				
				resultingDisjointedRangeList.addAndMaintainDisjunction(range);
				
				
			}
			
			
			Iterator<Range>	rangeIterator2 =rangeList.iterator();
			while (rangeIterator2.hasNext()) {
				Range range = (Range) rangeIterator2.next();
				
				resultingDisjointedRangeList.addAndMaintainDisjunction(range);
				
			}
			

		}

		return resultingDisjointedRangeList;

	}
	
	
	public DisjointedRangeList sort(){
		
		DisjointedRangeList sortedDisjointedRangeList = new DisjointedRangeList();
		
		Collections.sort(this);
		
		Iterator<Range> RIth= this.iterator();
		
		while (RIth.hasNext()) {
			Range range = (Range) RIth.next();
			
			sortedDisjointedRangeList.add(range);
			
		}
		
		return sortedDisjointedRangeList;
	
		
	}
	
	/**
	 * forme I3 à partir de I1 union I2
	 * si I1.maxvalue = I2.minvalue 
	 * cad in point ou un interval et fermer et lautre et ouvert 
	 * si les 2 interval sont ouvert en ce point ne pas faire la reunoin 
	 * */
	
	public DisjointedRangeList eliminateDisjointionPoint(){
		
		DisjointedRangeList joinedRangeList = new DisjointedRangeList();

		
		if (this.size()>1) {
			
			
			
			DisjointedRangeList sortedDisjointedRangeList = this.sort();

			boolean isTheFirstPass=true;
			boolean endwithAddedresultingRange=true;
			Range tempRange = null;
			Range resultingRange =sortedDisjointedRangeList.get(0);
			
			Iterator<Range> RIt= sortedDisjointedRangeList.iterator();

			while (RIt.hasNext()) {
				Range range = (Range) RIt.next();
			
				if (isTheFirstPass) {
					
					isTheFirstPass=false;
					resultingRange=range;
				}else {
					
					 tempRange =range;
					
					
					if ((resultingRange.getMaxValue().equals(tempRange.getMinValue()))&&(resultingRange.isMaxIncluded()||tempRange.isMinIncluded())) {
						
						/*dans ce cas les 2  intervalles consecutive ont
						 * une borne commune 
						 * et au moin un des intervale est inclusif ou fermé du coté de cet borne 
						 */
						
						
						
						resultingRange=resultingRange.union(tempRange);
						endwithAddedresultingRange=false;
						
					}else {
						/*dans ce cas les 2 intervalle sont disjoint 
						 * on ajoute l'intervalle deja construit à joinedRangeList 
						 *  et on commence la construction d'un nouveau intervalle 
						 */
						
						joinedRangeList.add(resultingRange);
						resultingRange=tempRange;
						
						endwithAddedresultingRange=true;
					}
					
					
				}
				
				
			}
			
			
			if (tempRange==null) {
				
				try {
					throw new Exception("tempRange is not suposed to be null");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}else {
			
				
				if(endwithAddedresultingRange){
					
					joinedRangeList.add(tempRange);

					
				}else {
					
					joinedRangeList.add(resultingRange);

					
				}			
		
				
				
				
			}
			
					
			
			
			
			
			
			
			
			
			
			
			
		}else {
			
			try {
				throw new Exception("DisjointedRangeList size must be >1 to perform eliminateDisjointionPoint operation");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return joinedRangeList;

		
	}

	
	
	
}
