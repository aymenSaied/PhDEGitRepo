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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import soot.util.Chain;

/**
 * An intraprocedural TypeRestriction analysis that computes for each location
 * and each value in a method if the value is (before or after that location)
 * definetely TypeIsRestricted, definetely TypeIsNotRestricted or neither.
 * 
 * 
 * 
 */
public class TypeRestrictionAnalysis extends ForwardBranchedFlowAnalysis {

	protected LocalTypeSet emptySet;
	
	
	/**
	 * The analysis info is a simple mapping of type {@link Value} to any of the
	 * constants BOTTOM, TYPE_IS_NOT_RESTRICTED, TYPE_IS_RESTRICTED or TOP. This
	 * class returns BOTTOM by default.
	 * 
	 * 
	 */
	protected class AnalysisInfo extends java.util.BitSet {

		LocalTypeSet localTypeRestrictionSet;

		public AnalysisInfo() {

			super(used);

			localTypeRestrictionSet = (LocalTypeSet) emptySet.clone();
		}

		public AnalysisInfo(AnalysisInfo other) {
			super(used);
			or(other);
			localTypeRestrictionSet = other.localTypeRestrictionSet;

		}

		public int get(Value key) {
			if (!valueToIndex.containsKey(key))
				return BOTTOM;

			int index = valueToIndex.get(key);
			int result = get(index) ? 2 : 0;
			result += get(index + 1) ? 1 : 0;

			return result;
		}

		public void put(Value key, int val) {
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

	}

	protected final static int BOTTOM = 0;
	protected final static int TYPE_IS_RESTRICTED = 1;
	protected final static int TYPE_IS_NOT_RESTRICTED = 2;
	protected final static int TOP = 3;

	protected final HashMap<Value, Integer> valueToIndex = new HashMap<Value, Integer>();
	protected HashMap<Value, Value> valueContainingRestrictionStmtToRestrictedValue = new HashMap<Value, Value>();
	protected HashMap<Value, Type> valueContainingRestrictionStmtToRestrictionType = new HashMap<Value, Type>();
	protected int used = 0;

	/**
	 * Creates a new analysis for the given graph/
	 * 
	 * @param graph
	 *            any unit graph
	 */
	public TypeRestrictionAnalysis(UnitGraph graph) {
		super(graph);
		makeInitialSetForRestriction();
		doAnalysis();
	}

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
		 * pas besoin de fair le kill de AnalysisInfo.localTypeRestrictionSet
		 * pour les Locals definit dans le statement car on fait l'acumulation
		 * des type posible que peut avoire la variable qui a une restirction de
		 * type mais la praisonce de la restriciton de type est grer dans
		 * TYPE_IS_RESTRICTED et TYPE_IS_NOT_RESTRICTED
		 */

		Stmt s = (Stmt) u;

		if (s instanceof DefinitionStmt) {
			DefinitionStmt defStmt = (DefinitionStmt) s;
			if (defStmt.getRightOp() instanceof JInstanceOfExpr) {
				handleInstanceOfAssignment(defStmt);
			} else if (defStmt.getRightOp() instanceof CastExpr) {
				handelCastAssignment(defStmt, in, out, outBranch);

			} else if (defStmt.getRightOp() instanceof Local) {
				handelSimpleCopyAssignment(defStmt, in, out, outBranch);

			} else {

				/*
				 * traitement pour les utilisation de local dans les use box qui ne
				 * sont pas prealablement restrin et donc il peuvent etre npon
				 * restrint
				 */
				handelNonRestrictedUse(defStmt, in, out, outBranch);

			}

		} else if (s instanceof JIfStmt) {
			JIfStmt ifStmt = (JIfStmt) s;

			handleIfStmt(ifStmt, in, out, outBranch);
		} else {

			/*
			 * traitement pour les utilisation de local dans les use box qui ne
			 * sont pas prealablement restrin et donc il peuvent etre npon
			 * restrint
			 */
			
			handelNonRestrictedUse(s, in, out, outBranch);
			
		}

		// now copy the computed info to all successors
		for (Iterator it = fallOut.iterator(); it.hasNext();) {
			copy(out, it.next());
		}
		for (Iterator it = branchOuts.iterator(); it.hasNext();) {
			copy(outBranch, it.next());
		}
	}

	private void handelSimpleCopyAssignment(DefinitionStmt assignStmt,
			AnalysisInfo in, AnalysisInfo out, AnalysisInfo outBranch) {
		Value left = assignStmt.getLeftOp();
		Value right = assignStmt.getRightOp();

		if (left instanceof Local && right.getType() instanceof RefType) {

			if (right instanceof Local) {
				out.localTypeRestrictionSet.localCopy((Local) left,
						(Local) right);

				out.put((Local) left, in.get((Local) right));
			}

		}

	}

