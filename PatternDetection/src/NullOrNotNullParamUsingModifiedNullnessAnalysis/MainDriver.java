package NullOrNotNullParamUsingModifiedNullnessAnalysis;
/* Usage: java MainDriver appClass
 */

/* import necessary soot packages */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import stat.Statistique;

public class MainDriver {
  public static void main(String[] args) throws IOException {

    /* check the arguments */
    
	  if (args.length == 0) {
      System.err.println("Usage: java MainDriver [options] classname");
      System.exit(0);
    }
    
	  
	  
	  String NomFichier1 = ".\\Test\\API\\P\\test1.txt";
	  PrintWriter file1 = new PrintWriter(new FileWriter(NomFichier1));
	  
	  String NomFichier2 = ".\\statistique\\API\\P\\TestDetectingPaternInAPI\\patternDistributionOverMethod.csv";
	  PrintWriter file2 = new PrintWriter(new FileWriter(NomFichier2));
	 
	  
	  String NomFichier3 = ".\\statistique\\API\\P\\TestDetectingPaternInAPI\\detectedPattern.csv";
	  PrintWriter file3 = new PrintWriter(new FileWriter(NomFichier3));
	 
	 
	  //	  String projectToAnalyzePath ="C:\\Users\\medsd\\Desktop\\UdeM\\progetAnalyse\\workspace\\TestDetectingPaternInAPI";
	  //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestDetectingPaternInAPI"; 
		  

	   // String projectToAnalyzePath ="..\\..\\JHotDraw7.0.6.git\\JHotDraw7.0.6";
	    //String StaticLocationPath  =".\\statistique\\API\\Patern\\JHotDraw7.0.6"; 
	   
	   
	  
	 /*  
String projectToAnalyzePath ="..\\..\\TestJdk.applet\\TestJdk.applet";
String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.applet"; 
String APIINFO_XML = ".\\api.xml.file\\java.applet.xml";
String API_NAME="java.applet";
*/


/*String projectToAnalyzePath ="..\\..\\TestJdk.awt\\TestJdk.awt";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.awt";
String APIINFO_XML = ".\\api.xml.file\\java.awt.xml";
String API_NAME="java.awt";  
*/


/*String projectToAnalyzePath ="..\\..\\TestJdk.beans\\TestJdk.beans";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.beans";
	  String APIINFO_XML = ".\\api.xml.file\\java.beans.xml";
	  String API_NAME="java.beans";	
*/
	  
	  
/*		 		  String projectToAnalyzePath ="..\\..\\TestJdk.io\\TestJdk.io";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.io";
String APIINFO_XML = ".\\api.xml.file\\java.io.xml";
String API_NAME="java.io";
*/	  


/* String projectToAnalyzePath ="..\\..\\TestJdk.lang\\TestJdk.lang";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
 String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.lang";
String APIINFO_XML = ".\\api.xml.file\\java.lang.xml";
String API_NAME="java.lang"; 
*/

/*	 String projectToAnalyzePath ="..\\..\\TestJdk.math\\TestJdk.math";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.math";
String APIINFO_XML = ".\\api.xml.file\\java.math.xml";
String API_NAME="java.math";
*/



/*	 			  String projectToAnalyzePath ="..\\..\\TestJdk.net\\TestJdk.net";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
		  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.net";
String APIINFO_XML = ".\\api.xml.file\\java.net.xml";
String API_NAME="java.net";
*/ 

		 /* String projectToAnalyzePath ="..\\..\\TestJdk.nio\\TestJdk.nio";
		  * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
		  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.nio";
String APIINFO_XML = ".\\api.xml.file\\java.nio.xml";
String API_NAME="java.nio";
*/

/*			  String projectToAnalyzePath ="..\\..\\TestJdk.rmi\\TestJdk.rmi";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.rmi";
String APIINFO_XML = ".\\api.xml.file\\java.rmi.xml";
String API_NAME="java.rmi";
*/	  

  /* String projectToAnalyzePath ="..\\..\\TestJdk.security\\TestJdk.security";
   * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.security";
String APIINFO_XML = ".\\api.xml.file\\java.security.xml";
String API_NAME="java.security";
*/

/*		  String projectToAnalyzePath ="..\\..\\TestJdk.sql\\TestJdk.sql";
 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.sql";
String APIINFO_XML = ".\\api.xml.file\\java.sql.xml";	
String API_NAME="java.sql";  
*/

		/*		  String projectToAnalyzePath ="..\\..\\TestJdk.text\\TestJdk.text";
		 * String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.text";
	String APIINFO_XML = ".\\api.xml.file\\java.text.xml";
	String API_NAME="java.text";
	  */

	  /*
String projectToAnalyzePath ="..\\..\\TestJdk.util\\TestJdk.util";
String pakagingPath ="\\src";
String pakagingClassPath ="\\bin";
String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.util";
String APIINFO_XML = ".\\api.xml.file\\java.util.xml";
String API_NAME="java.util"; 

*/
	  /*  
	  String projectToAnalyzePath ="..\\..\\super-csv\\super-csv";
	  String pakagingPath ="\\src\\main\\java";
	  String pakagingClassPath ="\\bin\\classes";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\super-csv";
	  String APIINFO_XML = ".\\api.xml.file\\org.supercsv.cellprocessor.xml";
	  String API_NAME="super-csv"; 
	  
	  */


	  String projectToAnalyzePath ="..\\..\\bankgiro-api\\bankgiro-api";
	  String pakagingPath ="\\src\\main\\java";
	  String pakagingClassPath ="\\target\\classes";
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\bankgiro-api";
	  String APIINFO_XML = ".\\api.xml.file\\org.supercsv.cellprocessor.xml";
	  String API_NAME="super-csv"; 
	  
	  
	  // NotNullParameterStaticInstrumenter staticInstrumenter =new NotNullParameterStaticInstrumenter(file1,file2,file3);
	  NotNullParameterStaticInstrumenter staticInstrumenter =new NotNullParameterStaticInstrumenter(StaticLocationPath,APIINFO_XML,API_NAME);
	  
    /* add a phase to transformer pack by call Pack.add */
    Pack jtp = PackManager.v().getPack("jtp");
    jtp.add(new Transform("jtp.instrumenter", 
    		staticInstrumenter));

    /* Give control to Soot to process all options, 
     * InvokeStaticInstrumenter.internalTransform will get called.
     */
   
  
    
    
    
    
    
   
    
    
    String SourcePath2 =projectToAnalyzePath+pakagingPath;
    
    
	 int j =0;
	 try {
		 
		 
		ArrayList<String> classes = getClass(SourcePath2);
		
		
		
		System.out.println(">>>----les classe  sont---->> ");
		
		for (String classname : classes) {
			
			System.out.println("# "+j+" ----->>>>>>>"+classname);
			j++;
		}
		
		// String[] internalargs = new String[classes.size()];
		List<String> internalargs = new ArrayList<String>(classes.size());
		// to preserve the names of local variables (if possible) when performing ana analysis  
		
		/*
		 * domage car cette option n'a pa eu l'effet atendue 
		internalargs.add("-p");
		internalargs.add("jb");
		internalargs.add("use-original-names:true");
		*/
		internalargs.add("-pp"); //pour ajouter les classe de la lib standar  à soot (en plus d'un probléme pour la configuration la verssion de lib standar  )
		  
		int i=0;
		for (String classename : classes) {
			if(!classename.equals("MainDriver") && !classename.equals("InvokeStaticInstrumenter")){
				System.out.println(i);
				internalargs.add(classename);
				i++;
				
			}
			
		}
		
		

	 if(internalargs.size()!=0){
		 
		
		 
		 args=internalargs.toArray(new String[internalargs.size()]);
	 }
	 
	 } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		
	}
	 
