/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */







import soot.options.*;

import soot.toolkits.graph.*;
import soot.*;
import soot.util.*;
import java.util.*;

import soot.toolkits.scalar.*;

// FSet version



/**
 *   Analysis that provides an implementation of the LocalDefs interface.
 */
public class SimpleLocalDefsUsingParameter implements LocalDefs
{
    Map<LocalUnitPair, List> localUnitPairToDefs;


    /**
     *   Computes the analysis given a UnitGraph computed from a method body.
     *   It is recommended that a ExceptionalUnitGraph (or similar) be provided
     *   for correct results in the case of exceptional control flow.
     *   @param g a graph on which to compute the analysis.
     *   
     *   @see ExceptionalUnitGraph
     */
    public SimpleLocalDefsUsingParameter(UnitGraph g)
    {
        if(Options.v().time())
            Timers.v().defsTimer.start();
        
        if(Options.v().verbose())
            G.v().out.println("[" + g.getBody().getMethod().getName() +
                               "]     Constructing SimpleLocalDefs...");
    
        LocalDefsFlowAnalysis analysis = new LocalDefsFlowAnalysis(g);
        
        if(Options.v().time())
            Timers.v().defsPostTimer.start();

        // Build localUnitPairToDefs map
        {
            Iterator unitIt = g.iterator();

            localUnitPairToDefs = new HashMap<LocalUnitPair, List>(g.size() * 2 + 1, 0.7f);

            while(unitIt.hasNext())
                {
                    Unit s = (Unit) unitIt.next();

                    Iterator boxIt = s.getUseBoxes().iterator();

                    while(boxIt.hasNext())
                        {
                            ValueBox box = (ValueBox) boxIt.next();

                            if(box.getValue() instanceof Local)
                                {
                                    Local l = (Local) box.getValue();
                                    LocalUnitPair pair = new LocalUnitPair(l, s);

                                    if(!localUnitPairToDefs.containsKey(pair))
                                        {
                                            IntPair intPair = analysis.localToIntPair.get(l);
					    
                                            ArrayPackedSet value = (ArrayPackedSet) analysis.getFlowBefore(s);

                                            List unitLocalDefs = value.toList(intPair.op1, intPair.op2);

                                            localUnitPairToDefs.put(pair, Collections.unmodifiableList(unitLocalDefs));
                                        }
                                }
                        }
                }
        }

        if(Options.v().time())
            Timers.v().defsPostTimer.end();
                
        if(Options.v().time())
            Timers.v().defsTimer.end();

	if(Options.v().verbose())
	    G.v().out.println("[" + g.getBody().getMethod().getName() +
                               "]     SimpleLocalDefs finished.");
    }

    public boolean hasDefsAt(Local l, Unit s)
    {
        return localUnitPairToDefs.containsKey( new LocalUnitPair(l,s) );
    }
    public List<Unit> getDefsOfAt(Local l, Unit s)
    {
        LocalUnitPair pair = new LocalUnitPair(l, s);

        List<Unit> toReturn = localUnitPairToDefs.get(pair);
        
        if(toReturn == null)
            throw new RuntimeException("Illegal LocalDefs query; local " + l + " has no definition at " + 
                                       s.toString());
               
        
        return toReturn;
    }



}

class IntPair
{
    int op1, op2;

    public IntPair(int op1, int op2)
    {
        this.op1 = op1;
        this.op2 = op2;
    }

}

class LocalDefsFlowAnalysis extends ForwardFlowAnalysis
{
    FlowSet emptySet;
    Map<Local, BoundedFlowSet> localToPreserveSet;
    Map<Local, IntPair> localToIntPair;
	ArrayList<Local> methodParameterChain =  new ArrayList<Local>();