	private void handelCastAssignment(DefinitionStmt assignStmt,
			AnalysisInfo in, AnalysisInfo out, AnalysisInfo outBranch) {

		Value left = assignStmt.getLeftOp();
		Value right = assignStmt.getRightOp();
		CastExpr cast = (CastExpr) right;

		if (cast.getCastType() instanceof RefType
				&& cast.getOp() instanceof Local) {
			RefType refType = (RefType) cast.getCastType();
			Local opLocal = (Local) cast.getOp();

			out.localTypeRestrictionSet.setRestrictionType(opLocal, refType);
			out.put(opLocal, TYPE_IS_RESTRICTED);
			/*
			 * TODO L = (T) P est ce que on efectue un kill pour les instruction
			 * suivante est ce que il y a restriction dans cette instruction
			 * pour P et dans les instruction suivante est ce que il y a
			 * restriction dans cette instruction pour L et dans les instruction
			 * suivantes
			 * resolut apres discution vec bruno et la version presente est correcte 
			 * 
			 */

			if (left instanceof Local && right.getType() instanceof RefType) {
				Local l = (Local) left;
				out.localTypeRestrictionSet.localCopy(l, opLocal);

				out.put(l, TYPE_IS_RESTRICTED); // je ne suis pas sur que il y a
												// une restriction sur L a cause
												// de L = (T) P

			}

		}

	}

	private void handelNonRestrictedUse(Stmt s, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {

		List<ValueBox> useBoxes = s.getUseBoxes();

		Iterator<ValueBox> ubIterator = useBoxes.iterator();

		while (ubIterator.hasNext()) {
			ValueBox valueBox = (ValueBox) ubIterator.next();

			if (valueBox.getValue() instanceof Local) {

				AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);

				if ((!(ai.get(valueBox.getValue()) == TYPE_IS_RESTRICTED))
						&& (!(ai.get(valueBox.getValue()) == TOP))) {

					/*
					 * à ce point on a une utilisation de local that have never
					 * been restricted donc on peut dir que il n'est pas
					 * restricted car il est utuliser sans restriction
					 */

					out.put(valueBox.getValue(), TYPE_IS_NOT_RESTRICTED);

				}

			}

		}

	}

	private void handleInstanceOfAssignment(DefinitionStmt assignStmt) {
		Value left = assignStmt.getLeftOp();
		Value right = assignStmt.getRightOp();
		JInstanceOfExpr instanceOfExpr = (JInstanceOfExpr) right;
		if (instanceOfExpr.getCheckType() instanceof RefType
				&& instanceOfExpr.getOp() instanceof Local) {
			valueContainingRestrictionStmtToRestrictedValue.put(left,
					instanceOfExpr.getOp());

			Type type = instanceOfExpr.getCheckType();
			valueContainingRestrictionStmtToRestrictionType.put(left, type);

		}

	}

	private void handleIfStmt(JIfStmt ifStmt, AnalysisInfo in,
			AnalysisInfo out, AnalysisInfo outBranch) {
		Value condition = ifStmt.getCondition();
		if (condition instanceof JEqExpr || condition instanceof JNeExpr) {
			// Z0==0 or Z0!=0
			AbstractBinopExpr eqExpr = (AbstractBinopExpr) condition;
			handleEqualityOrNonEqualityCheck(eqExpr, in, out, outBranch);

		}
	}

	private void handleEqualityOrNonEqualityCheck(AbstractBinopExpr eqExpr,
			AnalysisInfo in, AnalysisInfo out, AnalysisInfo outBranch) {
		Value left = eqExpr.getOp1();
		Value right = eqExpr.getOp2();

		// if we compare a local with null then process further...
		if (valueContainingRestrictionStmtToRestrictedValue.containsKey(left)) {
			if (eqExpr instanceof JEqExpr)
				// Z0==0
				handleEquality(left, out, outBranch);
			else if (eqExpr instanceof JNeExpr)
				// Z0!=0
				handleNonEquality(left, out, outBranch);
			else
				throw new IllegalStateException("unexpected condition: "
						+ eqExpr.getClass());
		}
	}

	private void handleNonEquality(Value val, AnalysisInfo out,
			AnalysisInfo outBranch) {

		// pour Z0!=0 c'et le outBranch pour le quele il y a la restriction
		// d'après ce que je vois dans le jimple

		Value op = valueContainingRestrictionStmtToRestrictedValue.get(val);
		Type type = valueContainingRestrictionStmtToRestrictionType.get(val);
		out.put(op, TYPE_IS_NOT_RESTRICTED);

		outBranch.put(op, TYPE_IS_RESTRICTED);
		outBranch.localTypeRestrictionSet.setRestrictionType((Local) op,
				(RefType) type);
	}

