package MoonCompiler.SemanticAnalyzer;

import java.util.ArrayList;

import MoonCompiler.parser.ASTnode;

public class TypeCheckingVisitor extends Visitor {

	public void visit(ASTnode node) {
	switch(node.getType()) {
	case "intNum" :{
		node.setBindType("integer");
		break;
	}
	case "floatNum" :{
		node.setBindType("float");
		break;
	}
	//
	//some node should grab children   : not, sign
	case "sign":{ //just grab children  + - 
		node.setBindType(node.getleftmostChild().getBindType()); 
		break;
	}
	case "not":{
		node.setBindType(node.getleftmostChild().getBindType()); 
		break;
	}
	//there will never be a node that is factor, arithExpr, term,expr   
	// they will finally be var, num, fcall,   
	case "addOp":{ //assignment3 new change ,check astnode case 33   + - or		
		if(node.getleftmostChild().getBindType().equals("integer")||node.getleftmostChild().getBindType().equals("float")||node.getleftmostChild().getBindType().equals("typeError")) {
			if(node.getleftmostChild().getBindType().equals(node.getChildren(1).getBindType()))
				node.setBindType(node.getleftmostChild().getBindType());
			else {
				node.setBindType("typeError");
				SemanticAnalyzer.Success=false;
				System.out.println("Failure,addOp,Type Error detected between "+node.getleftmostChild().getValue()+" and "+node.getChildren(1).getValue()+" in scope "+searchScope(node).getName()+" at Line "+node.getLine());
			}			
		}
		else{
			node.setBindType("typeError");
			SemanticAnalyzer.Success=false;
			System.out.println("Failure,operator on object between "+node.getleftmostChild().getValue()+" and "+node.getChildren(1).getValue()+" in scope "+searchScope(node).getName()+" at Line "+node.getLine());
		}
			
		break;
	}
	case "multOp":{ //assignment3 new change ,check astnode case 37   * / and	
		if(node.getleftmostChild().getBindType().equals(node.getChildren(1).getBindType()))
			node.setBindType(node.getleftmostChild().getBindType());
		else {
			node.setBindType("typeError");
			SemanticAnalyzer.Success=false;
			System.out.println("Failure,addOp,Type Error detected between "+node.getleftmostChild().getValue()+" and "+node.getChildren(1).getValue()+" in scope "+searchScope(node).getName()+" at Line "+node.getLine());
		}			
		break;
	}
	case "relExpr": {//special one, should get second child
		if(node.getleftmostChild().getBindType().equals(node.getChildren(2).getBindType()))  //child one is relOp
			//node.setBindType(node.getleftmostChild().getBindType());  //assign 4 change
			node.setBindType("integer");
		else {
			node.setBindType("typeError");
			SemanticAnalyzer.Success=false;
			System.out.println("Failure,relExpr,Type Error detected between "+node.getleftmostChild().getValue()+" and "+node.getChildren(2).getValue()+" in scope "+searchScope(node).getName()+" at Line "+node.getLine());
		}			
		break;
	}
	case "assignStat":{
		if(node.getleftmostChild().getBindType().equals(node.getChildren(1).getBindType()))
			node.setBindType(node.getleftmostChild().getBindType());
		else {
			node.setBindType("typeError");
			SemanticAnalyzer.Success=false;
			System.out.println("Failure,AssignStat,Type Error detected between "+node.getleftmostChild().getValue()+" and "+node.getChildren(1).getValue()+" in scope "+searchScope(node).getName()+" at Line "+node.getLine());
		}			
		break;
	}
	

	case "indexList":{//count how many dimissions ,if index is not int, error
		int dimission=0;
		ASTnode current=node.getleftmostChild(); 
		while(current!=null) { //indexList won't be epsilon
			if(!current.getBindType().equals("integer")) {
				System.out.println("Failure,array Index is not int for array " +node.getleftmostSibling().getValue() +" in scope "+searchScope(node).getName() +" at Line "+node.getLine());
				node.setBindType("typeError");
				SemanticAnalyzer.Success=false;
				break;
			}
			dimission+=1;
			current=current.getRightSibling();
		}
		if (current==null) //loop to the end
			node.setBindType(Integer.toString(dimission));
		break;
	}
	
	case "var":{
		//there are four types of var 
		//var: children [dataMember]  //single datamember like i , use id search local scope, 
		//var: children [fcall] //free functionCall
		//var: children [dataMember] [fcall] //left is Class, right is memberFunc
		//vaer: children [datamember] [datamember] //left is class ,left is memberFunc
		ASTnode left=node.getleftmostChild();
		ASTnode right=left.getRightSibling();
		if(left.getType().equals("dataMember")&&right==null) {//local dataMember.
			//IMPORTANT:
			//arr, should give type integer[], common
			//arr[2], should give type integer
			//System.out.println("free var "+ left.getleftmostChild().getValue() +" in "+ searchScope(node).getName());
			if(left.getChildren(1).getBindType().equals("0")) //so for common var, if it indexlist.getType=0,
				searchCommonNode(left,searchScope(left));
			else if(!left.getChildren(1).getBindType().equals("0")&&!left.getChildren(1).getBindType().equals("typeError"))
				searchArrNode(left,searchScope(left));
			else if(left.getChildren(1).getBindType().equals("typeError")) //dimension is not int,generated by indexlist
				left.setBindType("typeError");
			
			node.setBindType(left.getBindType());
			//System.out.println(node.getBindType());
		}
		else if(left.getType().equals("fCall")&&right==null) { //free fcall
			searchFunc(left,getGlobal(left));
			node.setBindType(left.getBindType());
			node.getParent().setBindType(node.getBindType());
			//System.out.println(node.getParent().getBindType());
		}
		//=========================================not sure
		else if(left.getType().equals("dataMember")&&right.getType().equals("fCall")) {
			//System.out.println("member Function Call "+ left.getleftmostChild().getValue()+""+right.getleftmostChild().getValue());
			if(left.getChildren(1).getBindType().equals("0")) //so for common var, if it indexlist.getType=0,
				searchCommonNode(left,searchScope(left));
			else if(!left.getChildren(1).getBindType().equals("0")&&!left.getChildren(1).getBindType().equals("typeError"))
				searchArrNode(left,searchScope(left));
			else if(left.getChildren(1).getBindType().equals("typeError")) //dimension is not int,generated by indexlist
				left.setBindType("typeError");
			//Important to give left real class type
			SymbolTable callClass=searchClass(left,getGlobal(left));
			if(callClass!=null) {
				searchFunc(right,callClass); //search right function
				node.setBindType(right.getBindType());
			}			
			//find globalscope, use link find table
		}

		else if(left.getType().equals("dataMember")&&right.getType().equals("dataMember")) {
			//not sure, copy from only dataMember
			if(left.getChildren(1).getBindType().equals("0")) //so for common var, if it indexlist.getType=0,
				searchCommonNode(left,searchScope(left));
			else if(!left.getChildren(1).getBindType().equals("0")&&!left.getChildren(1).getBindType().equals("typeError"))
				searchArrNode(left,searchScope(left));
			else if(left.getChildren(1).getBindType().equals("typeError")) //dimension is not int,generated by indexlist
				left.setBindType("typeError");
			SymbolTable callClass=searchClass(left,getGlobal(left));
			if(callClass!=null) {
				if(right.getChildren(1).getBindType().equals("0")) //so for common var, if it indexlist.getType=0,
					searchCommonNode(right,callClass);
				else if(!right.getChildren(1).getBindType().equals("0")&&!right.getChildren(1).getBindType().equals("typeError"))
					searchArrNode(right,callClass);
				else if(right.getChildren(1).getBindType().equals("typeError")) //dimension is not int,generated by indexlist
					right.setBindType("typeError");
				node.setBindType(right.getBindType());
			}
		}			
		break;
	}
	case "returnStat":{
		//System.out.println(node.getleftmostChild().getBindType());
		ASTnode current=node;
		while(current!=null&&!current.getValue().equals("EPSILON")) {
			if(current.getType().equals("funcDef")) {
				String[] funcType=(current.getSymbolTableRecord().getType().split(":"));
				if(!node.getleftmostChild().getBindType().equals(funcType[0])) {
					SemanticAnalyzer.Success=false;
					System.out.println("Failure,return type are different in "+current.getSymbolTable().getName()+" at Line "+node.getLine());//return type and func type are not same
				}
				break;  //anyway we find funcDef and break while loop
			}			
			current=current.getParent();
		}
		if (current==null)
			System.out.println("Failure,a return statement is not in funcDef (in main stateblock)"+" at Line "+node.getLine());
		break;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	}
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	}//switch one

	//visit one
	/*
	public SymbolTable searchScope(ASTnode node) {
		ASTnode current=node;
		while(current!=null&&!current.getValue().equals("EPSILON")) {
			if(current.getType().equals("classDecl"))
				return current.getSymbolTableRecord().getLink();
			if(current.getType().equals("funcDef")) //free and memberDef function
				return current.getSymbolTableRecord().getLink();
			if(current.getType().equals("prog"))//main statblock
				return current.getChildren(2).getSymbolTable();
			current=current.getParent();
		}
		return null;
	}
	
	public SymbolTable getGlobal(ASTnode node) {
		ASTnode current=node;
		while(current!=null&&!current.getValue().equals("EPSILON")) {
			if(current.getType().equals("prog"))//main statblock
				return current.getSymbolTable();
			current=current.getParent();
		}
		return null;
	}
	*/
	public SymbolTable searchClass(ASTnode node,SymbolTable symtab) {  
		String className=node.getBindType();
		ArrayList<SymbolTableRecord> records=symtab.getTableRecords();
		for (int i=0;i<records.size();i++) {
			if(className.equals(records.get(i).getName())&&records.get(i).getKind().equals("class")) {  //name equal and is class
				return records.get(i).getLink();
			}	                                   
		}
		node.setBindType("typeError");
		System.out.println("Failure,use of undeclared class "+ className +" in "+ searchScope(node).getName()+" at Line "+node.getLine());	
		return null;
	}
	
	public void searchCommonNode(ASTnode node,SymbolTable symtab) {  //node should be datamember or fcall, leftchild MUST be id
		String nodeId=node.getleftmostChild().getValue();
		ArrayList<SymbolTableRecord> records=symtab.getTableRecords();
		for (int i=0;i<records.size();i++) {
			if(nodeId.equals(records.get(i).getName())) { //var parameter, class won't have same name because overwriting
				node.setBindType(records.get(i).getType());  
				//System.out.println(node.getleftmostChild().getValue() +" in "+ searchScope(node).getName()+" type is "+node.getBindType());
				return;
			}	                                   
		}
		node.setBindType("typeError");
		System.out.println("Failure,use of undeclared local variable "+ node.getleftmostChild().getValue() +" in "+ searchScope(node).getName()+" at Line "+node.getLine());		
	}
	
	
	public void searchArrNode(ASTnode node,SymbolTable symtab) {// node like arr[2],   important: arr won't be arraynode,
		String nodeId=node.getleftmostChild().getValue();
		ArrayList<SymbolTableRecord> records=symtab.getTableRecords();
		for (int i=0;i<records.size();i++) 
		{
			if(nodeId.equals(records.get(i).getName())) { //var parameter, class won't have same name because overwriting
				//arr[2] name is arr, so it will search 
				//parameter           |arr                |integer[]                                        |Link:null    
				String type=(records.get(i).getType());
				String[] typearr=type.split("\\[");   // integer | []   
				int dimension=typearr.length-1; 
				if(Integer.toString(dimension).equals(node.getChildren(1).getBindType()))
				{
					node.setBindType(typearr[0]);	
					//System.out.println(node.getleftmostChild().getValue() +" in "+ searchScope(node).getName()+" type is "+node.getBindType());
					return;
				}	                                   
			}
		}
		node.setBindType("typeError");
		System.out.println("Failure,dimensions unmatch at "+ node.getleftmostChild().getValue() +" in "+ searchScope(node).getName()+" at Line "+node.getLine());		
	}
	
	public void searchFunc(ASTnode node,SymbolTable symtab) {// node like arr[2],   important: arr won't be arraynode,
		String nodeId=node.getleftmostChild().getValue();
		
		ArrayList<SymbolTableRecord> records=symtab.getTableRecords();
		for (int i=0;i<records.size();i++) 
		{
			if(nodeId.equals(records.get(i).getName())&&records.get(i).getKind().equals("function")) { //firstly find func with same name
					//function            |bubbleSort         |void: integer[], integer                         |Link:bubbleSort    
					String[] funcArr=records.get(i).getType().split(":");
					//System.out.println(funcArr[1].equals(" "));
					//function with no parameter,still have :
					//function            |test             |void:               

					String[] paraArr=funcArr[1].split(", ");
					if(!paraArr[0].equals(" ")) {
						paraArr[0]=paraArr[0].substring(1, paraArr[0].length());
					}//remove first space
					if(paraArr[0].equals(" ")&&node.getChildren(1).getleftmostChild()==null) {//parameter is empty
						node.setBindType(funcArr[0]);  
						return;
					}
					else 
					{
						int childrenCounter=0;
						ASTnode current=node.getChildren(1).getChildren(0);
						while(current!=null&&!current.getValue().equals("EPSILON")) {
							childrenCounter++;
							current=current.getRightSibling();
						}
						if(childrenCounter==paraArr.length) 
						{ //second check, has same number of params
							boolean reachEnd=true;
							for (int j=0;j<paraArr.length;j++) {
								//System.out.println(paraArr[j]);
								//System.out.println(node.getChildren(1).getChildren(j).getBindType().replaceAll("[0-9]",""));
								if(!paraArr[j].equals(node.getChildren(1).getChildren(j).getBindType().replaceAll("[0-9]",""))) { //one parameter doesn't match
									reachEnd=false;//why replace all num,   parameter is integer[], arr type  is integer[7]
									break;
								}
							}
							
							if(reachEnd==true) { //have same name ,have same paramLength, have same params 
								node.setBindType(funcArr[0]);  
								return;
							}
								
						}
					
					}

				}	                                   
			
		}
		node.setBindType("typeError");
		System.out.println("Failure,undeclared function: "+ node.getleftmostChild().getValue() +" in "+ searchScope(node).getName()+" at Line "+node.getLine());		
	}


}

