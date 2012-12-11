package NotNullDirective;

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
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class NotNullParameterStaticInstrumenter extends BodyTransformer {

	/* some internal fields */

	static PrintWriter out;
	static PrintWriter patternDistributionOverMethod;
	static PrintWriter detectedPattern;
	static HashMap<String, Integer> patternDistributionOverClasses;
	static SimpleLocalDefsUsingParameter simpleLocalDefs;
	static ArrayList<Local> methodParameterChain;

	static {
		patternDistributionOverClasses = new HashMap<String, Integer>(350);

	}

	public NotNullParameterStaticInstrumenter(PrintWriter pw1, PrintWriter pw2,
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
		simpleLocalDefs = new SimpleLocalDefsUsingParameter(cfg);

		methodParameterChain = new ArrayList<Local>();

		for (int j = 0; j < method.getParameterCount(); j++) {

			methodParameterChain.add(body.getParameterLocal(j));

		}

		ArrayList<Local> LocalsDefinedUsingParameterList =new ArrayList<Local>();
		
		LocalsDefinedUsingParameterList=getLocalsDefinedUsingParameter(cfg);
		
		
		SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(cfg); 
		
		Iterator<Unit> units =cfg.iterator();
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			
			
			List<ValueBox> useBoxes = unit.getUseBoxes();
			List<ValueBox> defBoxes = unit.getDefBoxes();
			System.out.println("1-------->unit:    "+unit);
			
			
			for (ValueBox valueBox : useBoxes) {
				
				System.out.println("2-------->useBoxes:    "+valueBox.getValue());
				
				if (valueBox.getValue() instanceof Local) {
					
					List<Unit> listOfDefiningUnit =simpleLocalDefs.getDefsOfAt((Local)valueBox.getValue(), unit);
					
					for (Unit unit2 : listOfDefiningUnit) {
						
						System.out.println("-------->DefiningUnit:    "+unit2);

					}
					
					
				}
				
			}
			
			for (ValueBox valueBox : defBoxes) {
			
				System.out.println("3-------->defBoxes:    "+valueBox.getValue());
				
				
				
				
			}
		
			
			
			
		}

		//ListingTestedLocalsBeforAndAfterUnit(cfg);

		
		ArrayList<Local> methodParameterAndLocalsDefinedUsingParameterList =new ArrayList<Local>();
		
		methodParameterAndLocalsDefinedUsingParameterList=methodParameterChain;
		
		if (LocalsDefinedUsingParameterList.size()>0) {
			
			System.out.println("il y a des locals DefinedUsingParameter  ");

			for (Local local : LocalsDefinedUsingParameterList) {
				
				System.out.println("local:  "+local);

				
			}
			
		}
		
		
		if (LocalsDefinedUsingParameterList.size()>0) {
			
			for (Local local : LocalsDefinedUsingParameterList) {
	
				methodParameterAndLocalsDefinedUsingParameterList.add(local);
				
			}
	
			
			//System.out.print("il y a des locals DefinedUsingParameter  ");
			
		}
		
		detectUsedAndNotTestedMetodParameter( cfg, methodParameterAndLocalsDefinedUsingParameterList);
	}

	private  ArrayList<Local> getLocalsDefinedUsingParameter(UnitGraph cfg) {
		
		 ArrayList<Local> LocalsDefinedUsingParameterList =new ArrayList<Local>();
		 
		Iterator<Unit> units =cfg.iterator();
		 
		 while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			List defBoxes = unit.getDefBoxes();
			if (!defBoxes.isEmpty()) {
				
				if(!(defBoxes.size() ==1)) 
	                throw new RuntimeException("SimpleLocalDefs: invalid number of def boxes");
				 	Value value = ((ValueBox)defBoxes.get(0)).getValue();
				 	
				 	if (value  instanceof Local) {
						
				 		Local defLocal = (Local) value;
				 		List<ValueBox> vb = unit.getUseBoxes();
				 		
				 		 for (ValueBox valueBox : vb) {
   		        		  
		            		  	if (methodParameterChain.contains(valueBox.getValue())) {
		            		  		
		            		  		
		            		  		 System.out.println("*----l'unité --> "+ unit +" -----utilise un parametre----la Value------> "+valueBox.getValue()+ " --------qui existe dans les methodParameterChain  ");
		            		  		 
		            		  		LocalsDefinedUsingParameterList.add(defLocal);
		            		  		
		            		  		//on peut vour la posibilité de fair un map qui contien les unit et les les local initialiser apartir d'un parametre dans cett unit 
		            		  	}
		            		        		  
		            	  }
					}
				
				
			} 
			
			
			
		}
		 
		 
		 return LocalsDefinedUsingParameterList;
		
	}
	
	
	
	
	protected void detectUsedAndNotTestedMetodParameter(UnitGraph cfg, ArrayList<Local> localsList) {

		Iterator<Unit> units = cfg.iterator();

		ArrayList<Local> methodParameterAndLocalsDefinedUsingParameterList =localsList;

		
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();

			if (!(unit instanceof soot.jimple.IfStmt)) {

				List<ValueBox> useBoxes = unit.getUseBoxes();

				for (ValueBox valueBox : useBoxes) {

					if (valueBox.getValue() instanceof Local) {
						
						
						
						if (methodParameterAndLocalsDefinedUsingParameterList.contains(valueBox.getValue())) {
							
							NullTestedLocals testedLocals = new NullTestedLocals((ExceptionalUnitGraph) cfg);
							
							List<Local> testedlocalsBeforUnit = testedLocals.getLiveLocalsBefore(unit);
							
							
							if (testedlocalsBeforUnit.contains(valueBox.getValue())) {
								
								System.out.println(" parametre convenablement utiliser   " + valueBox.getValue()+"  dans  " +unit);
							}else {
								
								System.out.println("# pattern detected parametre  utiliser sans etre tester par raport null  " + valueBox.getValue()+"  dans  " +unit);

								
							}
							
							
							System.out.println("   variable dans methodParameterChain " + valueBox.getValue());
							
						}else {
							
							
							System.out.println("   variable pas dans methodParameterChain " + valueBox.getValue());

							
							
						}
						

					}

				}

			}

		}

	}
	
	
	
	protected void ListingTestedLocalsBeforAndAfterUnit(UnitGraph cfg){
		
		NullTestedLocals testedLocals = new NullTestedLocals(
				(ExceptionalUnitGraph) cfg);

		Iterator<Unit> Units = cfg.iterator();
		
		while (Units.hasNext()) {
			Unit unit = (Unit) Units.next();

			System.out.println("a-----> unit:  " + unit);
			List<ValueBox> useBoxs = unit.getUseBoxes();

			for (ValueBox valueBox : useBoxs) {

				System.out.println("b-----> useBoxvalue:  "
						+ valueBox.getValue());

				List<Local> testedlocalsBeforUnit = testedLocals
						.getLiveLocalsBefore(unit);

				List<Local> testedlocalsAfter = testedLocals
						.getLiveLocalsAfter(unit);

				System.out.println("%---size--> testedlocalsBeforUnit :  "
						+ testedlocalsBeforUnit.size());
				System.out.println("%---size--> testedlocalsAfter     :  "
						+ testedlocalsAfter.size());

				// for (Object object : testedlocalsBeforUnit) {
				//
				// System.out.println("**%-----> testedlocalsBeforUnit:  "+object);
				//
				//
				// }for (Object object : testedlocalsAfter) {
				//
				// System.out.println("**%-----> testedlocalsAfter    :  "+object);
				//
				//
				// }

				for (Local local : testedlocalsBeforUnit) {

					System.out.println("%-----> testedlocalsBeforUnit:  "
							+ local);

				}

				for (Local local : testedlocalsAfter) {

					System.out.println("%-----> testedlocalsAfter:       "
							+ local);

				}

			}

		}
		
		
		
	}

}
