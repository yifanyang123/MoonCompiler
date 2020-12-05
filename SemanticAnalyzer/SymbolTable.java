package MoonCompiler.SemanticAnalyzer;

import java.util.ArrayList;
import java.util.Collections;

public class SymbolTable {
	private String name;
	private ArrayList<SymbolTableRecord> tableRecords;
	private SymbolTable parent;
	private int scopeOffSet;
	public SymbolTable(String name,SymbolTable parent) {
		this.name=name;
		this.tableRecords=new ArrayList<SymbolTableRecord>();
		this.parent=parent;
		SymbolTable current=parent;
		while(current!=null) {
			if(current.getName().equals(this.getName())) {
				System.out.println("Failure,Circular class dependencies:" +name);
				SemanticAnalyzer.Success=false;
			}
			current=current.getParent();
		}
		if (parent!=null)
		for(SymbolTableRecord i : parent.getTableRecords()) { //checked 
			if (i.getScope().equals(this.getName())) {
				System.out.println("Failure,Circular class dependencies:" +name);  //phase 14//means parent has already have its record
				SemanticAnalyzer.Success=false;
			}
		    this.tableRecords.add(new SymbolTableRecord(i)); //use clone record method
		}
	}
	
	public SymbolTable(SymbolTable clone) { //checked
		this.name=clone.getName();
		this.tableRecords=new ArrayList<SymbolTableRecord>();
		this.parent=parent;
		this.scopeOffSet=scopeOffSet;
		if(clone.getTableRecords().size()!=0) {
			for(SymbolTableRecord i : clone.getTableRecords()) {
			    this.tableRecords.add(new SymbolTableRecord(i));//use clone record method
			}
		}

	}

	
	public void insert(SymbolTableRecord newRecord) {
		newRecord.setScope(this.getName());
		int result=Search(newRecord);
		//System.out.println(result);
		if (result==1) {
			tableRecords.add(newRecord); 		
		}
		else if (result==2) {
			System.out.println("Failure,multiply declared "+newRecord.getKind()+" "+newRecord.getName()); //phase 8
			SemanticAnalyzer.Success=false;
		}
		else if (result==3) { //rule 5 overWrite phase 5 //checked
			for (int i=0;i<tableRecords.size();i++) {
				if (newRecord.getKind().equals("function")) {
					if (tableRecords.get(i).getName().equals(newRecord.getName())&&
						tableRecords.get(i).getKind().equals(newRecord.getKind())&&
						tableRecords.get(i).getType().equals(newRecord.getType())){
						tableRecords.remove(i);
						tableRecords.add(newRecord);
						System.out.println("Warning:OverWriting function "+newRecord.getName()+" "+newRecord.getType()+" in class "+newRecord.getScope());
						break;
					}					
				}
				else if(newRecord.getKind().equals("parameter")||newRecord.getKind().equals("variable")||newRecord.getKind().equals("class"))
				{
					if (tableRecords.get(i).getName().equals(newRecord.getName())&&
						tableRecords.get(i).getKind().equals(newRecord.getKind()))
						{
							tableRecords.remove(i);
							tableRecords.add(newRecord);
							System.out.println("Warning:OverWriting "+newRecord.getKind()+newRecord.getName()+" in class "+newRecord.getScope());
							break;
						}
				}
			}				
		}
		else if (result==4) { //CHECKED
			tableRecords.add(newRecord);
			System.out.println("Warning:OverLoading "+newRecord.getKind()+" "+newRecord.getName());    //phase 9 overLoad
		}
	}
	
	public int Search(SymbolTableRecord searchRecord) {
		// in same scope
		if(searchRecord.getKind().equals("parameter")||searchRecord.getKind().equals("variable")||searchRecord.getKind().equals("class")) 
		{ 	
			for (int i=0;i<tableRecords.size();i++) {
					if (tableRecords.get(i).getName().equals(searchRecord.getName())&&//if name equals
						tableRecords.get(i).getKind().equals(searchRecord.getKind())){
						if(tableRecords.get(i).getScope().equals(searchRecord.getScope()))
							return 2;//if searchRecord.getscope= oldrecord.getscope()//IN SAME SCOPE, and name equal retur 2, fail
						else if(!tableRecords.get(i).getScope().equals(searchRecord.getScope()))
							return 3;//if searchRecord.getscope!=oldscope(),//not in same scope , return 3 ,overwriting
					}									
			}			
			return 1;		//for variable, parameter, class, if name &&kind not equal return 1 success			
		}
		else if (searchRecord.getKind().equals("function"))
		{
			for (int i=0;i<tableRecords.size();i++) {
				if (tableRecords.get(i).getName().equals(searchRecord.getName())&& //name equal
					tableRecords.get(i).getKind().equals(searchRecord.getKind())){
					if (tableRecords.get(i).getType().equals(searchRecord.getType())) { //paramlist equal
						if(tableRecords.get(i).getScope().equals(searchRecord.getScope()))
							return 2;//IN SAME SCOPE, and name equal return 2, fail
						else if(!tableRecords.get(i).getScope().equals(searchRecord.getScope()))
							return 3;//not in same scope , return 3 ,overwriting
					}
					else if (!tableRecords.get(i).getType().equals(searchRecord.getType()))
						return 4;//if name equal ,paramList not equal, return 4 //overload
				}									
		}			
		return 1;		//for function, if name && kind not equal,  return 1 success		
		}
			   return 0; 		//error		
	}

	
	
	
	public ArrayList<SymbolTableRecord> getTableRecords(){
		return tableRecords;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name=name;
	}
	public SymbolTable getParent() {
		return parent;
	}
	public void setScopeOffset(int scopeOffset) {
		this.scopeOffSet=scopeOffset;
	}
	public int getScopeOffset() {
		return this.scopeOffSet;
	}
	
	public SymbolTableRecord searchNoFuncName(String searchName) {
		for (int i=0;i<tableRecords.size();i++) {
			if(!tableRecords.get(i).getKind().equals("function")&&tableRecords.get(i).getName().equals(searchName))
				return tableRecords.get(i);		
			}	
		System.out.println("no such records");
		return null;		
	}
	
	
	public String toString() {
		String temp="";
		temp=temp+"============================="+name+":startline================================================================================================\n";
		temp=temp+String.format("%-20s","table: "+this.getName())+String.format("%-20s","|OffSet:"+ this.getScopeOffset())+"\n";                                      
		temp=temp+"-----------------------------"+name+":recordline-----------------------------------------------------------------------------------------------\n";
		if(this.getParent()!=null)
			temp=temp+String.format("%-20s", "inherit")+"|"+this.getParent().getName()+"\n";
		for(SymbolTableRecord i : this.getTableRecords()) {
		    if (i.getScope().equals(this.getName())) {
		    	temp=temp+i.toString()+"\n";
		    	if (i.getLink()!=null)
		    		temp=temp+i.getLink().toString();
		    }
		}
		temp=temp+"============================="+name+":endline==================================================================================================\n";
		return temp;
	}
}
