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
	 
	 
	  //String projectToAnalyzePath ="C:\\Users\\medsd\\Desktop\\UdeM\\progetAnalyse\\workspace\\TestDetectingPaternInAPI";
	  //String StaticLocationPath  =".\\statistique\\API\\Patern\\TestDetectingPaternInAPI"; 
		  

	   // String projectToAnalyzePath ="..\\..\\JHotDraw7.0.6.git\\JHotDraw7.0.6";
	    //String StaticLocationPath  =".\\statistique\\API\\Patern\\JHotDraw7.0.6"; 
	   
	   
	  
	  
	    //		  String projectToAnalyzePath ="..\\..\\TestJdk.applet\\TestJdk.applet";
	    //		  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.applet"; 
	  
	  
	//	  String projectToAnalyzePath ="..\\..\\TestJdk.awt\\TestJdk.awt";
	//	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.awt";
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.beans\\TestJdk.beans";
		//	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.beans";

	  
		//	  String projectToAnalyzePath ="..\\..\\TestJdk.io\\TestJdk.io";
		  //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.io";
	  
	  
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.lang\\TestJdk.lang";
		  // 	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.lang";
	  
	  

		//	  
	  String projectToAnalyzePath ="..\\..\\TestJdk.math\\TestJdk.math";
		  //	 
	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.math";
	  
	  
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.net\\TestJdk.net";
		   //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.net";
	  
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.nio\\TestJdk.nio";
		  //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.nio";
	  
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.rmi\\TestJdk.rmi";
		  //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.rmi";
	  
	  

	//	  	  String projectToAnalyzePath ="..\\..\\TestJdk.security\\TestJdk.security";
		  //		  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.security";
	  
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.sql\\TestJdk.sql";
		  //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.sql";
	  
	  

		//	  String projectToAnalyzePath ="..\\..\\TestJdk.text\\TestJdk.text";
		  //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.text";
	  
	  
	//	  String projectToAnalyzePath ="..\\..\\TestJdk.util\\TestJdk.util";
    //	  String StaticLocationPath  =".\\statistique\\API\\Patern\\TestJdk.util";

	  
	  
	  
	  
	  
	  
	  
	  
	  // NotNullParameterStaticInstrumenter staticInstrumenter =new NotNullParameterStaticInstrumenter(file1,file2,file3);
	  NotNullParameterStaticInstrumenter staticInstrumenter =new NotNullParameterStaticInstrumenter(StaticLocationPath);
	  
    /* add a phase to transformer pack by call Pack.add */
    Pack jtp = PackManager.v().getPack("jtp");
    jtp.add(new Transform("jtp.instrumenter", 
    		staticInstrumenter));

    /* Give control to Soot to process all options, 
     * InvokeStaticInstrumenter.internalTransform will get called.
     */
   
  
    
    
    
    
    
    String SourcePath2 =projectToAnalyzePath+"\\src";
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
        resaltingClassPath.append(";"+projectToAnalyzePath+"\\bin");
        
        
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