    public LocalDefsFlowAnalysis(UnitGraph g)
    {
        super(g);

        Object[] defs;
        FlowUniverse defUniverse;

        if(Options.v().time())
            Timers.v().defsSetupTimer.start();
        
        
        
        //create a liste of method parametre 
        {
        	
        	Body body = g.getBody();
        	SootMethod method = body.getMethod();
            
            for (int j = 0; j < method.getParameterCount(); j++) {
        		
            	 
            	methodParameterChain.add(body.getParameterLocal(j));
            	    	  	
        	}
        	
        	
        	
        }

        // Create a list of all the definitions and group defs of the same local together
        {
            Map<Local, ArrayList> localToDefList = new HashMap<Local, ArrayList>(g.getBody().getLocalCount() * 2 + 1, 0.7f);

            // Initialize the set of defs for each local to empty
            {
                Iterator localIt = g.getBody().getLocals().iterator();

                while(localIt.hasNext())
                    {
                        Local l = (Local) localIt.next();

                        localToDefList.put(l, new ArrayList());
                    }
            }

            // Fill the sets up
            {
                Iterator it = g.iterator();

                while(it.hasNext())
                    {
                        Unit s = (Unit) it.next();

                    
                        List defBoxes = s.getDefBoxes();
                        if(!defBoxes.isEmpty()) {
                            if(!(defBoxes.size() ==1)) 
                                throw new RuntimeException("invalid number of def boxes");
                            
                            if(((ValueBox)defBoxes.get(0)).getValue() instanceof Local) {
                                Local defLocal = (Local) ((ValueBox)defBoxes.get(0)).getValue();
                                List<Unit> l = localToDefList.get(defLocal);
                            
                                if(l == null)
                                    throw new RuntimeException("local " + defLocal + " is used but not declared!");
                                else
                                    l.add(s);
                            }
                        }
                    
                    }
            }

            // Generate the list & localToIntPair
            {
                Iterator it = g.getBody().getLocals().iterator();
                List defList = new LinkedList();

                int startPos = 0;

                localToIntPair = new HashMap<Local, IntPair>(g.getBody().getLocalCount() * 2 + 1, 0.7f);

                // For every local, add all its defs
                {
                    while(it.hasNext())
                        {
                            Local l = (Local) it.next();
                            Iterator jt = localToDefList.get(l).iterator();

                            int endPos = startPos - 1;

                            while(jt.hasNext())
                                {
                                    defList.add(jt.next());
                                    endPos++;
                                }

                            localToIntPair.put(l, new IntPair(startPos, endPos));

                            // G.v().out.println(startPos + ":" + endPos);

                            startPos = endPos + 1;
                        }
                }

                defs = defList.toArray();
                defUniverse = new ArrayFlowUniverse(defs);
            }
        }

        emptySet = new ArrayPackedSet(defUniverse);

        // Create the preserve sets for each local.
        {
            Map<Local, FlowSet> localToKillSet = new HashMap<Local, FlowSet>(g.getBody().getLocalCount() * 2 + 1, 0.7f);
            localToPreserveSet = new HashMap<Local, BoundedFlowSet>(g.getBody().getLocalCount() * 2 + 1, 0.7f);

            Chain locals = g.getBody().getLocals();

            // Initialize to empty set
            {
                Iterator localIt = locals.iterator();

                while(localIt.hasNext())
                    {
                        Local l = (Local) localIt.next();

                        localToKillSet.put(l, emptySet.clone());
                    }
            }

            for (Object element : defs) {
			    Unit s = (Unit) element;
			    
			    List defBoxes = s.getDefBoxes();
			    if(!(defBoxes.size() ==1)) 
			        throw new RuntimeException("SimpleLocalDefs: invalid number of def boxes");
			            
			    if(((ValueBox)defBoxes.get(0)).getValue() instanceof Local) {
			        Local defLocal = (Local) ((ValueBox)defBoxes.get(0)).getValue();
			        BoundedFlowSet killSet = (BoundedFlowSet) localToKillSet.get(defLocal);
			        killSet.add(s, killSet);
			        
			    }
			}
            
            // Store complement
            {
                Iterator localIt = locals.iterator();

                while(localIt.hasNext())
                    {
                        Local l = (Local) localIt.next();

                        BoundedFlowSet killSet = (BoundedFlowSet) localToKillSet.get(l);

                        killSet.complement(killSet);

                        localToPreserveSet.put(l, killSet);
                    }
            }
        }

        if(Options.v().time())
            Timers.v().defsSetupTimer.end();

        if(Options.v().time())
            Timers.v().defsAnalysisTimer.start();

        doAnalysis();
        
        if(Options.v().time())
            Timers.v().defsAnalysisTimer.end();
    }
    
    protected Object newInitialFlow()
    {
        return emptySet.clone();
    }

    protected Object entryInitialFlow()
    {
        return emptySet.clone();
    }

    protected void flowThrough(Object inValue, Object d, Object outValue)
    {
    	
    	System.out.println("--------------------flowThrough ---------------------of aymen");
        FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;
        Unit unit = (Unit) d;
        
        List defBoxes = unit.getDefBoxes();
        if(!defBoxes.isEmpty()) {
            if(!(defBoxes.size() ==1)) 
                throw new RuntimeException("SimpleLocalDefs: invalid number of def boxes");
                          
            Value value = ((ValueBox)defBoxes.get(0)).getValue();
            if(value  instanceof Local) {
                Local defLocal = (Local) value;
            
                // Perform kill on value
                in.intersection(localToPreserveSet.get(defLocal), out);

                // Perform generation  --> in performing generation we just consider unit that use one of the methode Parameter
		  		 

                List<ValueBox> vb = unit.getUseBoxes();
            	  
        		  for (ValueBox valueBox : vb) {
            		        		  
            		  	if (methodParameterChain.contains(valueBox.getValue())) {
            		  		
            		  		
            		  		 System.out.println("*----l'unit� --> "+ unit +" -----utilise un parametre----la Value------> "+valueBox.getValue()+ " --------qui existe dans les methodParameterChain  ");
            		  		 
            		  		out.add(unit, out);
            		  	}
            		        		  
            	  }
                
                
                
            } else { 
                in.copy(out);
                return;
            }


        

        }
        else
            in.copy(out);
    }

    protected void copy(Object source, Object dest)
    {
        FlowSet sourceSet = (FlowSet) source,
            destSet = (FlowSet) dest;
        
        sourceSet.copy(destSet);
    }

    protected void merge(Object in1, Object in2, Object out)
    {
        FlowSet inSet1 = (FlowSet) in1,
            inSet2 = (FlowSet) in2;
        
        FlowSet outSet = (FlowSet) out;
        
        inSet1.union(inSet2, outSet);
    }
}
