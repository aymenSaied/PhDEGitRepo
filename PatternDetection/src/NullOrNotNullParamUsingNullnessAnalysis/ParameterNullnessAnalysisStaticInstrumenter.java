package NullOrNotNullParamUsingNullnessAnalysis;

/*
 * InvokeStaticInstrumenter inserts count instructions before
 * INVOKESTATIC bytecode in a program. The instrumented program will
 * report how many static invocations happen in a run.
 * 
 * Goal:
 *   Insert counter instruction before static invocation instruction.
 *   Report counters before program's normal exit point.
 *
 * Approach:
 *   1. Create a counter class which has a counter field, and 
 *      a reporting method.
 *   2. Take each method body, go through each instruction, and
 *      insert count instructions before INVOKESTATIC.
 *   3. Make a call of reporting method of the counter class.
 *
 * Things to learn from this example:
 *   1. How to use Soot to examine a Java class.
 *   2. How to insert profiling instructions in a class.
 */

/* InvokeStaticInstrumenter extends the abstract class BodyTransformer,
 * and implements <pre>internalTransform</pre> method.
 */
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.toolkits.annotation.nullcheck.NullnessAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class ParameterNullnessAnalysisStaticInstrumenter extends BodyTransformer {

	/* some internal fields */

	static PrintWriter out;
	static PrintWriter patternDistributionOverMethod;
	static PrintWriter detectedPattern;
	static HashMap<String, Integer> patternDistributionOverClasses;
	static ArrayList<Local> methodParameterChain;

	static {
		patternDistributionOverClasses = new HashMap<String, Integer>(350);

	}

	public ParameterNullnessAnalysisStaticInstrumenter(PrintWriter pw1, PrintWriter pw2,
			PrintWriter pw3) {

	}

	/*
	 * internalTransform goes through a method body and inserts counter
	 * instructions before an INVOKESTATIC instruction
	 */
	protected void internalTransform(Body body, String phase, Map options) {
		// body's method

		SootMethod method = body.getMethod();
		SootClass declaringClass = method.getDeclaringClass();
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		System.out.println("instrumenting method : " + method.getSignature()
				+ "    in class  " + declaringClass.getName());

		Integer nbOfDetectedpatternInCurrentMethod = 0;

		UnitGraph cfg = new ExceptionalUnitGraph(body);

		methodParameterChain = new ArrayList<Local>();

		
		
		for (int j = 0; j < method.getParameterCount(); j++) {

			methodParameterChain.add(body.getParameterLocal(j));

		}

		for (Local l : methodParameterChain) {
			
			
			System.out.println("-------->methodParameter:    "+l);
		}
		
		
		NullnessAnalysis nullnessAnalysis= new NullnessAnalysis(cfg);
		
		
		Iterator<Unit> units =cfg.iterator();
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			
			
			System.out.println("-------->unit:    "+unit);
			
			
			List<ValueBox> useBoxesList = unit.getUseBoxes();
			
			ArrayList<Local> usedLocal = new ArrayList<Local>() ;
			
			for (ValueBox valueBox : useBoxesList) {
				
				if (valueBox.getValue() instanceof Local) {
					
					usedLocal.add((Local)valueBox.getValue());

				}
				
			}
			
			  
			
			for (Local l : methodParameterChain) {
				
				
				//modifier pour inclur if in unit.getUseAndDefBoxes()
				
				if (true/*usedLocal.contains(l)*/) {
				
					System.out.println("-------->param:                       "+l);
					System.out.println("-------->isAlways Null Before:        "+ nullnessAnalysis.isAlwaysNullBefore(unit, l));
					System.out.println("-------->isAlways Non_Null Before:    "+ nullnessAnalysis.isAlwaysNonNullBefore(unit, l));
					
					
					
				}
				
				
			}
				
			
		
			
			
			
		}


		
		
		
		
			
		
	}

	
	
	
	

	
	


}
