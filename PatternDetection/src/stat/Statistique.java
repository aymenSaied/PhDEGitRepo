package stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javadoc.api.marshalling.ApiInfo;
import javadoc.api.marshalling.ClassInfo;
import javadoc.api.marshalling.MethodInfo;
import javadoc.api.marshalling.ParameterInfo;
import javadoc.api.marshalling.pattern.PaternInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import soot.Local;
import NullOrNotNullParamUsingModifiedNullnessAnalysis.NotNullParameterStaticInstrumenter;


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
	
	
	

	public void statistiqueForPatternAndJavaDocInMethod(
			String apiXmlFile,
			String apiname,
			String patternName,
			String className,
			String methodSignature ,
			String statistiqueLocation,
			String file,
			Map<Local, String> ParameterLimitedRangeComment,
			HashMap<Local, Integer> methodParameterToStringId
			) throws IOException, JAXBException{
		
		
		
		String APIINFO_XML = ".//patenrn.xml.file//"+apiname+"//"+patternName+apiname+".xml";
		
		
		
	
		String statistiqueLocationPath = statistiqueLocation;
		String filename = file;
		String filePath1 = statistiqueLocationPath + filename;

		File file1 = new File(filePath1);

		String javadoc = new String();
		String javadocforHtml = new String();
		javadoc = findComment(methodSignature, apiXmlFile);
		javadocforHtml=javadoc;
		javadoc=javadoc.replaceAll("[\r\n]+", "");
		
		if (!file1.exists()) {

			
			
			JAXBContext context = JAXBContext.newInstance(javadoc.api.marshalling.pattern.ApiInfo.class);
			Marshaller m = context.createMarshaller();
		 	
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		    
			
			
			PrintWriter patternDistributionOverMethod = new PrintWriter(file1);
			patternDistributionOverMethod.println("class name" + ";;"
					+ "Method Signature" + ";;" + "parameter" + ";;"
					+ "paternComment"+ ";;" + "javadoc");

			
			
			Iterator<Local> parameters = ParameterLimitedRangeComment.keySet()
					.iterator();

			
			ArrayList<PaternInfo>  paterninfolist1 = new ArrayList<PaternInfo>();
			
			while (parameters.hasNext()) {
				Local param = (Local) parameters.next();
				String paternComment=ParameterLimitedRangeComment.get(param);
				
				// la ligne suivante pour ajoute @parami dans le commment du pater pour savaoir quel est le param  oubin le non du paramaitre 
				
				
				String parameterNameOrIndex ="@param "+methodParameterToStringId.get(param).toString();
				
				
				if (parametersInfoList.size()>0) {
					
					
					parameterNameOrIndex =parametersInfoList.get(methodParameterToStringId.get(param)).getName();
					
				}
				
				
				paternComment += " ## the pattern was detected for parameter "+ parameterNameOrIndex; 
			
				patternDistributionOverMethod.println(className + ";;" + methodSignature
						+ ";;" + param + ";;"
						+ paternComment+ ";;" + javadoc);

			
				
				PaternInfo paterninfo = new PaternInfo();
				
				paterninfo.setMethodSignature(methodSignature);
				paternComment=paternComment.replaceAll( "##","\r\n");

				paterninfo.setPatern(paternComment);
				paterninfo.setPaternType(patternName);
				paterninfo.setCommentText(javadocforHtml);
				
				
				paterninfolist1.add(paterninfo);
				
				
			}			
			
			
			
			
			patternDistributionOverMethod.close();
			
			
			javadoc.api.marshalling.pattern.ApiInfo apiInfo = new javadoc.api.marshalling.pattern.ApiInfo();
			apiInfo.setName(apiname);
			apiInfo.setListpatern(paterninfolist1);
			
			  m.marshal(apiInfo, new File(APIINFO_XML));
			
		}else {
			
			JAXBContext context = JAXBContext.newInstance(javadoc.api.marshalling.pattern.ApiInfo.class);
			 Unmarshaller um = context.createUnmarshaller();
			 javadoc.api.marshalling.pattern.ApiInfo api = (javadoc.api.marshalling.pattern.ApiInfo) um.unmarshal(new FileReader(APIINFO_XML));
			
			
			
			
			ArrayList<PaternInfo>  paterninfolist2 = new ArrayList<PaternInfo>();
			
			paterninfolist2.addAll(api.getListpatern());
			
			
			PrintWriter patternDistributionOverMethod = new PrintWriter(
					new BufferedWriter(new FileWriter(file1, true)));
			
			Iterator<Local> parameters = ParameterLimitedRangeComment.keySet()
					.iterator();

			while (parameters.hasNext()) {
				Local param = (Local) parameters.next();
				String paternComment=ParameterLimitedRangeComment.get(param);
// la ligne suivante pour ajoute @parami dans le commment du pater pour savaoir quel est le param  oubin le non du paramaitre 
				
				
				String parameterNameOrIndex ="@param "+methodParameterToStringId.get(param).toString();
				
				
				if (parametersInfoList.size()>0) {
					
					
					parameterNameOrIndex =parametersInfoList.get(methodParameterToStringId.get(param)).getName();
					
				}
				
				
				paternComment += " ## the pattern was detected for parameter "+ parameterNameOrIndex; 
							
				patternDistributionOverMethod.println(className + ";;" + methodSignature
						+ ";;" + param + ";;"
						+ paternComment+ ";;" + javadoc);

			
				
				PaternInfo paterninfo = new PaternInfo();
				
				paterninfo.setMethodSignature(methodSignature);
				paternComment=paternComment.replaceAll( "##","\r\n");
				paterninfo.setPatern(paternComment);
				paterninfo.setPaternType(patternName);
				paterninfo.setCommentText(javadocforHtml);
				
				
				paterninfolist2.add(paterninfo);
				
				
				
			}			
			
			
			
			
			patternDistributionOverMethod.close();

			api.setListpatern(paterninfolist2);
			Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(api, new File(APIINFO_XML));
			
			
		}
		
		
		

		
		
		
	}
	
	
	static private ArrayList<ParameterInfo> parametersInfoList;
	
