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

package MethodParameterTypeRestrictionPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



import soot.Immediate;
import soot.Local;
import soot.RefLikeType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.ClassConstant;
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
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceOfExpr;
import soot.jimple.internal.JNeExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.ArraySet;


/**
 * An intraprocedural TypeRestriction analysis that computes for each location and each value
 * in a method if the value is (before or after that location) definetely TypeIsRestricted,
 * definetely  TypeIsNotRestricted or neither.
 
 *
 * 
 */
public class CopyOfTypeRestrictionAnalysis  extends ForwardBranchedFlowAnalysis
{
	
	
	
	/**
	 * The analysis info is a simple mapping of type {@link Value} to
	 * any of the constants BOTTOM, TYPE_IS_NOT_RESTRICTED, TYPE_IS_RESTRICTED or TOP.
	 * This class returns BOTTOM by default.
	 * 
	 * 
	 */
	protected class AnalysisInfo extends java.util.BitSet
	{
		private HashMap<Value, Set<String>> ValueToRestrictedType ;
	
		
		
		public AnalysisInfo() {
			
			super(used);
			ValueToRestrictedType= new HashMap<Value, Set<String>>();
		}

		public AnalysisInfo(AnalysisInfo other) {
			super(used);
			or(other);
			ValueToRestrictedType= new HashMap<Value, Set<String>>();
			
		}

		public int get(Value key)
		{
			if (!valueToIndex.containsKey(key))
				return BOTTOM;

			int index = valueToIndex.get(key);
			int result = get(index) ? 2 : 0;
			result += get(index + 1) ? 1 : 0;

			return result;
		}
		
		public void put(Value key, int val)
		{
			int index;
			if (!valueToIndex.containsKey(key)) {
				index = used;
				used += 2;
				valueToIndex.put(key, index);
			} else {
				index = valueToIndex.get(key);
			}
			set(index, (val & 2) == 2);
			set(index + 1, (val & 1) == 1);
		}

		public  Set<String> getRestrictedType(Value v) {
			return ValueToRestrictedType.get(v);
		}

		public void setRestrictedType(Value v, String Type) {
			
			
			if (!ValueToRestrictedType.containsKey(v)) {
				Set<String> types = new ArraySet<String>();
				types.add(Type);
				ValueToRestrictedType.put(v, types);
				
			} else {

				ValueToRestrictedType.get(v).add(Type);
				
			}
			
		}
	}

	protected final static int BOTTOM = 0;
	protected final static int TYPE_IS_RESTRICTED = 1;
	protected final static int TYPE_IS_NOT_RESTRICTED = 2;
	protected final static int TOP = 3;
	
	protected final HashMap<Value,Integer> valueToIndex = new HashMap<Value,Integer>();
	protected HashMap<Value, Value> valueContainingRestrictionStmtToRestrictedValue = new HashMap<Value, Value>();
	protected HashMap<Value, HashMap<Value, String>> valueContainingRestrictionStmtToRestrictedValueAndType = new HashMap<Value, HashMap<Value,String>>();
	protected int used = 0;

