package stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

public class Statistique {
	
	
	public void statistiqueForPatternDistributionOverClass(HashMap<String, Integer> patternDistributionOverClasses,String filePath, String  patterName) throws FileNotFoundException{
		
		
		
		
		
		String statFilePath=filePath;
		
		PrintWriter patternDistributionOverClass= new PrintWriter(statFilePath);  
		
		patternDistributionOverClass.println("class name"+";;"+"number of detected pattern");

		int i =0;
		int j=0; 
		System.out.println("___________________Statistique________________"+ patterName);

		
	Set<java.util.Map.Entry<String, Integer>> setEntry = patternDistributionOverClasses.entrySet();

	for (java.util.Map.Entry<String, Integer> entry : setEntry) {
		
		
		
		System.out.println(j+"la clase ---> "+entry.getKey()+" nbpatern ----> "+entry.getValue());
		patternDistributionOverClass.println(entry.getKey()+";;"+entry.getValue());

		i+=entry.getValue();
		j++;
	}

	System.out.println(" ___________________Fin Statistique_______"+patterName+"________avec nb totl de patron :_"+i);

	patternDistributionOverClass.close();
	}
	
	
public void statistiqueForPatternDistributionOverMethod(int nbOfDetectedpatternInCurrentMethod,String className, String methodName ,String statistiqueLocation,String file) throws IOException{
		
	
	


String statistiqueLocationPath  =statistiqueLocation;
String filename=file;
String filePath1 =  statistiqueLocationPath +filename;

File file1 = new File(filePath1);

if (!file1.exists()) {
	 
	 PrintWriter patternDistributionOverMethod= new PrintWriter(file1);
	 patternDistributionOverMethod.println("class name"+";;"+"Method name"+";;"+"number of detected pattern");
	
	 patternDistributionOverMethod.println(className+";;"+methodName+";;"+nbOfDetectedpatternInCurrentMethod);
	 patternDistributionOverMethod.close();
	 
} else {

	 PrintWriter patternDistributionOverMethod= new PrintWriter(new BufferedWriter(new FileWriter(file1, true)));
	 patternDistributionOverMethod.println(className+";;"+methodName+";;"+nbOfDetectedpatternInCurrentMethod);
		
		 
		 patternDistributionOverMethod.close();
		 
}

	
	
}
	

}
