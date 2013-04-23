package MethodParameterRangeLimitationPattern;

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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import NullOrNotNullParamUsingModifiedNullnessAnalysis.ModifiedNullnessAnalysis;
import NullOrNotNullParamUsingModifiedNullnessAnalysis.NullTestedLocalsAnalysis;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceOfExpr;
import soot.jimple.internal.JNeExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LocalUnitPair;
import soot.toolkits.scalar.SimpleLocalDefs;
import stat.PatternOccurrenceInfo;
import stat.Statistique;

public class RangeLimitationwithOneRangeStaticInstrumenter extends
		BodyTransformer {

	static {
		limitedParameterRangePaternDistributionOverClasses = new HashMap<String, Integer>(
				350);

		nbMethode = 0;
	}

	/*
	 * la classe PatternOccurrenceInfo est utiliser pour remplir un map qui
	 * representra pour chaque param qui ne doit pas etre nulll(le key du map )
	 * l'ensemble des d'iferentes ocurence des instruction qui impose que le
	 * param ne dois pas etre null
	 * 
	 * cette structure a pour nom unitCausingNullsNotAllowed c'est un map son
	 * key est le param et son value est une liste de PatternOccurrenceInfo
	 * 
	 * je vouler donner un ordre dans cet list talque le elemet corespond au
	 * type 1 et elem2 type 2 mais
	 * 
	 * plusieur occurence du maime tupe peuvent exister
	 */

	/* some internal fields */

	static PrintWriter out;
	static PrintWriter patternDistributionOverMethod;
	static PrintWriter detectedPattern;
	static HashMap<String, Integer> limitedParameterRangePaternDistributionOverClasses;
	static ArrayList<Local> methodParameterChain;
	static ArrayList<Local> numericParameterChain;
	static String satistuquePath;
	static int nbMethode;

	public RangeLimitationwithOneRangeStaticInstrumenter(String satatPath) {

		satistuquePath = satatPath;

	}

	/*
	 * internalTransform goes through a method body and inserts counter
	 * instructions before an INVOKESTATIC instruction
	 */
	protected void internalTransform(Body body, String phase, Map options) {
		// body's method

		SootMethod method = body.getMethod();
		nbMethode++;
		SootClass declaringClass = method.getDeclaringClass();
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		String methodSignature =method.getSignature();
		System.out.println("instrumenting method : " + methodSignature
				+ "    in class  " + declaringClass.getName());

		//"<java.net.URL: void set(java.lang.String,java.lang.String,int,java.lang.String,java.lang.String)>"
		//"<java.net.URL: void checkSpecifyHandler(java.lang.SecurityManager)>"
		if (methodSignature == "<java.net.URL: void set(java.lang.String,java.lang.String,int,java.lang.String,java.lang.String)>") {
			System.out.println("BreakPoint");
		}
		
		Integer nbOfDetectedpatternInCurrentMethod = 0;

		UnitGraph cfg = new ExceptionalUnitGraph(body);
		


		methodParameterChain = new ArrayList<Local>();

		for (int j = 0; j < method.getParameterCount(); j++) {

			methodParameterChain.add(body.getParameterLocal(j));

		}

		Iterator<Unit> units = cfg.iterator();

		while (units.hasNext()) {
			Unit unit = (Unit) units.next();

			System.out.println("-------->unit:    " + unit);

		}

		Map<Local, Local> localDefinedUsingParameterToParameter = new HashMap<Local, Local>(
				cfg.size() * 2 + 1, 0.7f);

		localDefinedUsingParameterToParameter = getLocalsDefinedUsingParameter(cfg);
		Set<Local> LocalsDefinedUsingParameterset = localDefinedUsingParameterToParameter
				.keySet();

		// ajout de une condition pour traiter uniquement les methode public et
		// eviter les accessor creer par le compilmateur donc qui contienne $
		// dans leur nom

		boolean methodIsPrivate = soot.Modifier
				.isPrivate(method.getModifiers());
		boolean MethodCreatedBycompiler = method.getName().contains("$");

		if (!methodIsPrivate && !MethodCreatedBycompiler) {

			

			// construire la liste des paramaitre numirique
			 numericParameterChain = new ArrayList<Local>();

			 
			 
			//List<String> consideredType = rangeLimitationAnalysis.getConsideredType();
			 	 
			 List<String> consideredType = new ArrayList<String>();
				consideredType.add("int");
				consideredType.add("byte");
				consideredType.add("short");
				
			for (Local l : methodParameterChain) {

				if (consideredType.contains(l.getType().toString())) {

					numericParameterChain.add(l);

				}

			}

			if (numericParameterChain.size()!=0) {
				
				// lacer les tested parameter et les modified nullnessAnalysis

				RangeLimitationWithOneRangeAnalysis rangeLimitationAnalysis = new RangeLimitationWithOneRangeAnalysis(
						cfg);
	
			
				// detectRestrictedTypeOnAllUnit(cfg, typeRestrictionAnalysis);

				
				
				
				Map<Local, ArrayList<PatternOccurrenceInfo>> UnitCausingParameterRangeLimitation = detectLimitedRangePatern(
						cfg, localDefinedUsingParameterToParameter,
						rangeLimitationAnalysis);
				
				
				
				
				Iterator<Unit> units2 = cfg.iterator();

				while (units2.hasNext()) {
					Unit unit = (Unit) units2.next();

					System.out.println("-------->unit:    " + unit);

					for (Local l : numericParameterChain){
						
						
							
							System.out.println("R--- "+l+" --range--->:    " +rangeLimitationAnalysis.getRangeLimitationBefore(unit, l).get(0).toString());
							
						
						
						
					}
					
					
					
				}
				
				
				
			}
			
			
			
	
			
			
			System.out.println("FIN     instrumenting method : " + method.getSignature()
					+ "    in class  " + declaringClass.getName());	
		}

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Map<Local, ArrayList<PatternOccurrenceInfo>> detectLimitedRangePatern(
			UnitGraph cfg,
			Map<Local, Local> localDefinedUsingParameterToParameter,
			RangeLimitationWithOneRangeAnalysis rangeLimitationAnalysis) {

		if (limitedParameterRangePaternDistributionOverClasses.containsKey(cfg
				.getBody().getMethod().getDeclaringClass().getName())) {

			// ne rien faire
		} else {

			limitedParameterRangePaternDistributionOverClasses.put(cfg
					.getBody().getMethod().getDeclaringClass().getName(), 0);

		}

		Map<Local, ArrayList<PatternOccurrenceInfo>> temporaryUnitCausingParameterLimitedRange = new HashMap<Local, ArrayList<PatternOccurrenceInfo>>();
//				numericParameterChain.size() * 2 + 1, 0.7f);
		;
		Map<Local, ArrayList<PatternOccurrenceInfo>> permanentUnitCausingParameterLimitedRange = new HashMap<Local, ArrayList<PatternOccurrenceInfo>>();
//				numericParameterChain.size() * 2 + 1, 0.7f);
		;

		/*
		 * TODO les element qui sont dans
		 * temporaryunitCausingParameterRestrictedType et pas dans
		 * permanentunitCausingParameterRestrictedType sont les ellement pour
		 * les quelle il ya restriction de type mais pas dans toutes les return
		 */

		/*
		 * faire une structure pour attraper les cas ou le paramaitre is
		 * restricted but not for all the return stmt this case can be used for
		 * the corolaited parameter restricted type
		 */

		List<Unit> exitpointlist = cfg.getTails();

		for (Local l : numericParameterChain) {

			ArrayList<PatternOccurrenceInfo> ListOfPatternOccurrenceInfo = new ArrayList<PatternOccurrenceInfo>();

			Boolean weFindReturnStmtThroughWhichParameterRangeIsNotLimited = false;
			Boolean weFindReturnStmtThroughWhichParameterRangeIsLimited = false;

			DisjointedRangeList ParameterRangeListFromDifferentExitpoint = new DisjointedRangeList();

			Iterator<Unit> exitpointIt = exitpointlist.iterator();

			while (exitpointIt.hasNext()
					&& !weFindReturnStmtThroughWhichParameterRangeIsNotLimited) {

				Unit ExitUnit = (Unit) exitpointIt.next();

				if (ExitUnit instanceof ReturnVoidStmt) {

					if (rangeLimitationAnalysis.isAlreadyRangeLimitedBefore(
							ExitUnit, l)) {
						weFindReturnStmtThroughWhichParameterRangeIsLimited = true;

						List<Range> parameterRnageInReturnStmt = new ArrayList<Range>();
						parameterRnageInReturnStmt = rangeLimitationAnalysis
								.getRangeLimitationBefore(ExitUnit, l);

						Iterator<Range> paramRangeIterator = parameterRnageInReturnStmt
								.iterator();
						while (paramRangeIterator.hasNext()) {
							Range range = (Range) paramRangeIterator.next();

							ParameterRangeListFromDifferentExitpoint
									.addAndMaintainDisjunction(range);

						}

						String coment = "pattern detected the Range of param  "
								+ l + "  is limeted to : ";
						String type1 = "Range Limitation for Param in ReturnVoidStmt ";

						if (parameterRnageInReturnStmt.size() != 0) {

							Iterator<Range> it = parameterRnageInReturnStmt
									.iterator();

							while (it.hasNext()) {
								Range range = it.next();
								coment += ", " + range.toString();

							}

						}

						if (rangeLimitationAnalysis.getNbOverestimatedUnion(l) > 0) {

							coment += " with "
									+ rangeLimitationAnalysis
											.getNbOverestimatedUnion(l)
									+ "Overestimated Union";

						}

						PatternOccurrenceInfo poi = new PatternOccurrenceInfo(
								ExitUnit, type1, coment);
						temporaryUnitCausingParameterLimitedRange = updateUnitCausingLimitedRange(
								temporaryUnitCausingParameterLimitedRange,
								(Local) l, poi);

					} else {

						weFindReturnStmtThroughWhichParameterRangeIsNotLimited = true;

					}

				} else if (ExitUnit instanceof ReturnStmt) {

					if (rangeLimitationAnalysis.isAlreadyRangeLimitedBefore(
							ExitUnit, l)) {

						weFindReturnStmtThroughWhichParameterRangeIsLimited = true;

						List<Range> parameterRnageInReturnStmt = new ArrayList<Range>();
						parameterRnageInReturnStmt = rangeLimitationAnalysis
								.getRangeLimitationBefore(ExitUnit, l);

						Iterator<Range> paramRangeIterator = parameterRnageInReturnStmt
								.iterator();
						while (paramRangeIterator.hasNext()) {
							Range range = (Range) paramRangeIterator.next();

							ParameterRangeListFromDifferentExitpoint
									.addAndMaintainDisjunction(range);

						}

						String coment = "pattern detected the Range of param  "
								+ l + "  is limeted to : ";
						String type1 = "Range Limitation for Param in ReturnStmt ";

						if (parameterRnageInReturnStmt.size() != 0) {

							Iterator<Range> it = parameterRnageInReturnStmt
									.iterator();

							while (it.hasNext()) {
								Range range = it.next();
								coment += ", " + range.toString();

							}

						}

						if (rangeLimitationAnalysis.getNbOverestimatedUnion(l) > 0) {

							coment += " with "
									+ rangeLimitationAnalysis
											.getNbOverestimatedUnion(l)
									+ "Overestimated Union";

						}

						PatternOccurrenceInfo poi = new PatternOccurrenceInfo(
								ExitUnit, type1, coment);
						temporaryUnitCausingParameterLimitedRange = updateUnitCausingLimitedRange(
								temporaryUnitCausingParameterLimitedRange,
								(Local) l, poi);

					} else {

						weFindReturnStmtThroughWhichParameterRangeIsNotLimited = true;

					}

				} else if (ExitUnit instanceof ThrowStmt) {

					/*
					 * TODO on peu faire un traitement avec les graphe de
					 * dominance pour voir si l'exception et en relation avec la
					 * limitation de domaine
					 */
					System.out.println("ThrowStmt " + ExitUnit);

				} else {

					try {
						throw new Exception(
								"un exite point non treter qui est ni un ReturnVoidStmt ni un ReturnStmt c'est   "
										+ ExitUnit);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}

			/*
			 * verification si le param is Range Limited dans tous les return
			 * 
			 * et que la reunin de tout ces range dans les diferent return
			 * statment
			 * 
			 * reste diferente de ]Integer.MIN_VALUE,Integer.MAX_VALUE [
			 * 
			 * alor detection du patern
			 */
			if (weFindReturnStmtThroughWhichParameterRangeIsLimited
					&& !weFindReturnStmtThroughWhichParameterRangeIsNotLimited) {

				if ((ParameterRangeListFromDifferentExitpoint.size() != 0)) {

					String comment = "pattern detected the Range of param  "
							+ l + "  is limeted to : ";

					Iterator<Range> it = ParameterRangeListFromDifferentExitpoint
							.iterator();

					while (it.hasNext()) {
						Range range = it.next();
						comment += ", " + range.toString();

					}

					// erification % ]Integer.MIN_VALUE,Integer.MAX_VALUE [
					if (ParameterRangeListFromDifferentExitpoint.size() > 1) {

						/*
						 * par defaut les interval sont disjointcomme li sont 2
						 * ou plus donc ne coinside pas avec
						 * ]Integer.MIN_VALUE,Integer.MAX_VALUE [
						 */
						System.out.println("#" + comment);
						permanentUnitCausingParameterLimitedRange.put(l,
								temporaryUnitCausingParameterLimitedRange
										.get(l));

					} else if (ParameterRangeListFromDifferentExitpoint.size() == 1) {

						if (!ParameterRangeListFromDifferentExitpoint.get(0)
								.contains(
										rangeLimitationAnalysis.initialRange(l
												.getType()))) {

							/*
							 * dans ce cas pas le range est diferent de
							 * ]Integer.MIN_VALUE,Integer.MAX_VALUE [
							 */
							System.out.println("#" + comment);
							permanentUnitCausingParameterLimitedRange.put(l,
									temporaryUnitCausingParameterLimitedRange
											.get(l));

						} else {

							/*
							 * dans ce cas la reunion des range dans les exit
							 * point donne un range est egale à
							 * ]Integer.MIN_VALUE,Integer.MAX_VALUE [
							 * 
							 * ces un cas rare pour ne pas dire bizar je fait un
							 * throw exception pour le remarquer si il existe
							 * 
							 * il represente un cas ous chaque sous domaine
							 * corespond à un traitement
							 * 
							 * peut etre un nouveau patern
							 */

						}

					}

				}

			}

		}

		Statistique Statistique = new Statistique();

		if (permanentUnitCausingParameterLimitedRange.keySet().size() > 0) {

			String className = cfg.getBody().getMethod().getDeclaringClass()
					.getName();

			// used for Statistique patternDistributionOverClasses
			// ParameterLimitedRange

			Integer nbOfParameterLimitedRangePaternIndeclaringClass = limitedParameterRangePaternDistributionOverClasses
					.get(className);

			nbOfParameterLimitedRangePaternIndeclaringClass += permanentUnitCausingParameterLimitedRange
					.keySet().size();

			limitedParameterRangePaternDistributionOverClasses.put(className,
					nbOfParameterLimitedRangePaternIndeclaringClass);

			// Statistique patternDistributionOverMethod ParameterLimitedRange
			String methodName = cfg.getBody().getMethod().getName();
			String methodDeclaration = cfg.getBody().getMethod()
					.getDeclaration();
			String statfileName = "\\LimitedParameterRangePatternDistributionOverMethod.csv";
			String OccurrenStatfileName = "\\LimitedParameterRangePatternOccurrenInMethod.csv";

			try {
				Statistique.statistiqueForPatternDistributionOverMethod(
						permanentUnitCausingParameterLimitedRange.keySet()
								.size(), className, methodName,
						methodDeclaration, satistuquePath, statfileName);
				Statistique.statistiqueForPatternOccurrenInMethod(
						permanentUnitCausingParameterLimitedRange.keySet()
								.size(), className, methodName,
						methodDeclaration, satistuquePath,
						OccurrenStatfileName,
						permanentUnitCausingParameterLimitedRange);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return permanentUnitCausingParameterLimitedRange;

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////:

	private Map<Local, ArrayList<PatternOccurrenceInfo>> updateUnitCausingLimitedRange(
			Map<Local, ArrayList<PatternOccurrenceInfo>> inMap, Local l,
			PatternOccurrenceInfo poi) {

		Map<Local, ArrayList<PatternOccurrenceInfo>> unitCausingNullsNotAllowed = inMap;

		if (unitCausingNullsNotAllowed.containsKey(l)) {

			unitCausingNullsNotAllowed.get(l).add(poi);
			// todo verifier que la liste est mise à jour

		} else {

			ArrayList<PatternOccurrenceInfo> ListOfPatternOccurrenceInfo = new ArrayList<PatternOccurrenceInfo>();

			ListOfPatternOccurrenceInfo.add(poi);
			unitCausingNullsNotAllowed.put(l, ListOfPatternOccurrenceInfo);
		}

		return unitCausingNullsNotAllowed;

	}

	public HashMap<String, Integer> getLimitedParameterRangePaternDistributionOverClasses() {
		return limitedParameterRangePaternDistributionOverClasses;
	}

	public int getNumberOfAnalyzedMethod() {

		return nbMethode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * dans getLocalsDefinedUsingParameter on recupaire les variable initialiser
	 * à partir de paramaitre sur un seul niveau si une variable est initialiser
	 * ou definit apartir d'un paramaitre on la recupaire si une variable est
	 * initailaiser à apartir d'une variable qui elle est initaliser d'un
	 * paramitre on ne la recupaire pas on ne considaire que les definition
	 * simple pas d'invocation ni d'expression qui contiennet un paramaitre
	 */

	private Map<Local, Local> getLocalsDefinedUsingParameter(UnitGraph cfg) {

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

						if (methodParameterChain.contains(right)) {

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

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////

}
