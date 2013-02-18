package stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import NullOrNotNullParamUsingModifiedNullnessAnalysis.NotNullParameterStaticInstrumenter;
import NullOrNotNullParamUsingModifiedNullnessAnalysis.NotNullParameterStaticInstrumenter.PatternOccurrenceInfo;

public class Statistique {

	public void statistiqueForPatternDistributionOverClass(
			HashMap<String, Integer> patternDistributionOverClasses,
			String filePath, String patterName, String statistiqueLocation,
			int nbAnalyzedMethod) throws FileNotFoundException {

		String statFilePath = filePath;

		PrintWriter patternDistributionOverClass = new PrintWriter(statFilePath);

		patternDistributionOverClass.println("class name" + ";;"
				+ "number of detected pattern");

		int i = 0;
		int j = 0;
		System.out.println("___________________Statistique________________"
				+ patterName);

		Set<java.util.Map.Entry<String, Integer>> setEntry = patternDistributionOverClasses
				.entrySet();

		for (java.util.Map.Entry<String, Integer> entry : setEntry) {

			System.out.println(j + "la clase ---> " + entry.getKey()
					+ " nbpatern ----> " + entry.getValue());
			patternDistributionOverClass.println(entry.getKey() + ";;"
					+ entry.getValue());

			i += entry.getValue();
			j++;
		}

		System.out.println(" ___________________Fin Statistique_______"
				+ patterName + "________avec nb totl de patron :_" + i);

		patternDistributionOverClass.close();

		// generation des statistique generale pour le patron nb de classe nb de
		// methode nb de pattern

		{

			String filePath1 = statistiqueLocation + "\\GeneralInformation.csv";

			File file1 = new File(filePath1);

			if (!file1.exists()) {

				// dans cet partie l'apel c'est le premier apel donc creation du
				// ficier et info pour NullNotAllowed

				PrintWriter GeneralInformation = new PrintWriter(file1);
				GeneralInformation.println("number of class" + ";;"
						+ "number of Method" + ";;" + patterName + ";;"
						+ "number of detected pattern");

				GeneralInformation.println(setEntry.size() + ";;"
						+ nbAnalyzedMethod + ";;" + patterName + ";;" + i);

				GeneralInformation.close();

			} else {

				// dans cet partie l'apel c'est le 2em apel donc info pour
				// NullAllowed

				PrintWriter GeneralInformation;
				try {
					GeneralInformation = new PrintWriter(new BufferedWriter(
							new FileWriter(file1, true)));

					GeneralInformation.println("*" + ";;" + "*" + ";;"
							+ patterName + ";;" + i);
					GeneralInformation.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	public void statistiqueForPatternDistributionOverMethod(
			int nbOfDetectedpatternInCurrentMethod, String className,
			String methodName, String methodDeclaration,
			String statistiqueLocation, String file) throws IOException {

		String statistiqueLocationPath = statistiqueLocation;
		String filename = file;
		String filePath1 = statistiqueLocationPath + filename;

		File file1 = new File(filePath1);

		if (!file1.exists()) {

			PrintWriter patternDistributionOverMethod = new PrintWriter(file1);
			patternDistributionOverMethod.println("class name" + ";;"
					+ "Method name" + ";;" + "method Declaration" + ";;"
					+ "number of detected pattern");

			patternDistributionOverMethod.println(className + ";;" + methodName
					+ ";;" + methodDeclaration + ";;"
					+ nbOfDetectedpatternInCurrentMethod);
			patternDistributionOverMethod.close();

		} else {

			PrintWriter patternDistributionOverMethod = new PrintWriter(
					new BufferedWriter(new FileWriter(file1, true)));
			patternDistributionOverMethod.println(className + ";;" + methodName
					+ ";;" + methodDeclaration + ";;"
					+ nbOfDetectedpatternInCurrentMethod);

			patternDistributionOverMethod.close();

		}

	}

	public void statistiqueForPatternOccurrenInMethod(
			int nbOfDetectedpatternInCurrentMethod,
			String className,
			String methodName,
			String methodDeclaration,
			String statistiqueLocation,
			String file,
			Map<Local, ArrayList<PatternOccurrenceInfo>> unitCausingNullsNotAllowed)
			throws IOException {

		String statistiqueLocationPath = statistiqueLocation;
		String filename = file;
		String filePath1 = statistiqueLocationPath + filename;

		File file1 = new File(filePath1);

		if (!file1.exists()) {

			PrintWriter patternDistributionOverMethod = new PrintWriter(file1);
			patternDistributionOverMethod.println("class name" + ";;"
					+ "Method name" + ";;" + "number of detected pattern"
					+ ";;" + "method Declaration" + ";;" + "parameter" + ";;"
					+ "number of occurrences" + ";;" + "occurrenceType" + ";;"
					+ "unitOnwhichOccurrenceIsDetected" + ";;"
					+ "commentFragment");

			patternDistributionOverMethod.println(className + ";;" + methodName
					+ ";;" + nbOfDetectedpatternInCurrentMethod + ";;"
					+ methodDeclaration);

			Iterator<Local> parameters = unitCausingNullsNotAllowed.keySet()
					.iterator();

			while (parameters.hasNext()) {
				Local param = (Local) parameters.next();
				ArrayList<PatternOccurrenceInfo> listOfPatternOccurrenceInfo = unitCausingNullsNotAllowed
						.get(param);

				patternDistributionOverMethod.println("*" + ";;" + " * " + ";;"
						+ " * " + ";;" + "*" + ";;" + param + ";;"
						+ listOfPatternOccurrenceInfo.size());

				Iterator<PatternOccurrenceInfo> patternOccurrenceInfoIterator = listOfPatternOccurrenceInfo
						.iterator();

				while (patternOccurrenceInfoIterator.hasNext()) {
					PatternOccurrenceInfo patternOccurrenceInfo = (PatternOccurrenceInfo) patternOccurrenceInfoIterator
							.next();

					patternDistributionOverMethod
							.println("*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ patternOccurrenceInfo.getOccurrenceType()
									+ ";;"
									+ patternOccurrenceInfo
											.getUnitOnwhichOccurrenceIsDetected()
									+ ";;"
									+ patternOccurrenceInfo
											.getCommentFragment());

				}

			}

			patternDistributionOverMethod.close();

		} else {

			PrintWriter patternDistributionOverMethod = new PrintWriter(
					new BufferedWriter(new FileWriter(file1, true)));
			patternDistributionOverMethod.println(className + ";;" + methodName
					+ ";;" + nbOfDetectedpatternInCurrentMethod + ";;"
					+ methodDeclaration);

			Iterator<Local> parameters = unitCausingNullsNotAllowed.keySet()
					.iterator();

			while (parameters.hasNext()) {
				Local param = (Local) parameters.next();
				ArrayList<PatternOccurrenceInfo> listOfPatternOccurrenceInfo = unitCausingNullsNotAllowed
						.get(param);

				patternDistributionOverMethod.println("*" + ";;" + " * " + ";;"
						+ " * " + ";;" + "*" + ";;" + param + ";;"
						+ listOfPatternOccurrenceInfo.size());

				Iterator<PatternOccurrenceInfo> patternOccurrenceInfoIterator = listOfPatternOccurrenceInfo
						.iterator();

				while (patternOccurrenceInfoIterator.hasNext()) {
					PatternOccurrenceInfo patternOccurrenceInfo = (PatternOccurrenceInfo) patternOccurrenceInfoIterator
							.next();

					patternDistributionOverMethod
							.println("*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ "*"
									+ ";;"
									+ patternOccurrenceInfo.getOccurrenceType()
									+ ";;"
									+ patternOccurrenceInfo
											.getUnitOnwhichOccurrenceIsDetected()
									+ ";;"
									+ patternOccurrenceInfo
											.getCommentFragment());

				}

			}

			patternDistributionOverMethod.close();

		}

	}

}
