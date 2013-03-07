package stat;

public class Test {

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param args
	 */
	
	public String nom;
	
	public void setnom(Test t,String n){
		
		
		t.nom= n ;
		
		System.out.println("le nom -->"+t.nom);
		
	}
	
public void afichenomSelonCondition(Test t){
		
		if (t.nom== "saied") {
			System.out.println("le nom -->"+t.nom);
			
		}
		
		
		
		
	}
	
public void afichenomdetest(Test t,Test t2){
	
	t.afichenom(t2.nom);
	
}

public void afichenom(String t){
	
	if (t== "saied") {
		System.out.println("le nom -->"+t);
		
	}
	
	
	
	
}



public static void main(String[] args) {
		// TODO Auto-generated method stub

		Test t =new Test();
		
		
		
		t.setnom(t, "saied1");
	//	t.setnom(null, "saied2")
		//t.afichenomSelonCondition(null);
		t.afichenomdetest(t,null);
		
		t.setnom(t, "saied3");
		
		
		
		
	}

}
