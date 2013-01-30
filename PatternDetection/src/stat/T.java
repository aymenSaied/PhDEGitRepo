package stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class T {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		System.out.println("test");
		
		
		T t = new T();
		t.m1(10);
		t.m1(20);
		t.m1(30);
		
		System.out.println("fin test");
		
	}
	
	
	
 public void m1(int i) throws IOException{
	 
	 String statistiqueLocationPath  =".\\statistique\\API\\Patern\\JHotDraw7.0.6";
	 String filePath1 =  statistiqueLocationPath +"\\NullNotAllowedPatternDistributionOverMethod.csv";
	 
	 File file1 = new File(filePath1);
	 
	 if (!file1.exists()) {
		 
		 PrintWriter patternDistributionOverMethod= new PrintWriter(file1);
		 patternDistributionOverMethod.println("class name"+";;"+"Method name"+";;"+"number of detected pattern");
		 patternDistributionOverMethod.close();
		//patternDistributionOverMethod.println(declaringClass+";;"+method.getSignature()+";;"+nbOfDetectedpatternInCurrentMethod);
		 
	} else {

		 PrintWriter patternDistributionOverMethod= new PrintWriter(new BufferedWriter(new FileWriter(file1, true)));
			//patternDistributionOverMethod.println(declaringClass+";;"+method.getSignature()+";;"+nbOfDetectedpatternInCurrentMethod);
			
			 patternDistributionOverMethod.println("classe1"+";;"+"methode1"+";;"+i);
			 patternDistributionOverMethod.close();
			 System.out.println("cas 2");
	}
	 
	 
	 
	 
	 
 }
	

}