static	 String findComment(String methodSignatureTofind,String apiXmlFile) throws JAXBException, FileNotFoundException {
		  
		  /*
		   * TODO
		   * loperation find telque elle est defini consomera 
		   * beucoup  de resource si on lui envoi chaque instance de paterne 
		   * pour chercher la javadoc qui lui correspond
		   * une facon plus optimale 
		   * est de separer les triatement
		   * envoyer a cette methode une liste des patern detecter 
		   * (par exemple une structure qui content le signature de la methode 
		   * dans la qule on a detecter le patern et le messag de detection exemple i0 is restricted to [0,1] )
		   * 
		   * et cette methode se charge de chercher la javadoc correspendante
		   * donc on fait le unmarshaling une seul fois 
		   * et pour chaque paterne detecter cherche la javadoc  et ecrire dans le cvs 
		   * nonClass methodSigneter  paternComment javadoc 
		   * */
		  
		  
		  
		  System.out.println("Output from our XML File: ");
		  
		  JAXBContext context = JAXBContext.newInstance(ApiInfo.class);
		  Unmarshaller um = context.createUnmarshaller();
		  ApiInfo api = (ApiInfo) um.unmarshal(new FileReader(apiXmlFile));
		  
		  Boolean methodFound =false;
		  String comment="no javadoc";
		  parametersInfoList = new ArrayList<ParameterInfo>();  /* initialisation dans cette position pour eviter 
		  que les valeur de la recherech presedante reste dans parametersInfoList car c'est une variable statique */ 
		  
		  
		  ArrayList<ClassInfo>clasList = api.getListclass();
		  Iterator<ClassInfo> itClassInfo= clasList.iterator();
		  
		  while (itClassInfo.hasNext()&&!methodFound) {
			ClassInfo classInfo = (ClassInfo) itClassInfo.next();
			
			ArrayList<MethodInfo>methodList=classInfo.getListMethod();
			Iterator<MethodInfo>itMethodInfo=methodList.iterator();
			while (itMethodInfo.hasNext()&&!methodFound) {
				MethodInfo methodInfo = (MethodInfo) itMethodInfo.next();
				
				//System.out.println("#1"+methodInfo.getMethodSignature());
				//System.out.println("#2"+methodSignatureTofind);
				
				if (methodInfo.getMethodSignature().equals(methodSignatureTofind)) {
					
					methodFound=true;
					comment=methodInfo.getCommentText();
					//TODO normalement ic je teste sur le commentaire vide pour afecter comment="no javadoc" si comentaire vide
					
					
					if (comment.equals("")) {
						
						  comment = " No javadoc for this method " ;
						  
					}
					
					parametersInfoList= methodInfo.getParameterList();
				} 
				
			}
			
		}
		  
		  
		  System.out.println("##############");
		  if (methodFound) {
			  
			  
			  
			  
			  
			System.out.println("the method is: "+methodSignatureTofind);
			System.out.println("javadoc is:  "+comment);
			
		}else {
			
			System.out.println("the method is: "+methodSignatureTofind);
			System.out.println("404 not found");
			comment="404 ;ethod not found";
			try {
				throw new Exception(methodSignatureTofind+" not found in xml representation of the API");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		  
		return comment;  
	  }

	
	

}
