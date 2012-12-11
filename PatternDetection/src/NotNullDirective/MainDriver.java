package NotNullDirective;
/* Usage: java MainDriver appClass
 */

/* import necessary soot packages */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import soot.*;

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
	 
	  NotNullParameterStaticInstrumenter staticInstrumenter =new NotNullParameterStaticInstrumenter(file1,file2,file3);
    /* add a phase to transformer pack by call Pack.add */
    Pack jtp = PackManager.v().getPack("jtp");
    jtp.add(new Transform("jtp.instrumenter", 
    		staticInstrumenter));

    /* Give control to Soot to process all options, 
     * InvokeStaticInstrumenter.internalTransform will get called.
     */
   
    
    
	 String SourcePath2 ="C:\\Users\\medsd\\Desktop\\UdeM\\progetAnalyse\\workspace\\TestDetectingPaternInAPI\\src";
	 int j =0;
	 try {
		ArrayList<String> classes = getClass(SourcePath2);
		System.out.println(">>>----les classe  sont---->> ");
		
		for (String classname : classes) {
			
			System.out.println("# "+j+" ----->>>>>>>"+classname);
			j++;
		}
		
		String[] internalargs = new String[classes.size()];
		
		int i=0;
		for (String classename : classes) {
			if(!classename.equals("MainDriver") && !classename.equals("InvokeStaticInstrumenter")){
				System.out.println(i);
				internalargs[i]=classename;
				i++;
				
			}
			
		}

	 if(internalargs.length!=0){
		 
		 args=internalargs;
	 }
	 
	 } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	    String classPath =Scene.v().getSootClassPath();
	    System.out.println("class path---------->"+Scene.v().getSootClassPath());
	    StringBuilder resaltingClassPath = new StringBuilder();
        resaltingClassPath.append(classPath);
        
        resaltingClassPath.append(";"+"C:\\Users\\medsd\\Desktop\\UdeM\\progetAnalyse\\workspace\\TestDetectingPaternInAPI\\bin");
        System.out.println("resaltingClassPath---------->"+resaltingClassPath);
        Scene.v().setSootClassPath(resaltingClassPath.toString());
	    System.out.println("new class path---------->"+Scene.v().getSootClassPath());

    
    soot.Main.main(args);
    
    
    file1.close();
    file2.close();
    file3.close();
   
    
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
		//System.out.println("chofniiiiiiiiiiiiiiiii");
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