	/**
	 * Creates a new analysis for the given graph/
	 * @param graph any unit graph
	 */
	public CopyOfTypeRestrictionAnalysis(UnitGraph graph) {
		super(graph);
		
		doAnalysis();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected void flowThrough(Object flowin, Unit u, List fallOut, List branchOuts) {
		AnalysisInfo in = (AnalysisInfo) flowin;
		AnalysisInfo out = new AnalysisInfo(in);
		AnalysisInfo outBranch = new AnalysisInfo(in);
		
		Stmt s = (Stmt)u;
		
		
		
		if(s instanceof DefinitionStmt) {
			DefinitionStmt defStmt = (DefinitionStmt) s;
			if(defStmt.getRightOp() instanceof JInstanceOfExpr) {
				handleInstanceOfAssignment(defStmt);
			}
		}
		
		
		
		if(s instanceof JIfStmt) {
			JIfStmt ifStmt = (JIfStmt) s;
			
			
			
			handleIfStmt(ifStmt, in, out, outBranch);
		}

		
		
		// now copy the computed info to all successors
		for( Iterator it = fallOut.iterator(); it.hasNext(); ) {
			copy( out, it.next() );
		}
		for( Iterator it = branchOuts.iterator(); it.hasNext(); ) {
			copy( outBranch, it.next() );
		}
	}
	
	
	
	
	private void handleInstanceOfAssignment(DefinitionStmt assignStmt) {
		Value left = assignStmt.getLeftOp();
		Value right = assignStmt.getRightOp();
	
		JInstanceOfExpr instanceOfExpr = (JInstanceOfExpr) right;
		
		valueContainingRestrictionStmtToRestrictedValue.put(left, instanceOfExpr.getOp());
		
		Type type = instanceOfExpr.getCheckType();
	
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private void handleIfStmt(JIfStmt ifStmt, AnalysisInfo in, AnalysisInfo out, AnalysisInfo outBranch) {
		Value condition = ifStmt.getCondition();
		if(condition instanceof JEqExpr || condition instanceof JNeExpr) {
			//Z0==0 or Z0!=0
			AbstractBinopExpr eqExpr = (AbstractBinopExpr) condition;
			handleEqualityOrNonEqualityCheck(eqExpr, in, out, outBranch); 	
			
		}
	}


	private void handleEqualityOrNonEqualityCheck(AbstractBinopExpr eqExpr, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {
		Value left = eqExpr.getOp1();
		Value right = eqExpr.getOp2();
		
		
		
		
		
		//if we compare a local with null then process further...
		if(valueContainingRestrictionStmtToRestrictedValue.containsKey(left)) {
			if(eqExpr instanceof JEqExpr)
				//Z0==0
				handleEquality(left, out, outBranch);
			else if(eqExpr instanceof JNeExpr)
				//Z0!=0
				handleNonEquality(left, out, outBranch);
			else
				throw new IllegalStateException("unexpected condition: "+eqExpr.getClass());
		}
	}
	

	private void handleNonEquality(Value val, AnalysisInfo out,
			AnalysisInfo outBranch) {
		
		
		//pour Z0!=0  c'et le outBranch pour le quele il y a la restriction dapre ce que je vois dans le jimple
		
		Value op = valueContainingRestrictionStmtToRestrictedValue.get(val);
		out.put(op, TYPE_IS_NOT_RESTRICTED);
		outBranch.put(op, TYPE_IS_RESTRICTED);
	}

	private void handleEquality(Value val, AnalysisInfo out,
			AnalysisInfo outBranch) {
		
		//pour Z0=0  c'et le out pour le quele il y a la restriction dapre ce que je vois dans le jimple		
		Value op = valueContainingRestrictionStmtToRestrictedValue.get(val);
		out.put(op, TYPE_IS_RESTRICTED);
		outBranch.put(op, TYPE_IS_NOT_RESTRICTED);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected void copy(Object source, Object dest) {
		AnalysisInfo s = (AnalysisInfo) source;
		AnalysisInfo d = (AnalysisInfo) dest;
		d.clear();
		d.or(s);
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
		outflow.clear();
		outflow.or((AnalysisInfo) in1);
		outflow.or((AnalysisInfo) in2);
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		return new AnalysisInfo();
	}
	
	/**
	 * Returns <code>true</code> if the analysis could determine that i is always null
	 * before the statement s.
	 * @param s a statement of the respective body
	 * @param i a local or constant of that body
	 * @return true if i is always null right before this statement
	 */
	public boolean isAlwaysRestrictedBefore(Unit s, Immediate i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);
		return ai.get(i)==TYPE_IS_RESTRICTED;
	}

	/**
	 * Returns <code>true</code> if the analysis could determine that i is always non-null
	 * before the statement s.
	 * @param s a statement of the respective body
	 * @param i a local of that body
	 * @return true if i is always non-null right before this statement
	 */
	public boolean isAlwaysNonRestrictedBefore(Unit s, Immediate i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);
		return ai.get(i)==TYPE_IS_NOT_RESTRICTED;
	}
}