package NotNullDirective;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.Unit;
import soot.UnitBox;
import soot.ValueBox;
import soot.jimple.ConditionExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArrayFlowUniverse;
import soot.toolkits.scalar.ArrayPackedSet;
import soot.toolkits.scalar.BoundedFlowSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.FlowUniverse;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;



public class NullTestedLocals 
{
	
	Map<Unit, List> unitToTestedLocalsAfter;
	Map<Unit, List> unitToTestedLocalsBefore;


	public NullTestedLocals(ExceptionalUnitGraph cfGraph){
		
		NullTestedLocalsFlowAnalysis analysis = new NullTestedLocalsFlowAnalysis(cfGraph);
		
		// Build unitToTestedLocals map
		{
			unitToTestedLocalsAfter = new HashMap(cfGraph.size() * 2 + 1, 0.7f);
			unitToTestedLocalsBefore = new HashMap(cfGraph.size() * 2 + 1, 0.7f);
			Iterator<Unit> unitIt = cfGraph.iterator();
			
			
			while (unitIt.hasNext()) {
				Unit unit = (Unit) unitIt.next();
				
				FlowSet set = (FlowSet) analysis.getFlowBefore(unit);
				unitToTestedLocalsBefore.put(unit,  Collections.unmodifiableList(set.toList()));
				
				
				
				set = (FlowSet) analysis.getFlowAfter(unit);
				unitToTestedLocalsAfter.put(unit,  Collections.unmodifiableList(set.toList()));
				
				
			}
			
			
		}
		
		
		
		
	}

	
	
	
	public List getLiveLocalsAfter(Unit unit)
	{
	return (List) unitToTestedLocalsAfter.get(unit);
	}
	public List getLiveLocalsBefore(Unit unit)
	{
	return (List) unitToTestedLocalsBefore.get(unit);
	}


}







 class NullTestedLocalsFlowAnalysis extends ForwardFlowAnalysis {

	FlowSet emptySet;
	Map<Unit, BoundedFlowSet> unitToGenerateSet;
	Map<Unit, BoundedFlowSet> unitToPreserveSet;

	protected Object newInitialFlow() {
		return emptySet.clone();
	}

	protected Object entryInitialFlow() {
		return emptySet.clone();
	}

	protected void flowThrough(Object inValue, Object d, Object outValue) {

		FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;
		Unit unit = (Unit) d;
		// we don't really need to perform kill because in this case all the
		// tested locals are preserved
		in.intersection((FlowSet) unitToPreserveSet.get(unit), out);

		// perform generation

		out.union((FlowSet) unitToGenerateSet.get(unit), out);

	}

	protected void merge(Object in1, Object in2, Object out) {

		FlowSet inSet1 = (FlowSet) in1;
		FlowSet inSet2 = (FlowSet) in2;

		FlowSet outSet = (FlowSet) out;

		inSet1.union(inSet2, outSet);

	}

	protected void copy(Object source, Object dest) {

		FlowSet sourceSet = (FlowSet) source;
		FlowSet destSet = (FlowSet) dest;

		sourceSet.copy(destSet);

	}

	public NullTestedLocalsFlowAnalysis(UnitGraph cfg) {
		super(cfg);

		// Generate list of locals and empty set

		{
			Chain<Local> locals = cfg.getBody().getLocals();
			FlowUniverse localUniverse = new ArrayFlowUniverse(locals.toArray());

			/*
			 * !!!! I have to verify with Bruno whether I need to have an
			 * emptySet initialized with all locals or empty (here i thought
			 * that ArrayPackedSet initialized emptySet with all locals in the
			 * Universe but I checked that emptySet was was empty i dont now i
			 * don't know how)
			 */

			emptySet = new ArrayPackedSet(localUniverse);

		}

		// Create preserve sets.

		{
			unitToPreserveSet = new HashMap(cfg.size() * 2 + 1, 0.7f);
			Iterator unitIt = cfg.iterator();

			BoundedFlowSet allLocalsPreservedSet = (BoundedFlowSet) emptySet
					.clone();
			allLocalsPreservedSet.complement(allLocalsPreservedSet);

			while (unitIt.hasNext()) {
				Unit unit1 = (Unit) unitIt.next();

				unitToPreserveSet.put(unit1, allLocalsPreservedSet);

			}

		}

		// Create generate sets
		{

			unitToGenerateSet = new HashMap(cfg.size() * 2 + 1, 0.7f);
			Iterator<Unit> unitIt = cfg.iterator();

			while (unitIt.hasNext()) {
				Unit unit = (Unit) unitIt.next();
				FlowSet genSet = (FlowSet) emptySet.clone();

				// the goal is to build a set of all locals which was compared
				// with null

				if (unit instanceof soot.jimple.IfStmt) {

					ConditionExpr condtionexpr = (ConditionExpr) ((soot.jimple.IfStmt) unit).getCondition();

					// check that the condition is a null verification

					if (condtionexpr.getOp2().toString() == "null") {

						List<ValueBox> useBox = unit.getUseBoxes();

						if (condtionexpr.getOp1() instanceof Local   && useBox.contains(condtionexpr.getOp1Box())  ) {

							genSet.add(condtionexpr.getOp1(), genSet);
							
							unitToGenerateSet.put(unit, (BoundedFlowSet) genSet);

							System.out.println("*****************1  condition valide ***************************");
							
						} else {
							// the genset in this case is empty
							unitToGenerateSet.put(unit, (BoundedFlowSet) genSet);
							System.out.println("*****************2  condition non valide op1 not instence of local or usebox dasent contain op1 ***************************");

						}

					} else {

						// the genset in this case is empty
						unitToGenerateSet.put(unit, (BoundedFlowSet) genSet);

						System.out.println("*****************3  condition non valide verification mais pas pour null ***************************");

						
					}

				} else {

					// the genset in this case is empty
					unitToGenerateSet.put(unit, (BoundedFlowSet) genSet);
					System.out.println("*****************4  condition non valide unit is not an if instraction  ***************************");

				}

			}

		}
		
		doAnalysis();

	}

}
