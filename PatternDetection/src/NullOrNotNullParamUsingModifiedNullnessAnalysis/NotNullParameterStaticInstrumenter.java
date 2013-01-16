package NullOrNotNullParamUsingModifiedNullnessAnalysis;

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
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JNeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LocalUnitPair;
import soot.toolkits.scalar.SimpleLocalDefs;

public class NotNullParameterStaticInstrumenter extends BodyTransformer {

	/* some internal fields */

	static PrintWriter out;
	static PrintWriter patternDistributionOverMethod;
	static PrintWriter detectedPattern;
	static HashMap<String, Integer> patternDistributionOverClasses;
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

		methodParameterChain = new ArrayList<Local>();

		
		
		for (int j = 0; j < method.getParameterCount(); j++) {

			methodParameterChain.add(body.getParameterLocal(j));

		}

		for (Local l : methodParameterChain) {
			
			
			System.out.println("-------->methodParameter:    "+l);
		}
		
		
		
		Iterator<Unit> units =cfg.iterator();
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			
			
			System.out.println("-------->unit:    "+unit);
			
			
			
		
			
			
			
		}


		Map<Local, Local> localDefinedUsingParameterToParameter = new  HashMap<Local, Local> (cfg.size() * 2 + 1, 0.7f);
		
		localDefinedUsingParameterToParameter= getLocalsDefinedUsingParameter(cfg);
		 Set<Local> LocalsDefinedUsingParameterset = localDefinedUsingParameterToParameter.keySet();
		
		 
		 
		 //lacer les tested parameter et les modified nullnessAnalysis

		 ModifiedNullnessAnalysis modifiedNullnessAnalysis = new ModifiedNullnessAnalysis(cfg);
		 NullTestedLocalsAnalysis nullTestedLocalsAnalysis = new NullTestedLocalsAnalysis((ExceptionalUnitGraph)cfg);
		 
		 
	//detect null not allowed patern 	
		
		 detectNullNotAllowedPatern(cfg,localDefinedUsingParameterToParameter,modifiedNullnessAnalysis,nullTestedLocalsAnalysis);
		
	}

	
	
	
	
	private void  detectNullNotAllowedPatern(UnitGraph cfg, Map<Local, Local> localDefinedUsingParameterToParameter,ModifiedNullnessAnalysis modifiedNullnessAnalysis, NullTestedLocalsAnalysis nullTestedLocalsAnalysis) {
		
		
		/*to do
		 *  faire une structure (NullNotAllowed for param )qui pour chaque paramaiter qui ne dois pas etre nulle 
		 *  map to un tableau ou une liste pour indique les diferent type d'utilisation  qui ont 
		 *  iplique que le paramaitre ne  dois pas etre null si il y a plusieur ou la seul si il ya une seul 
		 *  
		 *   cette structure est retourne par la methode detectNullNotAllowedPatern
		 *  
		 *  cette structure est construite en utilisant les diferente methode 
		 *  handleThrowStmt ,handlearray ref fild ref ... qui doive retournier un bool qui indique que 
		 *  la patron est detecter ou non 
		 * 
		 * */ 
		
		
		
		Set<Local> LocalsDefinedUsingParameterSet = localDefinedUsingParameterToParameter.keySet();
		
		Iterator<Unit> units =cfg.iterator();
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			
			if (unit instanceof ThrowStmt) {
				
				for (Local l : methodParameterChain) {
					
					
					Boolean throwStmtBecauseofnNullParam = false ;
					
					if(modifiedNullnessAnalysis.isAlwaysNullBefore(unit, l)){
						
						throwStmtBecauseofnNullParam= handleThrowStmt(l,unit,cfg);
						
						
					}
					
					//ici construire progresivement la structure (NullNotAllowed for param )
					
					if (throwStmtBecauseofnNullParam) {
						
						System.out.println("pattern detected param  "+ l + " must not be null  otherwise an exception is thrown ");
						
					}
					
					
				}
				
				
				for (Local l : LocalsDefinedUsingParameterSet) {
					
					Boolean throwStmtBecauseofnNullParam = false ;
					
					
					if (modifiedNullnessAnalysis.isAlwaysNullBefore(unit, l)) {
						
						
						throwStmtBecauseofnNullParam= handleThrowStmt(l,unit,cfg);
						
					}
					
					//ici construire progresivement la structure (NullNotAllowed for param )
					
					if (throwStmtBecauseofnNullParam) {
						
						System.out.println("pattern detected param  "+ localDefinedUsingParameterToParameter.get(l) + " must not be null it define a local "+ l  +  "which when is null an exception is thrown ");
						
					}
					
					
					
				}
				
				
				
				
				
				
				
				
				
				
				
			} else {
				
				
				
				
				
				

			}
			
			
		}
		
					
				
	}
	
	
	
	private Boolean handleThrowStmt (Local l,Unit unit, UnitGraph cfg){
		
		
		int PathLength=0;
		
		
		Boolean findConditionComparingLlocalToNull=false;
		Boolean mustExitWhileLoop =false;
		Unit theConsideredPredecessor  = null;
		
		List<Unit> entryPointList=cfg.getHeads();
		List<Unit> predsList = cfg.getPredsOf(unit);
		
		
		while (PathLength < 4 && !findConditionComparingLlocalToNull && !mustExitWhileLoop){
			
			
			
			if (predsList.size() == 1) {
				
				theConsideredPredecessor=predsList.get(0);
				
				if (theConsideredPredecessor instanceof soot.jimple.IfStmt) {
					
					Value condition =((soot.jimple.IfStmt)theConsideredPredecessor).getCondition();
					
					
					if(condition instanceof JEqExpr || condition instanceof JNeExpr) {
						//a==b or a!=b
						AbstractBinopExpr eqExpr = (AbstractBinopExpr) condition;
						findConditionComparingLlocalToNull = handleEqualityOrNonEqualityToNullCheck(eqExpr, l,theConsideredPredecessor);
					} 		
					
					
				}else if (entryPointList.contains(unit)) {
					
					mustExitWhileLoop=true;
					
					
				}  
					
					//monter dans le pathe pour verifier les autre unit 
					
					predsList = cfg.getPredsOf(theConsideredPredecessor);
					
					PathLength++;
				
				
				
				
				
			} else if (predsList.size()== 0) {
				
				mustExitWhileLoop =true ; //pas de Predecessor 
				
			} else if (predsList.size() > 1) {
				
				
				mustExitWhileLoop =true ; //plusieur  Predecessor  ce cas n'est pas traiter voir la documentation 
				
			} 		
			
			
			
		}
		
		
		return findConditionComparingLlocalToNull ;
		
	}
	
	
	
	
	
	private Boolean handleEqualityOrNonEqualityToNullCheck(AbstractBinopExpr eqExpr, Local l, Unit theConsideredPredecessor) {
		Value left = eqExpr.getOp1();
		Value right = eqExpr.getOp2();
		
		Value val=null;
		if(left==NullConstant.v()) {
			if(right!=NullConstant.v()) {
				val = right;
			}
		} else if(right==NullConstant.v()) {
			if(left!=NullConstant.v()) {
				val = left;
			}
		}
		
		//if we compare a local with null then process further...
		if(val!=null && val instanceof Local) {
			if(eqExpr instanceof JEqExpr ){
				//a==null
				if (val.equals(l)) {
					
					return true;
					
				} else {
					
					return false; 

				}
				 
				
			}else if(eqExpr instanceof JNeExpr ){
				//a!=null
				if (val.equals(l)) {
					
					return true;
					
				} else {
					
					return false; 

				}

			}else
				throw new IllegalStateException("unexpected condition: "+eqExpr.getClass() + "  unit is :  " +theConsideredPredecessor);
		}else {
			
			
			return false ;
			
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/* dans getLocalsDefinedUsingParameter
	 * on recupaire les variable initialiser à partir de paramaitre sur un seul niveau
	 * si une variable est initialiser ou definit apartir d'un paramaitre on la recupaire 
	 * si une variable est initailaiser à apartir
	 * d'une variable qui elle est initaliser d'un paramitre on ne la recupaire pas 
	 * on ne considaire que les definition simple  pas d'invocation ni d'expression qui contiennet un paramaitre 
	 * 
	 * */
	
	
	private Map<Local, Local> getLocalsDefinedUsingParameter(UnitGraph cfg) {
		
		 
		
		Map<Local, Local> localToParameter = new  HashMap<Local, Local> (cfg.size() * 2 + 1, 0.7f);
		
		Iterator<Unit> units =cfg.iterator();
		
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			Stmt stmt= (Stmt)unit;
			
			if (stmt  instanceof DefinitionStmt) {
				
				
				DefinitionStmt assignStmt = (DefinitionStmt) stmt;
				Value left = assignStmt.getLeftOp();
				Value right = assignStmt.getRightOp();
				
				//unbox casted value
				if(right instanceof JCastExpr) {
					JCastExpr castExpr = (JCastExpr) right;
					right = castExpr.getOp();
				}
				
				
				if (left  instanceof Local){
					
					if (right  instanceof Local){
					
						
						if (methodParameterChain.contains(right)) {
						
						localToParameter.put((Local)left, (Local)right);
						
						 System.out.println("*----l'unité --> "+ unit +" --definit" + left+	" parun parametre----la Value------> "+right);
						 
						
						
						}
						
					}
					
					
				}
				
				
				
				
			}
			
			
		}
		
		
		
		
		
		
		
		
		
//		Set<Local> LocalsDefinedUsingParameterList = localToParameter.keySet();
		
		
		return localToParameter ;
	}
	
	
	/*getLocalsDefinedUsingParameter1
	 * recupairer une liste de local initialisetr à partir de paramaitre 
	 * linitialisation et de tout forme il sufit que dans la parti droite un paramaitre existe pour que on recupaire
	 * la variable dans la partie gauche danc les invocation de methode qui utiolise un paramaitre font partie des initialisation
	 * prise en charge 
	 * 
	 * 
	 * */
	
	
	private  ArrayList<Local> getLocalsDefinedUsingParameter1(UnitGraph cfg) {
		
		 ArrayList<Local> LocalsDefinedUsingParameterList =new ArrayList<Local>();
		 Map<Local, Local> localToParameter = new  HashMap<Local, Local> (cfg.size() * 2 + 1, 0.7f);
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
		            		  		
		            		  		localToParameter.put(defLocal, (Local)valueBox.getValue());
		            		  		 System.out.println("*----l'unité --> "+ unit +" -----utilise un parametre----la Value------> "+valueBox.getValue()+ " --------qui existe dans les methodParameterChain  ");
		            		  		 
		            		  		LocalsDefinedUsingParameterList.add(defLocal);
		            		  		
		            		  		//on peut vour la posibilité de fair un map qui contien les unit et les les local initialiser apartir d'un parametre dans cett unit 
		            		  	}
		            		        		  
		            	  }
					}
				
				
			} 
			
			
			
		}
		 
		 Set<Local> LocalsDefinedUsingParameterset = localToParameter.keySet();
		 return LocalsDefinedUsingParameterList;
		
	}
	
	

	


}
