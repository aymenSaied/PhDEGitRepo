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
import soot.*;
import soot.JastAddJ.IfStmt;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.*;
import soot.jimple.internal.JThrowStmt;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.KeyStore.Entry;
import java.util.*;

import javax.swing.text.StyledEditorKit.BoldAction;

public class InvokeStaticInstrumenterP1 extends BodyTransformer{

  /* some internal fields */
  static SootClass counterClass;
  static SootMethod increaseCounter, reportCounter;
  static PrintWriter out  ;
  static PrintWriter patternDistributionOverMethod  ;
  static PrintWriter detectedPattern  ;
  static HashMap<String, Integer> patternDistributionOverClasses; 
  
  
  static {
    //counterClass    = Scene.v().loadClassAndSupport("MyCounter");
    //increaseCounter = counterClass.getMethod("void increase(int)");
    //reportCounter   = counterClass.getMethod("void report()");
	  patternDistributionOverClasses=new HashMap<String, Integer>(350);
	  
  }

 public InvokeStaticInstrumenterP1(PrintWriter pw1 ,PrintWriter pw2 ,PrintWriter pw3){
	  
	  out=pw1;
	  patternDistributionOverMethod=pw2;
	  detectedPattern=pw3;
	  
	  patternDistributionOverMethod.println("class name"+";;"+"Method name"+";;"+"number of detected pattern");
	  
	  detectedPattern.println("class name"+";;"+"Method name"+";;"+";;"+"Method signature"+";;"+";;"+"type of exitStmt"+";;"+"exit stmt" +";;"+"Analysed unit");
  }

  
  protected void internalTransform(Body body, String phase, Map options) {
    // body's method
    SootMethod method = body.getMethod();
    SootClass declaringClass =method.getDeclaringClass();
    Integer nbOfDetectedpatternInCurrentMethod =0;
    
    
    //initialisation du hashmap pour la distrubution des patron sur les classes 
    if (patternDistributionOverClasses.containsKey(declaringClass.getName())) {
    	
		//ne rien faire 
	} else {
		
		patternDistributionOverClasses.put(declaringClass.getName(),0);

	}    
       
    out.println("$classe name ----> "+declaringClass.getName());
    
    out.println("@@@@@@@@@@@@@@@@@@@@@@ Debut instrumenting method @@@@@@@@@@@@@@@@@@@@@@@@@");

    out.println("instrumenting method : " + method.getSignature());
    
    UnitGraph cfg = new ExceptionalUnitGraph(body);
   
    out.println("1 ############### Debut unit of the method ###############");
    
    for (Unit unitOfTheBodyMethod : cfg) {
    	
    	out.println("-----unit of the method----> "+unitOfTheBodyMethod);
		
	}
     out.println("1 ############### Fin unit of the method ###############");
    
    ///////////A
    
    
     out.println("2 ############### Debut traitement des variables et de paramètres ###############");
    
    
    Chain<Local> localsOfTheBody =body.getLocals();
   
     out.println("----------local of the body :------------>");
    
    for (Local local : localsOfTheBody) {
    	
    	 out.println("*-------->"+local.toString());
    	
	}
    
     out.println("----------Parameter of the method :------------>");

     out.println("la methode a " + method.getParameterCount()+ " parametre ");
    
   
    ArrayList<Local> methodParameterChain =  new ArrayList<Local>();
    
    for (int j = 0; j < method.getParameterCount(); j++) {
		
    	 out.println("**-------->"+body.getParameterLocal(j)) ;
    	methodParameterChain.add(body.getParameterLocal(j));
    	    	  	
	}
    
     out.println("2 ############### Fin traitement des variables et de paramètres ###############");
    
     out.println("@@@@@@@@@@@@@@@@@@@@@@ Fin instrumenting method @@@@@@@@@@@@@@@@@@@@@@@@@");
    ///////////A
    
    
    
    
    

    ////////////////1.1
     out.println("@@@@@@@@@@@@@@@@@@@@@@ start pattern detection @@@@@@@@@@@@@@@@@@@@@@@@@");

    List<Unit> exitpointlist=cfg.getTails();
    
    
   
    
    
    
    for (Unit unit4 : cfg) {
    	
    	if (unit4 instanceof soot.jimple.IfStmt ){
    		
    		
    		
    		
    		Boolean  conditionRefersTomethodParameter =false;
    		Boolean conditionContainsNullVerification =false;
    		
    		Unit targeSucessorOfStmtIf = null;  		   		
    		
    		Unit theOtherSucessorOfStmtIf  = null;		
    		
    		Unit theConsideredSucessor = null;
    		
    		Boolean findExitStmtOrThroStmt=false;
    		
    		Boolean notEqualNullCondition =false;
    		
    		Boolean equalNullCondition =false;
    		Boolean exitStmtDetectedForCurrentIfStmt=false;
    		
    		int PathLength=0;
    		
    		 
    	//verifier que la condition porte sur un des paramètre de la methode
 	

  		  List<ValueBox> vb = unit4.getUseBoxes();
      	  
  		  for (ValueBox valueBox : vb) {
      		        		  
      		  	if (methodParameterChain.contains(valueBox.getValue())) {
      		  		
      		  		conditionRefersTomethodParameter=true;
      		  		 out.println("5----------la Value------> "+valueBox.getValue()+ " existe dans les methodParameterChain  ");
      		  	}
      		        		  
      	  }
    		
    		
    		
    		
    		
    		ConditionExpr Condtionexpr =(ConditionExpr)((soot.jimple.IfStmt) unit4).getCondition();
    		 
    		//verifier que la condition et du type null verification  
    		
    		
    		if (Condtionexpr.getOp2().toString() == "null") {
    			
    			conditionContainsNullVerification=true;
				
			}
    		
    		//debut de la detection 
    		
    		
    		
    	if (conditionRefersTomethodParameter) {
				
			    		
    		if (conditionContainsNullVerification) {
			    		 
    		 if (Condtionexpr.getOp2().toString() == "null" && Condtionexpr.getSymbol() == " == "  ) {
    			 
    			 equalNullCondition=true;
   			
   		} 
       	  

       	  if (Condtionexpr.getOp2().toString() == "null" && Condtionexpr.getSymbol() == " != "  ) {
       		  
       		  
       		notEqualNullCondition=true;
       		  
   			
   		} 
       	  
       	  if (equalNullCondition && notEqualNullCondition) {
       		  
       		  try {
				throw new Exception("contradiction equalNullCondition and  notEqualNullCondition are true ");
			} catch (Exception e) {
				e.printStackTrace();
			}
       		  
			
		}
       	  
       	  
       	  
       	targeSucessorOfStmtIf = ((soot.jimple.IfStmt) unit4).getTarget();
		List<Unit> successorListOfUnit = cfg.getSuccsOf(unit4);
		

  	  	  for (Unit sucessorunit : successorListOfUnit) {
  	  		  
  	  		  if (!sucessorunit.equals(targeSucessorOfStmtIf)) {
  	  			  
  	  			theOtherSucessorOfStmtIf =sucessorunit;
					
				}
    		
  	  		  
  	  	  }
  	  	  
  	  	  
  	  	  
  	  	  if (equalNullCondition) {
  	  		  
  	  		theConsideredSucessor=targeSucessorOfStmtIf;
			
		} else if (notEqualNullCondition) {
			
			
			theConsideredSucessor=theOtherSucessorOfStmtIf;
			
		}  	  		  
  	  		
  	  	  
  	  	  //verifier l'existanse de exit stmt apres le if 
  	  	  
  	  	  out.println("#######        recherech de exit stmt qui suit iftmt               ######");  	  
  	  	  if (exitpointlist.contains(theConsideredSucessor)) {
			
  	  		  exitStmtDetectedForCurrentIfStmt=true;
  	  		   out.println(">>>>>--------on a trouver le exitpoint stmt qui suit le if stmt donc detection du pattern------->>>>   "+theConsideredSucessor  +" suit " + unit4 );
  	  		   nbOfDetectedpatternInCurrentMethod++;
  	  		   
  	  		  detectedPattern.println(declaringClass+";;"+method.getName()+";;"+";;"+method.getSignature()+";;"+";;"+"exitpoin"+";;"+theConsideredSucessor +";;"+unit4);

  	  		  
  	  		  if (theConsideredSucessor instanceof ReturnStmt) {
    		
  	  			   out.println("@-----------exitpouintunit instens Of ReturnStmt  ------------------->"+theConsideredSucessor);
			
  	  		  	} else if (theConsideredSucessor instanceof ReturnVoidStmt) {
  				
  	  		  		 out.println("@-----------exitpouintunit instens Of ReturnVoidStmt------------------->"+theConsideredSucessor);

  				
  	  		  		} else {
  	  				
  	  		  			 out.println("@-----------exitpouintunit non roconue ca peut etre un throw stmt si c'est le cas il serat detecter par la boucle while qui vient juste aprés ------------------->"+theConsideredSucessor);
  	  				
  	  		  		}
  	  		  
  	  		  
  	  		  
		}
  	  	  
  	  	  
  	  	  
  	  	  
  	  	  
  	  	  //verifier l'existance du throw apres le if 
  	  	  
  	  	  // on pet verifier l'existance de throw stmt mais on peut le cherche et si on le trouve pas au bout de 4 pas on abandonne    
  	  		
  	  	  out.println("#######        recherech de throw qui suit iftmt               ######");
  	  	  
  	  	 	if (!exitStmtDetectedForCurrentIfStmt) {
  	  	 		
  	  	 		//si on a pa trouver exitStmt qui suit le if on passe à la detection du throw qui suit le if 
				
  	  	 		Boolean mustExitWhileLoop =false;
  	  	 		
			
    		while (PathLength < 4 && !findExitStmtOrThroStmt && !mustExitWhileLoop) {
    			
    			if (theConsideredSucessor instanceof ThrowStmt ) {
    				
    				findExitStmtOrThroStmt=true;
    				 out.println(">>>>>--------on a trouver le throw stmt qui suit le if stmt donc detection du pattern------->>>>   "+theConsideredSucessor  +" suit " + unit4 );
    				 nbOfDetectedpatternInCurrentMethod++;
					
    				  detectedPattern.println(declaringClass+";;"+method.getName()+";;"+";;"+method.getSignature()+";;"+";;"+"ThrowStmt"+";;"+theConsideredSucessor +";;"+unit4);

    				 
    				 
				}else if (exitpointlist.contains(theConsideredSucessor)) {
					
					findExitStmtOrThroStmt=true;
					 out.println(">>>>>--------+on a trouver exitpoint suit le if stmt mais  cet exit stmt n'est pas immediatemet à la suit du if  donc on ne dois pas le considérer------->>>>   "+theConsideredSucessor +" suit " + unit4);
				
				}else if (theConsideredSucessor instanceof soot.jimple.IfStmt) {
					
					
					//on dois sortire du while sans faire return pour paser à la prochaine ifstmt du cfg 
					mustExitWhileLoop= true;
					
				}else{
					
					
					List<Unit> succesorlist =cfg.getSuccsOf(theConsideredSucessor);
					if (succesorlist.size()== 0) {
						 out.println(">>>>>--------pas de succesor  ------->>>>   "+ theConsideredSucessor);
						mustExitWhileLoop= true;//on dois sortire du while sans faire return pour paser à la prochaine ifstmt du cfg 
					} else if (succesorlist.size()== 1) {
						
						
						theConsideredSucessor=succesorlist.get(0);
						PathLength++;
					}else if (succesorlist.size() > 1) {
						
						 out.println(">>>>>--------succesorlist.size() > 1  unhandled case  ------->>>>   "+ theConsideredSucessor);
						mustExitWhileLoop= true;//on dois sortire du while sans faire return pour paser à la prochaine ifstmt du cfg
					}
					
					
				}     			
    			
				
			}
    		
    		}
    		
    		
		} else {
			
			 out.println(">>>>>--------ce type de if stmt n'est pas considéré  car il ne contient pas de null verification ------->>>>   "+ unit4);

		}
    		 
    		 
    		 
    		} else {
    			
    			
    			 out.println(">>>>>--------ce type de if stmt n'est pas considéré  la condition ne porte pas sur  sur un des paramètre de la methode ------->>>>   "+ unit4);

			}

    		 
    		
    	
    		
    	}
    	
    	
    }
    
     out.println("@@@@@@@@@@@@@@@@@@@@@@ End pattern detection @@@@@@@@@@@@@@@@@@@@@@@@@");
     
     
     //mise à jour du nombre de patron detecter pour la classe 
     
     
	  patternDistributionOverMethod.println(declaringClass+";;"+method.getSignature()+";;"+nbOfDetectedpatternInCurrentMethod);

     
     
     if (nbOfDetectedpatternInCurrentMethod > 0) {
    	 
    
    	 Integer nbOfDetectedpatternIndeclaringClass = patternDistributionOverClasses.get(declaringClass.getName());
    	 
    	 nbOfDetectedpatternIndeclaringClass += nbOfDetectedpatternInCurrentMethod;
    	 
    	 patternDistributionOverClasses.put(declaringClass.getName(), nbOfDetectedpatternIndeclaringClass);
		
	}    
    
    
    
    
    
    
    

  }
  
public void statistique() throws FileNotFoundException{
	
	PrintWriter patternDistributionOverClass= new PrintWriter(".\\statistique\\P1\\JHotDraw7.0.6\\patternDistributionOverClass.csv");  
	
	patternDistributionOverClass.println("class name"+";;"+"number of detected pattern");

	int i =0;
	int j=0; 
	System.out.println("___________________Statistique________________");

	
Set<java.util.Map.Entry<String, Integer>> setEntry = patternDistributionOverClasses.entrySet();

for (java.util.Map.Entry<String, Integer> entry : setEntry) {
	
	
	
	System.out.println(j+"la clase ---> "+entry.getKey()+" nbpatern ----> "+entry.getValue());
	patternDistributionOverClass.println(entry.getKey()+";;"+entry.getValue());

	i+=entry.getValue();
	j++;
}

System.out.println(" ___________________Fin Statistique_______________avec nb totl de patron :_"+i);

patternDistributionOverClass.close();
} 
  
}
