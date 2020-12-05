package MoonCompiler.SemanticAnalyzer;

import java.util.ArrayList;

import MoonCompiler.parser.ASTnode;

public class SymTabCreationVisitor extends Visitor {
	
	public void visit(ASTnode node) {
		
		
		switch(node.getType()) { //I didn't divide node to different class, use switch instead
		case "prog":{	
			node.setSymbolTable(new SymbolTable("global",null));  //phase 1
			ASTnode currentClass=node.getleftmostChild().getleftmostChild();
			while(currentClass!=null&&!currentClass.getValue().equals("EPSILON")) { //insert all classEntry        //phase 2
				node.getSymbolTable().insert(currentClass.getSymbolTableRecord());
				currentClass=currentClass.getRightSibling();
			}
			
			ASTnode currentFunc=node.getleftmostChild().getRightSibling().getleftmostChild();
			while(currentFunc!=null&&!currentFunc.getValue().equals("EPSILON")) { //insert only free function
				if(currentFunc.getChildren(1).getValue().equals("fparamList"))
				node.getSymbolTable().insert(currentFunc.getSymbolTableRecord());
				currentFunc=currentFunc.getRightSibling();
			}
			
			ASTnode main=node.getleftmostChild().getRightSibling().getRightSibling();
		    SymbolTable maintable = main.getSymbolTable(); 
		    node.getSymbolTable().insert(main.getSymbolTableRecord());		      
		    break;
		  
		}
		case "classDecl":{
			String className=node.getleftmostChild().getValue();
            ASTnode inherit = node.getleftmostChild().getRightSibling().getleftmostChild();    //now only accept one parent
            String inheritName=inherit.getValue();
            if(!inheritName.equals("EPSILON")) {
            	ASTnode current=node.getleftmostSibling();//left most classdecl
            	while(current!=null) {
            		if(current.getleftmostChild().getValue().equals(inheritName))
            			break;
            		current=current.getRightSibling();
            	}
            	if(current==null) {
            		System.out.println("Failure,no such parent for "+className+" at Line "+node.getLine()+" at Line "+node.getLine());  
    				SemanticAnalyzer.Success=false;
            	}
            		
            	else {
                	if(current.getSymbolTable()!=null)
                		node.setSymbolTable(new SymbolTable(className,current.getSymbolTable()));//current will be parent classdecl node
                	else
                		node.setSymbolTable(new SymbolTable(className,new SymbolTable(inheritName,null))); //special empty symboltable for inherit Circular
            	}
            }
            else
            	node.setSymbolTable(new SymbolTable(className,null));//no parent
            ASTnode membList=node.getleftmostChild().getRightSibling().getRightSibling();
            ASTnode currentMember=membList.getleftmostChild();
            while(currentMember!=null&&!currentMember.getValue().equals("EPSILON")) {
            	node.getSymbolTable().insert((currentMember.getSymbolTableRecord()));
            	currentMember=currentMember.getRightSibling();
            }
            node.setSymbolTableRecord(new SymbolTableRecord(className,"class","",node.getSymbolTable()));
            //phase 1            
            break;	
		}
		case "varDecl":{
			String varName=node.getleftmostChild().getRightSibling().getValue();
			String varType=varParamType(node);
			node.setSymbolTableRecord(new SymbolTableRecord(varName,"variable",varType,null));
			break;
		}
		case "funcDecl":{ //param dim checked //first Check
			String funcName=node.getleftmostChild().getValue();
			String funcType=fparamType(node);
			node.setSymbolTableRecord(new SymbolTableRecord(funcName,"function",funcType,node.getSymbolTable()));
			break;
			}
		case "funcDef":{
			if(node.getChildren(2).getValue().equals("fparamList")) {// third one is fparamlist, then it is not free
				//try to find the original funcDecl, cause java is object-oriented ,use this.record to point to fdecl.record
				//then when you change this.record.symboltable,will change fdecl.record.symboltable
				ASTnode currentClassDecl=node.getParent().getleftmostSibling().getleftmostChild();
				while (currentClassDecl!=null&&!currentClassDecl.getValue().equals("EPSILON")) {
					if(currentClassDecl.getleftmostChild().getValue().equals(node.getleftmostChild().getValue())) {//match class name
						ASTnode currentMember=currentClassDecl.getChildren(2).getleftmostChild();//memblist's first child
						while (currentMember!=null&&!currentMember.getValue().equals("EPSILON")) {
							SymbolTableRecord temp=currentMember.getSymbolTableRecord();
							if(temp.getKind().equals("function")&&temp.getName().equals(node.getChildren(1).getValue())&&temp.getType().equals(fparamType(node))) {//function, same name ,same type
								node.setSymbolTableRecord(temp);//!!!!!!share record when matched, and then you change funcdef record will change funcDecl record, then you can add list!!!KEY OF DOUBLE CHECK	
								break;
							}								
							currentMember=currentMember.getRightSibling();
						}
						if (currentMember==null||currentMember.getValue().equals("EPSILON")) {
							System.out.println("Failure,definition provided for undeclared member function:" +node.getChildren(1).getValue()+" at Line "+node.getLine());//find class, no such decl //phase 6
							SemanticAnalyzer.Success=false;
						}
							
						break;//searched right class, but no such function
					}					
					currentClassDecl=currentClassDecl.getRightSibling();
				}
				if(currentClassDecl==null||currentClassDecl.getValue().equals("EPSILON")) {
					System.out.println("Failure,no such class "+node.getleftmostChild().getValue()+" at Line "+node.getLine());
					SemanticAnalyzer.Success=false;
				}
				if(node.getSymbolTableRecord()!=null) {
					String tableName=node.getleftmostChild().getValue()+": "+node.getChildren(1).getValue();
					node.getSymbolTableRecord().setLink(new SymbolTable(tableName,null));
					SymbolTable funcTab=node.getSymbolTableRecord().getLink();
					node.setSymbolTable(funcTab);
					//binding done, funcTab is the table of this function
					ASTnode currentFparam=node.getChildren(2).getleftmostChild();
					while(currentFparam!=null&&!currentFparam.getValue().equals("EPSILON")) {
						String paramName=currentFparam.getleftmostChild().getRightSibling().getValue();
						String paramType=varParamType(currentFparam);
						funcTab.insert(new SymbolTableRecord(paramName,"parameter",paramType,null));
						currentFparam=currentFparam.getRightSibling();
					}				
					//param done  //temp,statblock should already have table
					ASTnode statBlock=node.getChildren(4);
					ArrayList<SymbolTableRecord> currentRecords=statBlock.getSymbolTable().getTableRecords();
					for(int i=0;i<currentRecords.size();i++) {
						funcTab.insert(currentRecords.get(i)); 
					}
					statBlock.deleteSymbolTable();
					//add variable and delete statBlock table if it is not main
				}		
			}
			else {		
				String tableName=node.getleftmostChild().getValue();
				node.setSymbolTableRecord(new SymbolTableRecord(tableName,"function",fparamType(node),new SymbolTable(tableName,null)));
				SymbolTable funcTab=node.getSymbolTableRecord().getLink();
				node.setSymbolTable(funcTab);
				//binding done, funcTab is the table of this function
				
				ASTnode currentFparam=node.getChildren(1).getleftmostChild();
				while(currentFparam!=null&&!currentFparam.getValue().equals("EPSILON")) {
					String paramName=currentFparam.getleftmostChild().getRightSibling().getValue();
					String paramType=varParamType(currentFparam);
					funcTab.insert(new SymbolTableRecord(paramName,"parameter",paramType,null));
					currentFparam=currentFparam.getRightSibling();
				}				
				//param done  //temp,statblock should already have table
				ASTnode statBlock=node.getChildren(3);
				ArrayList<SymbolTableRecord> currentRecords=statBlock.getSymbolTable().getTableRecords();
				for(int i=0;i<currentRecords.size();i++) {
					funcTab.insert(currentRecords.get(i)); 
				}
				statBlock.deleteSymbolTable();
				//add variable and delete statBlock table if it is not main
			}	
			break;
		}
		case "statBlock":{
			node.setSymbolTable(new SymbolTable("main",null));
			node.setSymbolTableRecord(new SymbolTableRecord("main","function",null,node.getSymbolTable()));
			ASTnode currentStat=node.getleftmostChild();//var or stat
			while(currentStat!=null&&!currentStat.getValue().equals("EPSILON")) {
				if(currentStat.getValue().equals("varDecl"))
					node.getSymbolTable().insert(currentStat.getSymbolTableRecord());	//cause it is varDecl,already defined							
				currentStat=currentStat.getRightSibling();				
			}
			
			break;
		}
		case "funcDefList":{ //all funcDef are done, can check phase 6 "no definition for declared member function"
			ASTnode currentClass=node.getleftmostSibling().getleftmostChild();
			while(currentClass!=null&&!currentClass.getValue().equals("EPSILON")) {
				ArrayList<SymbolTableRecord> currentRecords=currentClass.getSymbolTable().getTableRecords();
				for(int i=0;i<currentRecords.size();i++){ //loop every record
					if(currentRecords.get(i).getKind().equals("function")) {//currentRecord is function
						ASTnode currentfuncDef=node.getleftmostChild();
						while(currentfuncDef!=null&&!currentfuncDef.equals("EPSILON")) {//loop all funcDecl
							if(currentRecords.get(i).getScope().equals(currentfuncDef.getleftmostChild().getValue()) //match scope
							&&currentRecords.get(i).getName().equals(currentfuncDef.getChildren(1).getValue()) //match name
							&&currentRecords.get(i).getType().equals(fparamType(currentfuncDef)))
								break;//find the definition, break;
							currentfuncDef=currentfuncDef.getRightSibling();
						}
						if (currentfuncDef==null) {//loop all and cannot match
							System.out.println("Failure,no definition for declared member function "+currentRecords.get(i).getScope()+": "+currentRecords.get(i).getName()+" at Line "+node.getLine());//phase 6	
							SemanticAnalyzer.Success=false;
						}
					}						
				}
				currentClass=currentClass.getRightSibling();
			}
				
			break;
		}
			
		
		
		}
		
		
	}
	
