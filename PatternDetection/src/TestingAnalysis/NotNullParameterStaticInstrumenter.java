package TestingAnalysis;

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
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.util.Chain;

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

	public NotNullParameterStaticInstrumenter(/* PrintWriter pw1, PrintWriter pw2,	PrintWriter pw3 */) {

	}

	/*
	 * internalTransform goes through a method body and inserts counter
	 * instructions before an INVOKESTATIC instruction
	 */
	protected void internalTransform(Body body, String phase, Map options) {
		// body's method

		
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		
		SootMethod method = body.getMethod();
		System.out.println("methodSignature----->"+method.getSignature());
		System.out.println("methodModifiers----->"+method.getModifiers());
		
		if (method.getModifiers()==soot.Modifier.PRIVATE) {
			
			System.out.println("methodModifiers----->PRIVATE"+ soot.Modifier.isPrivate(method.getModifiers()));
		}
		
		
		System.out.println("method.Declaration----->"+method.getDeclaration());
		
		System.out.println("method.Source----->"+method.getSource());
		

		SootClass declaringClass = method.getDeclaringClass();
	 //  /*
		
		
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
			
			
			System.out.println("-------->methodParameter:    "+l );
			
		}
		
		
Iterator<Unit> units =cfg.iterator();
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			System.out.println("-------->unit:    "+unit);
			if( unit instanceof AssignStmt ){
				
				AssignStmt astmt = (AssignStmt) unit;
	            Value rhs = astmt.getRightOp();
	            Value lhs = astmt.getLeftOp();
				
	            if( rhs instanceof CastExpr ) {
	            	 CastExpr cast = (CastExpr) rhs;
	            	 Type castType = cast.getCastType();
	            	//System.out.println("*-------->cast op:    "+	cast.getOp());
	            //	System.out.println("*-------->castType:    "+	castType);
	            	
	            }
				
			} 
			
		}
		
		
		
		Chain locals = ((UnitGraph)cfg).getBody().getLocals();
        List<Local> refLocals = new ArrayList<Local>();
        for( Iterator lIt = locals.iterator(); lIt.hasNext(); ) {
            final Local l = (Local) lIt.next();
            if( l.getType() instanceof RefType ) {
                
            	
            	System.out.println("-------->RefType:    "+l);
            }
            
            if( l.getType() instanceof RefLikeType ) {
                
            	
            	System.out.println("-------->RefLikeType:    "+l);
            }
            
        }
        
        
        
        Type t= new Type() {
			
			@Override
			public String toString() {
				// TODO Auto-generated method stub
				return null;
			}
		} ;
        RefType rlt = new RefType(null);
        
        //rlt=t; //rlt=(RefType)t;
        t=rlt;
		
/*		
		
		Iterator<Unit> units =cfg.iterator();
		
		while (units.hasNext()) {
			Unit unit = (Unit) units.next();
			
			
			
			System.out.println("-------->unit:    "+unit);
			
			
			
		
			
			
			
		}

		Iterator<Unit> units2 =cfg.iterator();
		
		while (units2.hasNext()) {
			Unit unit = (Unit) units2.next();
			Stmt stmt = (Stmt)unit ;
			
			
			
			if (stmt.containsArrayRef()) {
				System.out.println("*******");
				ArrayRef arrayRef = stmt.getArrayRef();
				Value array = arrayRef.getBase();
				
				
				System.out.println("arrayRef:    "+arrayRef);
				System.out.println("Base must be non be null:    "+array);
			}
			
			
			if(stmt.containsFieldRef()) {
				System.out.println("######");
				
				System.out.println("stmt:    "+stmt);
				
				FieldRef fieldRef = stmt.getFieldRef();
				if(fieldRef instanceof InstanceFieldRef) {
				InstanceFieldRef instanceFieldRef = (InstanceFieldRef) fieldRef;
				//here we know that the receiver must point to an object
				Value base = instanceFieldRef.getBase();
				
				System.out.println("fieldRef:    "+fieldRef);
				System.out.println("the receiver  must be non null:    "+base);
				}
			}
			
			
			
			if(stmt.containsInvokeExpr()) {
				
				System.out.println("@@@@@@");
				
				System.out.println("stmt:    "+stmt);
				
				
				InvokeExpr invokeExpr = stmt.getInvokeExpr();
				
				if(invokeExpr instanceof InstanceInvokeExpr) {
					InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
					//here we know that the receiver must point to an object
					Value base = instanceInvokeExpr.getBase();
					System.out.println("invokeExpr:    "+invokeExpr);
					System.out.println("the receiver  must be non null:    "+base);
					
				}
				
				
			}
			
			
			//in case of a monitor statement, we know that if it succeeds, we have a non-null value
			if(stmt instanceof MonitorStmt) {
				System.out.println("MMMMMMMM");
				MonitorStmt monitorStmt = (MonitorStmt) stmt;
				
				
				
				System.out.println("monitorStmt:    "+monitorStmt);
				System.out.println("the op must be non null:    "+monitorStmt.getOp());
				
				
				
				
			}
			
					
			
			if(stmt instanceof ThrowStmt){
				
				ThrowStmt throwStmt = (ThrowStmt) stmt;
				
				Value theexceptionop =throwStmt.getOp();
				
				// TODO  utiliser getDefsOfAt(Local l, Unit s) de SmartLocalDefs pour trouver l'uniter qui a fait l'afectationde l'operateur et prendre la deuxieme partie de cette affectaion qui represente l'exeption                       theexceptionop.get
				
				System.out.println("ThrowStmt:    "+stmt);
				System.out.println("the op is :    "+throwStmt.getOp() );
				
				
				
			}
			
			
			
		}

		
		
		
//		*/
		
			
		
	}

	
	
	
	

	
	


}
