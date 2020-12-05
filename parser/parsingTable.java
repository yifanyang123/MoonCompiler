package MoonCompiler.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class parsingTable {
	
	private Hashtable myTable;
	private Hashtable First;
	private Hashtable Follow;
	private HashSet terminal;
	private HashSet nonTerminal;
	private String[] terminalArray;
	private String[] nonTerminalArray;
	private ArrayList<String> rules;
	
	public parsingTable(HashSet terminal,HashSet nonTerminal,ArrayList<String> rule,Hashtable first,Hashtable follow) {
		myTable=new Hashtable();
		this.terminal=terminal;
		this.nonTerminal=nonTerminal;
		this.rules=rule;
		this.First=first;
		this.Follow=follow;
		terminal.add(":");
		terminal.add("then");
		terminal.add("expr");
		terminal.add("(");
	}
	
	public void generateTable() {
		 terminalArray= (String[]) terminal.toArray(new String[terminal.size()]);
		 nonTerminalArray= (String[]) nonTerminal.toArray(new String[nonTerminal.size()]);
		 for(int i=0;i< nonTerminalArray.length;i++) 	 
			 for (int j=0;j< terminalArray.length;j++) {			 
				if(!terminalArray[j].equals("EPSILON")) {  //remove epsilon
					myTable.put(terminalArray[j].concat(nonTerminalArray[i]), "error");
				}
			 }
		 //all value are error
		 
			for(int i=0;i<rules.size();i++){
			   String currentRule=rules.get(i);
			   String[] temp1=currentRule.split(" ::= "); //temp1[0] is lhs 1[1] is rhs
			   String[] rhsF=temp1[1].split(" ");
			   
			   int firstIndex=0;
			   try {
				   Integer.parseInt(rhsF[0]);
				   firstIndex=1;
			   }
			   catch(NumberFormatException e){
			   }
			   
			   if(rhsF[firstIndex].charAt(0)=='\'') { //checked
				   rhsF[firstIndex]=rhsF[firstIndex].replaceAll("\\'", "");
				   //System.out.println(rhsF[0].concat(temp1[0])+":"+currentRule);
				   myTable.put(rhsF[firstIndex].concat(temp1[0]), currentRule);
			   }
			   else if(rhsF[firstIndex].charAt(0)=='<') {
				   	HashSet tempfirst=(HashSet) First.get(rhsF[firstIndex]);
				   	//System.out.println(rhsF[0]+tempfirst); okforhere 
					Iterator firstiterator = tempfirst.iterator();  
					  while (firstiterator.hasNext()) {
						 String firstElement=firstiterator.next().toString();
						 String myString=null;
						 if (!firstElement.equals("EPSILON")) {
							 myString=firstElement.concat(temp1[0]); //terminal in first element + nonterminal lhs 
							// System.out.println(myString+":"+currentRule); //checked
							 myTable.put(myString, currentRule);   
						 }						         
					   }
					  //step 2 done-==-=-=-=-=-====================
					  //System.out.println(tempfirst);
					  if (tempfirst.contains("EPSILON")) { //
						 // System.out.println(temp1[0]);
						 // System.out.println(rhsF[0]); //CANGETALL EPSILON
						  //System.out.println(true);
						  //HashSet tempFollow=(HashSet) Follow.get(temp1[0]);
						  HashSet tempFollow=(HashSet) Follow.get(rhsF[firstIndex]);
						  if (!tempFollow.contains("")) {
							  Iterator followiterator = tempFollow.iterator();  
							  while (followiterator.hasNext()) {
								 String followElement=followiterator.next().toString();
								 String myString=null;
								 myString=followElement.concat(temp1[0]); //terminal in follow element + nonterminal lhs 
								//System.out.println(myString+":"+currentRule); checked
								myTable.put(myString, currentRule);   
								 				         
							   }
						  }
					  	}
			   		}
			   else if (rhsF[firstIndex].charAt(0)=='E') {  //rhs is epsilon
				   HashSet tempFollow=(HashSet) Follow.get(temp1[0]);
					  if (!tempFollow.contains("")) {
						  Iterator followiterator = tempFollow.iterator();  
						  while (followiterator.hasNext()) {
							 String followElement=followiterator.next().toString();
							 String myString=null;
								myString=followElement.concat(temp1[0]); //terminal in follow element + nonterminal lhs 
								//System.out.println(myString+":"+currentRule);  checked
							 myTable.put(myString, currentRule);   							 					         
						   }
					  }
			   }
		   
			} 
		 }
	 public Hashtable getParsingTable() {
		 return myTable;
	 }
	 public void display() {
		 for(int i=0;i< nonTerminalArray.length;i++) 	 
			 for (int j=0;j< terminalArray.length;j++) {
				System.out.print(terminalArray[j].concat(nonTerminalArray[i]+" : "));
				System.out.println(myTable.get(terminalArray[j].concat(nonTerminalArray[i])));
			 }
	 }
	
}