	public String fparamType(ASTnode node){
		String funcType="";
		if(node.getChildren(1).getValue().equals("fparamList")) { //funcDecl or free funcDef
			funcType=node.getChildren(2).getValue()+": "; //return type
			ASTnode fparamList=node.getChildren(1);
			if (fparamList.getleftmostChild().getValue().equals("EPSILON"))
					funcType=funcType;
			else {
					ASTnode currentfparam=fparamList.getleftmostChild();
					while(currentfparam!=null) 
					{	
					String paramType=currentfparam.getleftmostChild().getValue();
						//dim part
						ASTnode dimList=currentfparam.getChildren(2);
						if (dimList.getleftmostChild().getValue().equals("EPSILON"))
								paramType=paramType;
						else {
							ASTnode currentDim=dimList.getleftmostChild();
							while(currentDim!=null) {
								if(currentDim.getValue().equals("]"))
									paramType=paramType+"[]";
								else //integer
									paramType=paramType+("["+currentDim.getValue()+"]");
									currentDim=currentDim.getRightSibling();
								}
							}
					//then we will get fparam with dimlist
					funcType=funcType+paramType+", ";
					currentfparam=currentfparam.getRightSibling();
					}
					funcType=funcType.substring(0, funcType.length()-2);
				}//funcDecl or free func
		}
		else if (node.getChildren(2).getValue().equals("fparamList")){ //member funcDef
			funcType=node.getChildren(3).getValue()+": ";
			ASTnode fparamList=node.getChildren(2);
			if (fparamList.getleftmostChild().getValue().equals("EPSILON"))
					funcType=funcType;
			else {
					ASTnode currentfparam=fparamList.getleftmostChild();
					while(currentfparam!=null) 
					{	
					String paramType=currentfparam.getleftmostChild().getValue();
						//dim part
						ASTnode dimList=currentfparam.getChildren(2);
						if (dimList.getleftmostChild().getValue().equals("EPSILON"))
								paramType=paramType;
						else {
							ASTnode currentDim=dimList.getleftmostChild();
							while(currentDim!=null) {
								if(currentDim.getValue().equals("]"))
									paramType=paramType+"[]";
								else //integer
									paramType=paramType+("["+currentDim.getValue()+"]");
									currentDim=currentDim.getRightSibling();
								}
							}
					//then we will get fparam with dimlist
					funcType=funcType+paramType+", ";
					currentfparam=currentfparam.getRightSibling();
					}
					funcType=funcType.substring(0, funcType.length()-2);
				}//funcDecl or free func
		}
			
		
		return funcType;		
	}
	
	public String varParamType(ASTnode node) {
		String varType=node.getleftmostChild().getValue();
		ASTnode dimList=node.getleftmostChild().getRightSibling().getRightSibling();
		if (dimList.getleftmostChild().getValue().equals("EPSILON"))
				varType=varType;
		else {
			ASTnode currentDim=dimList.getleftmostChild();
			while(currentDim!=null) {
				if(currentDim.getValue().equals("]")) //[]
					varType=varType+"[]";
				else //integer
					varType=varType+("["+currentDim.getValue()+"]");
					currentDim=currentDim.getRightSibling();
				}
			}
		return varType;
	}
	
	
}
