package MethodParameterTypeRestrictionPattern;

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
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LocalUnitPair;
import soot.toolkits.scalar.SimpleLocalDefs;
import stat.PatternOccurrenceInfo;
import stat.Statistique;

public class TypeRestrictionStaticInstrumenter extends BodyTransformer {

	static {
		restrictedparameterTypePaternDistributionOverClasses = new HashMap<String, Integer>(
				350);
		nullAllowedPaternDistributionOverClasses = new HashMap<String, Integer>(
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
	static HashMap<String, Integer> nullAllowedPaternDistributionOverClasses;
	static HashMap<String, Integer> restrictedparameterTypePaternDistributionOverClasses;
	static ArrayList<Local> methodParameterChain;
	static String satistuquePath;
	static int nbMethode;

	public TypeRestrictionStaticInstrumenter(String satatPath) {

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

		System.out.println("instrumenting method : " + method.getSignature()
				+ "    in class  " + declaringClass.getName());

		Integer nbOfDetectedpatternInCurrentMethod = 0;

		UnitGraph cfg = new ExceptionalUnitGraph(body);

		methodParameterChain = new ArrayList<Local>();

		for (int j = 0; j < method.getParameterCount(); j++) {

			methodParameterChain.add(body.getParameterLocal(j));

		}

		for (Local l : methodParameterChain) {

			System.out.println("-------->methodParameter:    " + l);
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

			// lacer les tested parameter et les modified nullnessAnalysis

			TypeRestrictionAnalysis typeRestrictionAnalysis = new TypeRestrictionAnalysis(
					cfg);

			// detectRestrictedTypeOnAllUnit(cfg, typeRestrictionAnalysis);

			Map<Local, ArrayList<PatternOccurrenceInfo>> UnitCausingParameterRestrictedType = detectRestrictedTypePatern(
					cfg, localDefinedUsingParameterToParameter,
					typeRestrictionAnalysis);

		}

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

	/*
	 * getLocalsDefinedUsingParameter1 recupairer une liste de local
	 * initialisetr à partir de paramaitre linitialisation et de tout forme il
	 * sufit que dans la parti droite un paramaitre existe pour que on recupaire
	 * la variable dans la partie gauche danc les invocation de methode qui
	 * utiolise un paramaitre font partie des initialisation prise en charge
	 */

	private ArrayList<Local> getLocalsDefinedUsingParameter1(UnitGraph cfg) {

		ArrayList<Local> LocalsDefinedUsingParameterList = new ArrayList<Local>();
		Map<Local, Local> localToParameter = new HashMap<Local, Local>(
				cfg.size() * 2 + 1, 0.7f);
		Iterator<Unit> units = cfg.iterator();

		while (units.hasNext()) {
			Unit unit = (Unit) units.next();

			List defBoxes = unit.getDefBoxes();
			if (!defBoxes.isEmpty()) {

				if (!(defBoxes.size() == 1))
					throw new RuntimeException(
							"SimpleLocalDefs: invalid number of def boxes");
				Value value = ((ValueBox) defBoxes.get(0)).getValue();

				if (value instanceof Local) {

					Local defLocal = (Local) value;
					List<ValueBox> vb = unit.getUseBoxes();

					for (ValueBox valueBox : vb) {

						if (methodParameterChain.contains(valueBox.getValue())) {

							localToParameter.put(defLocal,
									(Local) valueBox.getValue());
							System.out
									.println("*----l'unité --> "
											+ unit
											+ " -----utilise un parametre----la Value------> "
											+ valueBox.getValue()
											+ " --------qui existe dans les methodParameterChain  ");

							LocalsDefinedUsingParameterList.add(defLocal);

							// on peut vour la posibilité de fair un map qui
							// contien les unit et les les local initialiser
							// apartir d'un parametre dans cett unit
						}

					}
				}

			}

		}

		Set<Local> LocalsDefinedUsingParameterset = localToParameter.keySet();
		return LocalsDefinedUsingParameterList;

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Map<Local, ArrayList<PatternOccurrenceInfo>> detectRestrictedTypePatern(
			UnitGraph cfg,
			Map<Local, Local> localDefinedUsingParameterToParameter,
			TypeRestrictionAnalysis typeRestrictionAnalysis) {

		if (restrictedparameterTypePaternDistributionOverClasses
				.containsKey(cfg.getBody().getMethod().getDeclaringClass()
						.getName())) {

			// ne rien faire
		} else {

			restrictedparameterTypePaternDistributionOverClasses.put(cfg
					.getBody().getMethod().getDeclaringClass().getName(), 0);

		}

		Map<Local, ArrayList<PatternOccurrenceInfo>> temporaryUnitCausingParameterRestrictedType = new HashMap<Local, ArrayList<PatternOccurrenceInfo>>(
				methodParameterChain.size() * 2 + 1, 0.7f);
		;
		Map<Local, ArrayList<PatternOccurrenceInfo>> permanentUnitCausingParameterRestrictedType = new HashMap<Local, ArrayList<PatternOccurrenceInfo>>(
				methodParameterChain.size() * 2 + 1, 0.7f);
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

		for (Local l : methodParameterChain) {

			Unit lastUnit = null;
			ArrayList<PatternOccurrenceInfo> ListOfPatternOccurrenceInfo = new ArrayList<PatternOccurrenceInfo>();

			Boolean weFindReturnStmtThroughWhichParameterTypeIsNotRestrected = false;
			Boolean weFindReturnStmtThroughWhichParameterTypeIsRestrected = false;

			Iterator<Unit> exitpointIt = exitpointlist.iterator();

			while (exitpointIt.hasNext()
					&& !weFindReturnStmtThroughWhichParameterTypeIsNotRestrected) {

				Unit ExitUnit = (Unit) exitpointIt.next();

				if (ExitUnit instanceof ReturnVoidStmt) {

					if ((typeRestrictionAnalysis.isAlwaysRestrictedBefore(
							ExitUnit, l))
							|| (typeRestrictionAnalysis
									.isBothRestrictedAndUnRestrictedBefore(
											ExitUnit, l))) {
						weFindReturnStmtThroughWhichParameterTypeIsRestrected = true;

						List<Type> rt = typeRestrictionAnalysis
								.getRestrictionTypesBefore(ExitUnit, l);

						/*
						 * faire la diference entre les risque de error lorsque
						 * la restriction et sur tous les chemin et les warning
						 * lorsque il ya eu une utulisation sans restriction sur
						 * une des branches
						 */

						String coment = "pattern detected param  " + l;
						String type1 = "";
						if (typeRestrictionAnalysis
								.isBothRestrictedAndUnRestrictedBefore(
										ExitUnit, l)) {

							coment += " maybe type Restricted  it can be ";
							type1 = "Warning: Restriction for param  ReturnVoidStmt";
						} else if (typeRestrictionAnalysis
								.isAlwaysRestrictedBefore(ExitUnit, l)) {

							coment += " is type Restricted  it can be ";
							type1 = "Error risk: Restriction for param  ReturnVoidStmt";
						}

						if (rt.size() != 0) {

							Iterator<Type> it = rt.iterator();

							while (it.hasNext()) {
								Type type = (Type) it.next();
								coment += ", " + type;

							}

						}

						PatternOccurrenceInfo poi = new PatternOccurrenceInfo(
								ExitUnit, type1, coment);
						temporaryUnitCausingParameterRestrictedType = updateUnitCausingNullsNotAllowed(
								temporaryUnitCausingParameterRestrictedType,
								(Local) l, poi);

						lastUnit = ExitUnit;

					} else {

						weFindReturnStmtThroughWhichParameterTypeIsNotRestrected = true;

					}

				} else if (ExitUnit instanceof ReturnStmt) {

					if (typeRestrictionAnalysis.isAlwaysRestrictedBefore(
							ExitUnit, l)
							|| (typeRestrictionAnalysis
									.isBothRestrictedAndUnRestrictedBefore(
											ExitUnit, l))) {
						weFindReturnStmtThroughWhichParameterTypeIsRestrected = true;

						List<Type> rt = typeRestrictionAnalysis
								.getRestrictionTypesBefore(ExitUnit, l);

						/*
						 * faire la diference entre les risque de error lorsque
						 * la restriction et sur tous les chemin et les warning
						 * lorsque il ya eu une utulisation sans restriction sur
						 * une des branches
						 */

						String coment = "pattern detected param  " + l;
						String type2 = "";
						if (typeRestrictionAnalysis
								.isBothRestrictedAndUnRestrictedBefore(
										ExitUnit, l)) {

							coment += " maybe type Restricted  it can be ";
							type2 = "Warning: Restriction for param  ReturnStmt";
						} else if (typeRestrictionAnalysis
								.isAlwaysRestrictedBefore(ExitUnit, l)) {

							coment += " is type Restricted  it can be ";
							type2 = "Error risk: Restriction for param  ReturnStmt";
						}

						if (rt.size() != 0) {

							Iterator<Type> it = rt.iterator();

							while (it.hasNext()) {
								Type type = (Type) it.next();
								coment += ", " + type;

							}

						}

						PatternOccurrenceInfo poi = new PatternOccurrenceInfo(
								ExitUnit, type2, coment);
						temporaryUnitCausingParameterRestrictedType = updateUnitCausingNullsNotAllowed(
								temporaryUnitCausingParameterRestrictedType,
								(Local) l, poi);

						lastUnit = ExitUnit;

					} else {

						weFindReturnStmtThroughWhichParameterTypeIsNotRestrected = true;

					}

				} else if (ExitUnit instanceof ThrowStmt) {

					/*
					 * TODO on peu faire un traitement avec les graphe de
					 * dominance pour voir si l'exception et en relation avec la
					 * restriction de type
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

			// verification si le param is restrected dans tous les return alor
			// detection du patern

			if (weFindReturnStmtThroughWhichParameterTypeIsRestrected
					&& !weFindReturnStmtThroughWhichParameterTypeIsNotRestrected) {

				if (lastUnit != null) {

					List<Type> rt = typeRestrictionAnalysis
							.getRestrictionTypesBefore(lastUnit, l);

					String comment = "pattern detected param  " + l
							+ " is type Restricted  it can be ";

					if (rt.size() != 0) {

						Iterator<Type> it = rt.iterator();

						while (it.hasNext()) {
							Type type = (Type) it.next();
							comment += ", " + type;

						}

					}

					System.out.println("#" + comment);
					permanentUnitCausingParameterRestrictedType.put(l,
							temporaryUnitCausingParameterRestrictedType.get(l));

				}

			}

		}

		Statistique Statistique = new Statistique();

		if (permanentUnitCausingParameterRestrictedType.keySet().size() > 0) {

			String className = cfg.getBody().getMethod().getDeclaringClass()
					.getName();

			// used for Statistique patternDistributionOverClasses
			// ParameterRestrictedType

			Integer nbOfParameterRestrictedTypePaternIndeclaringClass = restrictedparameterTypePaternDistributionOverClasses
					.get(className);

			nbOfParameterRestrictedTypePaternIndeclaringClass += permanentUnitCausingParameterRestrictedType
					.keySet().size();

			restrictedparameterTypePaternDistributionOverClasses.put(className,
					nbOfParameterRestrictedTypePaternIndeclaringClass);

			// Statistique patternDistributionOverMethod ParameterRestrictedType
			String methodName = cfg.getBody().getMethod().getName();
			String methodDeclaration = cfg.getBody().getMethod()
					.getDeclaration();
			String statfileName = "\\RestrictedParameterTypePatternDistributionOverMethod.csv";
			String OccurrenStatfileName = "\\RestrictedParameterTypePatternOccurrenInMethod.csv";

			try {
				Statistique.statistiqueForPatternDistributionOverMethod(
						permanentUnitCausingParameterRestrictedType.keySet()
								.size(), className, methodName,
						methodDeclaration, satistuquePath, statfileName);
				Statistique.statistiqueForPatternOccurrenInMethod(
						permanentUnitCausingParameterRestrictedType.keySet()
								.size(), className, methodName,
						methodDeclaration, satistuquePath,
						OccurrenStatfileName,
						permanentUnitCausingParameterRestrictedType);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return permanentUnitCausingParameterRestrictedType;

	}

	private void detectRestrictedTypeOnAllUnit(UnitGraph cfg,
			TypeRestrictionAnalysis typeRestrictionAnalysis) {

		Iterator<Unit> unitIt = cfg.iterator();

		while (unitIt.hasNext()) {
			Unit unit = (Unit) unitIt.next();

			List<ValueBox> useAndDefBox = unit.getUseAndDefBoxes();

			ArrayList<Local> useAndDefValnInCurrentUnit = new ArrayList<Local>();

			for (ValueBox valueBox : useAndDefBox) {

				if (valueBox.getValue() instanceof Local) {

					useAndDefValnInCurrentUnit.add((Local) valueBox.getValue());

				}

			}

			System.out.println("-------->unit:    " + unit);
			for (Local l : methodParameterChain) {

				System.out.println("---"
						+ l
						+ "----->isAlwaysRestrictedBefore:    "
						+ typeRestrictionAnalysis.isAlwaysRestrictedBefore(
								unit, l));
				if (typeRestrictionAnalysis.isAlwaysRestrictedBefore(unit, l)) {

					List<Type> rt = typeRestrictionAnalysis
							.getRestrictionTypesBefore(unit, l);

					if (rt.size() != 0) {

						Iterator<Type> it = rt.iterator();

						while (it.hasNext()) {
							Type type = (Type) it.next();
							System.out.println("---" + l + "----->can be:  "
									+ type);

						}

					}

				}

				System.out.println("---"
						+ l
						+ "----->isAlwaysNonRestrictedBefore:    "
						+ typeRestrictionAnalysis.isAlwaysNonRestrictedBefore(
								unit, l));

			}

			if (unit instanceof JIfStmt) {
				JIfStmt ifStmt = (JIfStmt) unit;
				Value condition = ifStmt.getCondition();

				if (condition instanceof JInstanceOfExpr) {

					System.out
							.println("-------->condition Type:   instanceof JInstanceOfExpr ");
				} else {

					System.out
							.println("-------->condition Type:  not instanceof JInstanceOfExpr  ");

				}

			}

		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////:

	private Map<Local, ArrayList<PatternOccurrenceInfo>> updateUnitCausingNullsNotAllowed(
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

	public HashMap<String, Integer> getRestrictedParameterTypePaternDistributionOverClasses() {
		return restrictedparameterTypePaternDistributionOverClasses;
	}

	public int getNumberOfAnalyzedMethod() {

		return nbMethode;
	}

}