	private void handleEquality(Value val, AnalysisInfo out,
			AnalysisInfo outBranch) {

		// pour Z0=0 c'et le out pour le quele il y a la restriction dapre ce
		// que je vois dans le jimple
		Value op = valueContainingRestrictionStmtToRestrictedValue.get(val);
		Type type = valueContainingRestrictionStmtToRestrictionType.get(val);
		out.put(op, TYPE_IS_RESTRICTED);
		out.localTypeRestrictionSet.setRestrictionType((Local) op,
				(RefType) type);

		outBranch.put(op, TYPE_IS_NOT_RESTRICTED);
	}

	protected void copy(Object source, Object dest) {
		AnalysisInfo s = (AnalysisInfo) source;
		AnalysisInfo d = (AnalysisInfo) dest;

		d.clear();
		d.localTypeRestrictionSet.clearAllBits();

		d.or(s);
		d.localTypeRestrictionSet.or(s.localTypeRestrictionSet);

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
		outflow.localTypeRestrictionSet.clearAllBits();

		outflow.or((AnalysisInfo) in1);
		outflow.localTypeRestrictionSet
				.or(((AnalysisInfo) in1).localTypeRestrictionSet);

		outflow.or((AnalysisInfo) in2);
		outflow.localTypeRestrictionSet
				.or(((AnalysisInfo) in2).localTypeRestrictionSet);
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {

		return new AnalysisInfo();
	}

	/**
	 * Find all the locals of reference type and all the types used in
	 * JInstanceOfExpr and CastExpr to initialize the mapping from locals and
	 * types to bits in the bit vector in LocalTypeSet.
	 */
	protected void makeInitialSetForRestriction() {
		// Find all locals of reference type
		Chain locals = ((UnitGraph) graph).getBody().getLocals();
		List<Local> refLocals = new ArrayList<Local>();
		for (Iterator lIt = locals.iterator(); lIt.hasNext();) {
			final Local l = (Local) lIt.next();
			if (l.getType() instanceof RefType) {
				refLocals.add(l);
			}
		}

		// Find types of all JInstanceOf and Cast Expr
		List<Type> types = new ArrayList<Type>();
		for (Iterator sIt = ((UnitGraph) graph).getBody().getUnits().iterator(); sIt
				.hasNext();) {
			final Stmt s = (Stmt) sIt.next();

			if (s instanceof DefinitionStmt) {
				DefinitionStmt ds = (DefinitionStmt) s;
				Value rhs = ds.getRightOp();

				if (rhs instanceof JInstanceOfExpr) {

					Type t = ((JInstanceOfExpr) rhs).getCheckType();
					if (t instanceof RefType && !types.contains(t)) {
						types.add(t);
					}

				} else if (rhs instanceof CastExpr) {
					Type t = ((CastExpr) rhs).getCastType();
					if (t instanceof RefType && !types.contains(t)) {
						types.add(t);
					}
				}
			}
		}

		emptySet = new LocalTypeSet(refLocals, types);
	}

	/**
	 * Returns <code>true</code> if the analysis could determine that i is
	 * always null before the statement s.
	 * 
	 * @param s
	 *            a statement of the respective body
	 * @param i
	 *            a local or constant of that body
	 * @return true if i is always null right before this statement
	 */

	public boolean isAlwaysRestrictedBefore(Unit s, Immediate i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);
		return ai.get(i) == TYPE_IS_RESTRICTED;
	}

	
	public boolean isBothRestrictedAndUnRestrictedBefore(Unit s, Immediate i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);
		return ai.get(i) == TOP;
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
	public boolean isAlwaysNonRestrictedBefore(Unit s, Immediate i) {
		AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);
		// getFallFlowAfter(s)
		return ai.get(i) == TYPE_IS_NOT_RESTRICTED;
	}

	/**
	 * 
	 * return null if isAlwaysRestrictedBefore(s,i) return false on dois
	 * verifier que RestrictionTypesList.size() != 0
	 * 
	 * */
	public List<Type> getRestrictionTypesBefore(Unit s, Immediate i) {

		List<Type> RestrictionTypesList = new ArrayList<Type>();

		if (isAlwaysRestrictedBefore(s, i)||isBothRestrictedAndUnRestrictedBefore(s,i)) {

			AnalysisInfo ai = (AnalysisInfo) getFlowBefore(s);

			Iterator<Type> RestrictionTypeItherator = ai.localTypeRestrictionSet.types
					.iterator();

			while (RestrictionTypeItherator.hasNext()) {
				Type type = (Type) RestrictionTypeItherator.next();

				int index = ai.localTypeRestrictionSet.indexOf((Local) i,
						(RefType) type);

				if (ai.localTypeRestrictionSet.get(index)) {

					RestrictionTypesList.add(type);

				}

			}

		}

		if (isAlwaysRestrictedBefore(s, i)&&RestrictionTypesList.size() == 0) {

			try {
				throw new Exception(
						"RestrictionTypesList.size() is not suposed to be 0 ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return RestrictionTypesList;

	}

}