	    String classPath =Scene.v().getSootClassPath();
	    System.out.println("class path---------->"+Scene.v().getSootClassPath());
	    StringBuilder resaltingClassPath = new StringBuilder();
        resaltingClassPath.append(classPath);
        //*
        resaltingClassPath.append(";C:\\Program Files (x86)\\Java\\jre7\\lib\\jce.jar");
        
       
        resaltingClassPath.append(";"+projectToAnalyzePath+pakagingClassPath);
        
        
        System.out.println("resaltingClassPath---------->"+resaltingClassPath);
        Scene.v().setSootClassPath(resaltingClassPath.toString());
	    System.out.println("new class path---------->"+Scene.v().getSootClassPath());

    
    soot.Main.main(args);
    
    
    file1.close();
    file2.close();
    file3.close();
   
    
    Statistique Statistique = new Statistique();
    int nbAnalyzedMethod =staticInstrumenter.getNumberOfAnalyzedMethod();
    // Statistique patternDistributionOverClasses  NullNotAllowedPattern
    HashMap<String, Integer> patternDistributionOverClasses1= staticInstrumenter.getNullNotAllowedPaternDistributionOverClasses();
    String  patterName1 = "NullNotAllowed";
    
    String filePath1 =  StaticLocationPath +"\\NullNotAllowedPatternDistributionOverClass.csv";
    Statistique.statistiqueForPatternDistributionOverClass(patternDistributionOverClasses1, filePath1, patterName1,StaticLocationPath,nbAnalyzedMethod);
    
    // Statistique patternDistributionOverClasses  NullAllowedPattern    
    
    HashMap<String, Integer> patternDistributionOverClasses2= staticInstrumenter.getNullAllowedPaternDistributionOverClasses();
    String  patterName2 = "NullAllowed";
    String filePath2 =  StaticLocationPath +"\\NullAllowedpatternDistributionOverClass.csv";
    Statistique.statistiqueForPatternDistributionOverClass(patternDistributionOverClasses2, filePath2, patterName2,StaticLocationPath,nbAnalyzedMethod);
    
    
    
  }

	// ça liste les classe java du repertoire source 
	public static ArrayList<String> getClass(String repertoire) throws Exception{
		ArrayList<String> list;
		
			File rep = new File(repertoire);
			
			rep.getName();
			
			
			String [] listefichiers;
			list = new ArrayList<String>();
			listefichiers=rep.list();
			
			for(int i=0;i<listefichiers.length;i++){
				if( listefichiers[i].endsWith(".java") ||listefichiers[i].endsWith(".class")){ //
					int dot = listefichiers[i].lastIndexOf('.');
						String filename = listefichiers[i].substring(0, dot);
						//System.out.println(filename);
						list.add(filename);
				}
			}
			for (File r : rep.listFiles()) {
				if(r.isDirectory())
					list.addAll(getClass(r.getName(), r));
			}
			
		return list;
	}


		
	private static ArrayList<String> getClass(String repertoire,File rep){
		
		String [] listefichiers;
		ArrayList<String> list = new ArrayList<String>();
		
		int i;
		listefichiers=rep.list();
		
		for(i=0;i<listefichiers.length;i++){
			if( listefichiers[i].endsWith(".java") ||listefichiers[i].endsWith(".class")){
					int dot = listefichiers[i].lastIndexOf('.');
					String filename = listefichiers[i].substring(0, dot);
					list.add(repertoire+"."+filename);
			}
		}
		for (File r : rep.listFiles()) {
			if(r.isDirectory())
				list.addAll(getClass(repertoire+"."+r.getName(), r));
		}

		return list;
	}










}


