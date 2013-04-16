/* NullnessAnalysis
 * Copyright (C) 2006 Eric Bodden
 * Copyright (C) 2007 Julian Tibble
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package MethodParameterRangeLimitationPattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Immediate;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.ClassConstant;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.NumericConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JCmpgExpr;
import soot.jimple.internal.JCmplExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceOfExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JNeExpr;

import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.ArraySet;
import soot.util.Chain;

public class RangeLimitationWithOneRangeAnalysis extends
		ForwardBranchedFlowAnalysis {

	// protected LocalTypeSet emptySet;
	protected HashMap<Local, Range> emptySet;

	private HashMap<Local,Integer> localToNbOverestimatedUnion;

	private List<String> consideredType;

	/**
	 * The analysis info is a simple mapping of type {@link Value} to any of the
	 * constants BOTTOM, TYPE_IS_NOT_RESTRICTED, TYPE_IS_RESTRICTED or TOP. This
	 * class returns BOTTOM by default.
	 * 
	 * 
	 */
	protected class AnalysisInfo {

		HashMap<Local, Range> ParamToRangeLimitation;

		public AnalysisInfo() {

			ParamToRangeLimitation = emptySet;
		}

		public AnalysisInfo(AnalysisInfo other) {

			ParamToRangeLimitation = other.ParamToRangeLimitation;

		}

		/*
		 * public int get(Value key) { if (!valueToIndex.containsKey(key))
		 * return BOTTOM;
		 * 
		 * int index = valueToIndex.get(key); int result = get(index) ? 2 : 0;
		 * result += get(index + 1) ? 1 : 0;
		 * 
		 * return result; }
		 * 
		 * public void put(Value key, int val) { int index; if
		 * (!valueToIndex.containsKey(key)) { index = used; used += 2;
		 * valueToIndex.put(key, index); } else { index = valueToIndex.get(key);
		 * } set(index, (val & 2) == 2); set(index + 1, (val & 1) == 1); }
		 */

		public void put(Local key, Range val) {

			ParamToRangeLimitation.put(key, val);

		}

		public Range get(Local key) {

			return ParamToRangeLimitation.get(key);

		}
	}

	/**
	 * Creates a new analysis for the given graph/
	 * 
	 * @param graph
	 *            any unit graph
	 */
	public RangeLimitationWithOneRangeAnalysis(UnitGraph graph) {
		super(graph);
		
		consideredType = new ArrayList<String>();
		consideredType.add("int");
		consideredType.add("byte");
		consideredType.add("short");

		makeInitialSetForRangeLimitation();
		doAnalysis();
	}

	protected void copy(Object source, Object dest) {
		AnalysisInfo s = (AnalysisInfo) source;
		AnalysisInfo d = (AnalysisInfo) dest;

		d.ParamToRangeLimitation = s.ParamToRangeLimitation;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object entryInitialFlow() {

		return new AnalysisInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void merge(Object in1, Object in2, Object out) {

		AnalysisInfo outflow = (AnalysisInfo) out;

		Set<Local> ParamSet = outflow.ParamToRangeLimitation.keySet();

		Iterator<Local> paramIth = ParamSet.iterator();

		while (paramIth.hasNext()) {
			Local local = (Local) paramIth.next();

			Range resultingUnionRange = ((AnalysisInfo) in1).ParamToRangeLimitation
					.get(local).union(
							((AnalysisInfo) in2).ParamToRangeLimitation
									.get(local));

			outflow.ParamToRangeLimitation.put(local, resultingUnionRange);

			/*
			 * we log the nember of overestimated union
			 * 
			 * if range doesn't intersect it is an overestimation
			 */

			if (!((AnalysisInfo) in1).ParamToRangeLimitation.get(local)
					.intersects(
							((AnalysisInfo) in2).ParamToRangeLimitation
									.get(local))) {

				
				
				localToNbOverestimatedUnion.put(local,localToNbOverestimatedUnion.get(local)+1);

			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {

		return new AnalysisInfo();
	}

	/**
	 * Returns <code>true</code> if the analysis could determine that the range
	 * of param i is Alread LimitedB before the statement s.
	 * 
	 * @param s
	 *            a statement of the respective body
	 * @param i
	 *            a method param that body
	 */

	public boolean isAlreadyRangeLimitedBefore(Unit s, Local i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);

		// if the given range dosent contains the initialRange then it is
		// AlreadyRangeLimited

		Boolean alreadyRangeLimited = !ai.get(i).contains(
				initialRange(i.getType()));
		return alreadyRangeLimited;
	}

	/**
	 * Returns <code>true</code> if the analysis could determine that i is
	 * always non-null before the statement s.
	 * 
	 * @param s
	 *            a statement of the respective body
	 * @param i
	 *            a local of that body
	 * @return true if i is always non-null right before this statement
	 */
	public boolean isStillRangeNonLimitedBefore(Unit s, Local i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);

		// if the given range contains the initialRange then it is
		// StillRangeNonLimited

		Boolean StillRangeNonLimited = ai.get(i).contains(
				initialRange(i.getType()));
		return StillRangeNonLimited;
	}

	/**
	 * 
	 * return null if isAlwaysRestrictedBefore(s,i) return false on dois
	 * verifier que RestrictionTypesList.size() != 0
	 * 
	 * */

	
	
	
	public List<Range> getRangeLimitationBefore(Unit s, Local i) {
	  
	  List<Range> rangeLimitationList = new ArrayList<Range>();
	 
	  if (isAlreadyRangeLimitedBefore(s, i)) {
	  
	  AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);
	  
	  /*
	   * TODO   normalement si on migre vrs la version ou chaque
	   * param pointe vers une list de Range 
	   * alors dans  rangeLimitationList on doit mettre la liste des range qui normalement sont disjoint par construction
	   * 
	   * */
	  rangeLimitationList.add(ai.get(i)) ;
	  
	  }
	  
		if (isAlreadyRangeLimitedBefore(s, i)&&rangeLimitationList.size() == 0) {

			try {
				throw new Exception(
						"rangeLimitationList.size() is not suposed to be 0 ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	  
	  return rangeLimitationList;

	}
	  

	  /*  */
	  
	  
	// ///////////////////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected void flowThrough(Object flowin, Unit u, List fallOut,
			List branchOuts) {
		AnalysisInfo in = (AnalysisInfo) flowin;
		AnalysisInfo out = new AnalysisInfo(in);
		AnalysisInfo outBranch = new AnalysisInfo(in);

		/*
		 * le kil set et le paramaitre sur le quelle porte une condition
		 * comparative
		 */

		Stmt s = (Stmt) u;

		if (s instanceof JIfStmt) {
			JIfStmt ifStmt = (JIfStmt) s;

			handleIfStmt(ifStmt, in, out, outBranch);
		}

		// now copy the computed info to all successors
		for (Iterator it = fallOut.iterator(); it.hasNext();) {
			copy(out, it.next());
		}
		for (Iterator it = branchOuts.iterator(); it.hasNext();) {
			copy(outBranch, it.next());
		}
	}

	private void handleIfStmt(JIfStmt ifStmt, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {

		ConditionExpr Condtionexpr = (ConditionExpr) ifStmt.getCondition();

		Boolean conditionOnNumericParameter = false;
		Local param;

		if (Condtionexpr.getOp1() instanceof Local
				&& in.ParamToRangeLimitation.containsKey(Condtionexpr.getOp1())) {

			conditionOnNumericParameter = true;
			param = (Local) Condtionexpr.getOp1();

		} else if (Condtionexpr.getOp2() instanceof Local
				&& in.ParamToRangeLimitation.containsKey(Condtionexpr.getOp2())) {

			conditionOnNumericParameter = true;
			param = (Local) Condtionexpr.getOp2();
		}

		if (conditionOnNumericParameter) {

			if (Condtionexpr instanceof JEqExpr) {
				JEqExpr equalExpr = (JEqExpr) Condtionexpr;
				handleEqualityCheck(equalExpr, in, out, outBranch);

			} else if (Condtionexpr instanceof JNeExpr) {

				JNeExpr neExpr = (JNeExpr) Condtionexpr;
				handleNotEqualCheck(neExpr, in, out, outBranch);

			} else if (Condtionexpr instanceof JCmplExpr) {

				JCmplExpr lessThanExpr = (JCmplExpr) Condtionexpr;
				handleLessThanCheck(lessThanExpr, in, out, outBranch);

			} else if (Condtionexpr instanceof JCmpgExpr) {

				JCmpgExpr greaterThanExpr = (JCmpgExpr) Condtionexpr;
				handleGreaterThanCheck(greaterThanExpr, in, out, outBranch);

			} else if (Condtionexpr instanceof JGeExpr) {

				JGeExpr greaterThanOrEqualToExpr = (JGeExpr) Condtionexpr;
				handleGreaterThanOrEqualToCheck(greaterThanOrEqualToExpr, in,
						out, outBranch);

			} else if (Condtionexpr instanceof JLeExpr) {

				JLeExpr lessThanOrEqualToExpr = (JLeExpr) Condtionexpr;
				handleLessThanOrEqualToCheck(lessThanOrEqualToExpr, in, out,
						outBranch);

			}

		}

	}

	private void handleEqualityCheck(JEqExpr eqExpr, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {

		Boolean comparingLocalAndNumericConstant = false;
		Local param;
		NumericConstant comparaisonCanstant;
		if (eqExpr.getOp1() instanceof Local
				&& eqExpr.getOp2() instanceof NumericConstant) {
			param = (Local) eqExpr.getOp1();
			comparaisonCanstant = (NumericConstant) eqExpr.getOp2();
			comparingLocalAndNumericConstant = true;

		} else if (eqExpr.getOp1() instanceof Local
				&& eqExpr.getOp2() instanceof NumericConstant) {

			param = (Local) eqExpr.getOp2();
			comparaisonCanstant = (NumericConstant) eqExpr.getOp1();
			comparingLocalAndNumericConstant = true;
		} else {
			param = null;
			comparaisonCanstant = null;
			comparingLocalAndNumericConstant = false;

		}

		/*
		 * we dont perform kil because the generation in it self perform the kil
		 * it it doesn't add new Range but it modify existing one
		 */

		if (comparingLocalAndNumericConstant) {

			Range oldRange = in.ParamToRangeLimitation.get(param);

			if (oldRange.contains(transformToComparable(comparaisonCanstant))) {

				/* case 1 comparaisonCanstant in the considered Range */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						transformToComparable(comparaisonCanstant), true,
						transformToComparable(comparaisonCanstant), true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMaxValue())
					.compareTo(transformToComparable(comparaisonCanstant)) < 0) {

				/*
				 * case 2 comparaisonCanstant on the left of the considered
				 * Range
				 */

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						1, true, 0, true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMinValue())
					.compareTo(transformToComparable(comparaisonCanstant)) > 0) {

				/*
				 * case 3 comparaisonCanstant on the right of the considered
				 * Range
				 */

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						1, true, 0, true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			}

		}

	}

	private void handleLessThanCheck(JCmplExpr lessThanExpr, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {

		Boolean comparingLocalAndNumericConstant = false;
		Local param;
		NumericConstant comparaisonCanstant;
		if (lessThanExpr.getOp1() instanceof Local
				&& lessThanExpr.getOp2() instanceof NumericConstant) {
			param = (Local) lessThanExpr.getOp1();
			comparaisonCanstant = (NumericConstant) lessThanExpr.getOp2();
			comparingLocalAndNumericConstant = true;

		} else if (lessThanExpr.getOp1() instanceof Local
				&& lessThanExpr.getOp2() instanceof NumericConstant) {

			param = (Local) lessThanExpr.getOp2();
			comparaisonCanstant = (NumericConstant) lessThanExpr.getOp1();
			comparingLocalAndNumericConstant = true;
		} else {
			param = null;
			comparaisonCanstant = null;
			comparingLocalAndNumericConstant = false;

		}

		/*
		 * we dont perform kil because the generation in it self perform the kil
		 * it it doesn't add new Range but it modify existing one
		 */

		if (comparingLocalAndNumericConstant) {

			Range oldRange = in.ParamToRangeLimitation.get(param);

			if (oldRange.contains(transformToComparable(comparaisonCanstant))) {

				/* case 1 comparaisonCanstant in the considered Range */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						oldRange.getMinValue(), oldRange.isMinIncluded(),
						transformToComparable(comparaisonCanstant), false);

				Range newOutRAnge = new Range(oldRange.getElementClass(),
						transformToComparable(comparaisonCanstant), true,
						oldRange.getMaxValue(), oldRange.isMaxIncluded());

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMaxValue())
					.compareTo(transformToComparable(comparaisonCanstant)) <= 0) {

				/*
				 * case 2 comparaisonCanstant on the left of the considered
				 * Range
				 */

				Range newOutBranchRange = oldRange;

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutRAnge = new Range(oldRange.getElementClass(), 1,
						true, 0, true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMinValue())
					.compareTo(transformToComparable(comparaisonCanstant)) >= 0) {

				/*
				 * case 3 comparaisonCanstant on the right of the considered
				 * Range
				 */

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						1, true, 0, true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			}

		}

	}

	private void handleGreaterThanCheck(JCmpgExpr greaterThanExpr,
			AnalysisInfo in, AnalysisInfo out, AnalysisInfo outBranch) {

		Boolean comparingLocalAndNumericConstant = false;
		Local param;
		NumericConstant comparaisonCanstant;
		if (greaterThanExpr.getOp1() instanceof Local
				&& greaterThanExpr.getOp2() instanceof NumericConstant) {
			param = (Local) greaterThanExpr.getOp1();
			comparaisonCanstant = (NumericConstant) greaterThanExpr.getOp2();
			comparingLocalAndNumericConstant = true;

		} else if (greaterThanExpr.getOp1() instanceof Local
				&& greaterThanExpr.getOp2() instanceof NumericConstant) {

			param = (Local) greaterThanExpr.getOp2();
			comparaisonCanstant = (NumericConstant) greaterThanExpr.getOp1();
			comparingLocalAndNumericConstant = true;
		} else {
			param = null;
			comparaisonCanstant = null;
			comparingLocalAndNumericConstant = false;

		}

		/*
		 * we dont perform kil because the generation in it self perform the kil
		 * it it doesn't add new Range but it modify existing one
		 */

		if (comparingLocalAndNumericConstant) {

			Range oldRange = in.ParamToRangeLimitation.get(param);

			if (oldRange.contains(transformToComparable(comparaisonCanstant))) {

				/* case 1 comparaisonCanstant in the considered Range */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						transformToComparable(comparaisonCanstant), false,
						oldRange.getMaxValue(), oldRange.isMaxIncluded());

				Range newOutRAnge = new Range(oldRange.getElementClass(),
						oldRange.getMinValue(), oldRange.isMinIncluded(),
						transformToComparable(comparaisonCanstant), true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMaxValue())
					.compareTo(transformToComparable(comparaisonCanstant)) <= 0) {

				/*
				 * case 2 comparaisonCanstant on the left of the considered
				 * Range
				 */

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						1, true, 0, true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMinValue())
					.compareTo(transformToComparable(comparaisonCanstant)) >= 0) {

				/*
				 * case 3 comparaisonCanstant on the right of the considered
				 * Range
				 */

				Range newOutBranchRange = oldRange;

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutRAnge = new Range(oldRange.getElementClass(), 1,
						true, 0, true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			}

		}

	}

	private void handleGreaterThanOrEqualToCheck(
			JGeExpr greaterThanOrEqualToExpr, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {

		Boolean comparingLocalAndNumericConstant = false;
		Local param;
		NumericConstant comparaisonCanstant;
		if (greaterThanOrEqualToExpr.getOp1() instanceof Local
				&& greaterThanOrEqualToExpr.getOp2() instanceof NumericConstant) {
			param = (Local) greaterThanOrEqualToExpr.getOp1();
			comparaisonCanstant = (NumericConstant) greaterThanOrEqualToExpr
					.getOp2();
			comparingLocalAndNumericConstant = true;

		} else if (greaterThanOrEqualToExpr.getOp1() instanceof Local
				&& greaterThanOrEqualToExpr.getOp2() instanceof NumericConstant) {

			param = (Local) greaterThanOrEqualToExpr.getOp2();
			comparaisonCanstant = (NumericConstant) greaterThanOrEqualToExpr
					.getOp1();
			comparingLocalAndNumericConstant = true;
		} else {
			param = null;
			comparaisonCanstant = null;
			comparingLocalAndNumericConstant = false;

		}

		/*
		 * we dont perform kil because the generation in it self perform the kil
		 * it it doesn't add new Range but it modify existing one
		 */

		if (comparingLocalAndNumericConstant) {

			Range oldRange = in.ParamToRangeLimitation.get(param);

			if (oldRange.contains(transformToComparable(comparaisonCanstant))) {

				/* case 1 comparaisonCanstant in the considered Range */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						transformToComparable(comparaisonCanstant), true,
						oldRange.getMaxValue(), oldRange.isMaxIncluded());

				Range newOutRAnge = new Range(oldRange.getElementClass(),
						oldRange.getMinValue(), oldRange.isMinIncluded(),
						transformToComparable(comparaisonCanstant), false);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMaxValue())
					.compareTo(transformToComparable(comparaisonCanstant)) <= 0) {

				/*
				 * case 2 comparaisonCanstant on the left of the considered
				 * Range
				 */

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						1, true, 0, true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMinValue())
					.compareTo(transformToComparable(comparaisonCanstant)) >= 0) {

				/*
				 * case 3 comparaisonCanstant on the right of the considered
				 * Range
				 */

				Range newOutBranchRange = oldRange;

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutRAnge = new Range(oldRange.getElementClass(), 1,
						true, 0, true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			}

		}

	}

	private void handleLessThanOrEqualToCheck(JLeExpr lessThanOrEqualToExpr,
			AnalysisInfo in, AnalysisInfo out, AnalysisInfo outBranch) {

		Boolean comparingLocalAndNumericConstant = false;
		Local param;
		NumericConstant comparaisonCanstant;
		if (lessThanOrEqualToExpr.getOp1() instanceof Local
				&& lessThanOrEqualToExpr.getOp2() instanceof NumericConstant) {
			param = (Local) lessThanOrEqualToExpr.getOp1();
			comparaisonCanstant = (NumericConstant) lessThanOrEqualToExpr
					.getOp2();
			comparingLocalAndNumericConstant = true;

		} else if (lessThanOrEqualToExpr.getOp1() instanceof Local
				&& lessThanOrEqualToExpr.getOp2() instanceof NumericConstant) {

			param = (Local) lessThanOrEqualToExpr.getOp2();
			comparaisonCanstant = (NumericConstant) lessThanOrEqualToExpr
					.getOp1();
			comparingLocalAndNumericConstant = true;
		} else {
			param = null;
			comparaisonCanstant = null;
			comparingLocalAndNumericConstant = false;

		}

		/*
		 * we dont perform kil because the generation in it self perform the kil
		 * it it doesn't add new Range but it modify existing one
		 */

		if (comparingLocalAndNumericConstant) {

			Range oldRange = in.ParamToRangeLimitation.get(param);

			if (oldRange.contains(transformToComparable(comparaisonCanstant))) {

				/* case 1 comparaisonCanstant in the considered Range */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						oldRange.getMinValue(), oldRange.isMinIncluded(),
						transformToComparable(comparaisonCanstant), true);

				Range newOutRAnge = new Range(oldRange.getElementClass(),
						transformToComparable(comparaisonCanstant), false,
						oldRange.getMaxValue(), oldRange.isMaxIncluded());

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMaxValue())
					.compareTo(transformToComparable(comparaisonCanstant)) <= 0) {

				/*
				 * case 2 comparaisonCanstant on the left of the considered
				 * Range
				 */

				Range newOutBranchRange = oldRange;

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutRAnge = new Range(oldRange.getElementClass(), 1,
						true, 0, true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMinValue())
					.compareTo(transformToComparable(comparaisonCanstant)) >= 0) {

				/*
				 * case 3 comparaisonCanstant on the right of the considered
				 * Range
				 */

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */

				Range newOutBranchRange = new Range(oldRange.getElementClass(),
						1, true, 0, true);

				Range newOutRAnge = oldRange;

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			}

		}

	}

	private void handleNotEqualCheck(JNeExpr neExpr, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {

		Boolean comparingLocalAndNumericConstant = false;
		Local param;
		NumericConstant comparaisonCanstant;
		if (neExpr.getOp1() instanceof Local
				&& neExpr.getOp2() instanceof NumericConstant) {
			param = (Local) neExpr.getOp1();
			comparaisonCanstant = (NumericConstant) neExpr.getOp2();
			comparingLocalAndNumericConstant = true;

		} else if (neExpr.getOp1() instanceof Local
				&& neExpr.getOp2() instanceof NumericConstant) {

			param = (Local) neExpr.getOp2();
			comparaisonCanstant = (NumericConstant) neExpr.getOp1();
			comparingLocalAndNumericConstant = true;
		} else {
			param = null;
			comparaisonCanstant = null;
			comparingLocalAndNumericConstant = false;

		}

		/*
		 * we dont perform kil because the generation in it self perform the kil
		 * it it doesn't add new Range but it modify existing one
		 */

		if (comparingLocalAndNumericConstant) {

			Range oldRange = in.ParamToRangeLimitation.get(param);

			if (oldRange.contains(transformToComparable(comparaisonCanstant))) {

				/* case 1 comparaisonCanstant in the considered Range */

				Range newOutBranchRange = oldRange;

				Range newOutRAnge = new Range(oldRange.getElementClass(),
						transformToComparable(comparaisonCanstant), true,
						transformToComparable(comparaisonCanstant), true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMaxValue())
					.compareTo(transformToComparable(comparaisonCanstant)) < 0) {

				/*
				 * case 2 comparaisonCanstant on the left of the considered
				 * Range
				 */

				Range newOutBranchRange = oldRange;

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutRAnge = new Range(oldRange.getElementClass(), 1,
						true, 0, true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			} else if ((oldRange.getMinValue())
					.compareTo(transformToComparable(comparaisonCanstant)) > 0) {

				/*
				 * case 3 comparaisonCanstant on the right of the considered
				 * Range
				 */

				Range newOutBranchRange = oldRange;

				/*
				 * the range [1,0] is an is a special value that indicates the
				 * empty Range minimum value is greater than it's maximum
				 */
				Range newOutRAnge = new Range(oldRange.getElementClass(), 1,
						true, 0, true);

				outBranch.put(param, newOutBranchRange);
				out.put(param, newOutRAnge);

			}

		}

	}

	protected void makeInitialSetForRangeLimitation() {
		// Find all parameter of numiric type

		ArrayList<Local> methodParameterChain = new ArrayList<Local>();
		ArrayList<Local> numericParameterChain = new ArrayList<Local>();
		for (int j = 0; j < ((UnitGraph) graph).getBody().getMethod()
				.getParameterCount(); j++) {

			methodParameterChain.add(((UnitGraph) graph).getBody()
					.getParameterLocal(j));

		}

		emptySet = new HashMap<Local, Range>(methodParameterChain.size());
		localToNbOverestimatedUnion=new HashMap<Local, Integer>(methodParameterChain.size());
		for (Local l : methodParameterChain) {

			if (consideredType.contains(l.getType().toString())) {

				numericParameterChain.add(l);
				Range initialRange = initialRange(l.getType());
				emptySet.put(l, initialRange);

				localToNbOverestimatedUnion.put(l, 0);
			}

		}

	}

	public Range basedOnSootTypeRange(Type elementClassType,
			Comparable minValue, boolean isMinIncluded, Comparable maxValue,
			boolean isMaxIncluded) {

		String type = elementClassType.toString();

		switch (type) {
		case "int":
			return new Range(Integer.class, minValue, isMinIncluded, maxValue,
					isMaxIncluded);

		case "byte":
			return new Range(Integer.class, minValue, isMinIncluded, maxValue,
					isMaxIncluded);

		case "short":
			return new Range(Integer.class, minValue, isMinIncluded, maxValue,
					isMaxIncluded);

			// TODO continuer pour le reste des type cosiderer

		default:

			return new Range(Integer.class, minValue, isMinIncluded, maxValue,
					isMaxIncluded);

		}

	}

	public Range initialRange(Type elementClassType) {

		String type = elementClassType.toString();

		switch (type) {
		case "int":
			return new Range(Integer.class, Integer.MIN_VALUE, false,
					Integer.MAX_VALUE, false);

		case "byte":
			return new Range(Integer.class, Integer.MIN_VALUE, false,
					Integer.MAX_VALUE, false);

		case "short":
			return new Range(Integer.class, Integer.MIN_VALUE, false,
					Integer.MAX_VALUE, false);

			// TODO continuer pour le reste des type cosiderer

		default:

			return new Range(Integer.class, Integer.MIN_VALUE, false,
					Integer.MAX_VALUE, false);

		}

	}

	private Comparable transformToComparable(Value element) {

		if (element instanceof NumericConstant) {

			String type = element.getType().toString();

			switch (type) {
			case "int":
				return new Integer(element.toString());
			case "byte":
				return new Integer(element.toString());

			case "short":
				return new Integer(element.toString());
				// TODO continuer pour le reste des type cosiderer

			default:

				return new Integer(element.toString());
			}

		} else {

			throw new IllegalArgumentException(
					"parameter is not a NumericConstant");
		}

	}

	/*
	 * dans getLocalsDefinedUsingParameter on recupaire les variable initialiser
	 * à partir de paramaitre sur un seul niveau si une variable est initialiser
	 * ou definit apartir d'un paramaitre on la recupaire si une variable est
	 * initailaiser à apartir d'une variable qui elle est initaliser d'un
	 * paramitre on ne la recupaire pas on ne considaire que les definition
	 * simple pas d'invocation ni d'expression qui contiennet un paramaitre
	 */

	private Map<Local, Local> getLocalsDefinedUsingParameter(UnitGraph cfg,
			ArrayList<Local> numericParameterChain/*
												 * dois etre
												 * numericParameterChain
												 */) {

		Map<Local, Local> localToParameter = new HashMap<Local, Local>(
				cfg.size() * 2 + 1, 0.7f);

		Iterator<Unit> units = cfg.iterator();

		while (units.hasNext()) {
			Unit unit = (Unit) units.next();

			Stmt stmt = (Stmt) unit;

			if (stmt instanceof DefinitionStmt) {

				DefinitionStmt assignStmt = (DefinitionStmt) stmt;
				Value left = assignStmt.getLeftOp();
				Value right = assignStmt.getRightOp();

				// unbox casted value
				if (right instanceof JCastExpr) {
					JCastExpr castExpr = (JCastExpr) right;
					right = castExpr.getOp();
				}

				if (left instanceof Local) {

					if (right instanceof Local) {

						if (numericParameterChain.contains(right)) {

							localToParameter.put((Local) left, (Local) right);

							System.out.println("*----l'unité --> " + unit
									+ " --definit" + left
									+ " parun parametre----la Value------> "
									+ right);

						}

					}

				}

			}

		}

		// Set<Local> LocalsDefinedUsingParameterList =
		// localToParameter.keySet();

		return localToParameter;
	}

	public Integer getNbOverestimatedUnion(Local l) {
		return localToNbOverestimatedUnion.get(l);
	}

	

	public List<String> getConsideredType() {

		return consideredType;
	}

}
