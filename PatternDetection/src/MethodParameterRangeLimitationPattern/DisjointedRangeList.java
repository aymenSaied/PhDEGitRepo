package MethodParameterRangeLimitationPattern;

import java.util.ArrayList;
import java.util.List;

import soot.util.ArraySet;

public class DisjointedRangeList extends ArrayList<Range> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5609692440160425495L;
	

	
	

	public void addAndMaintainDisjunction(Range rangeToAdd){
		
		/*TODO normalement cette methode permet de ajouter un range
		 * à une liste de range masi dois verifier si cette elemet intersecct avec 
		 * un des ranges dans le set si oui alor remplacer ce dernier par la reunion des deux 
		 * et conserver lordre des range et leur disjunction dans le set
		*/
		
		this.add(rangeToAdd);
		
		
	}




	
	

}
