package MoonCompiler.parser;

import MoonCompiler.SemanticAnalyzer.SymbolTable;
import MoonCompiler.SemanticAnalyzer.SymbolTableRecord;
import MoonCompiler.SemanticAnalyzer.Visitor;

public class ASTnode {
	private ASTnode parent;
	private ASTnode rightSibling;
	private ASTnode leftmostChild; 
	private ASTnode leftmostSibling;
	private String type;
	private String value;
	private int line=0;
	private SymbolTable symbolTable;
	private SymbolTableRecord symbolTableRecord;
	private String bindType; //used for typechecking, ,maybe not it direct type, binded by its children 
	private String tag;//assign 4 
	private String tempName;//assign 4
	ASTnode(String type,String value,int line){
		this.type=type;
		this.value=value;
		this.line=line;
	}
	
	public void makeSiblings(ASTnode sibling) { //this function won't set you as leftmost sibling if both has no leftmostsibling
		ASTnode current=this;
		while(current.getRightSibling()!=null)
			current=current.getRightSibling();
		ASTnode toAdd;
		if (sibling.getleftmostSibling()!=null)
			toAdd=sibling.getleftmostSibling();
		else
			toAdd=sibling;
		current.setRightSibling(toAdd);
		while(toAdd!=null) { 		
			if(this.leftmostSibling!=null)
				toAdd.setLeftmostSibling(this.leftmostSibling);
			if(this.parent!=null)
				toAdd.setParent(this.parent);
			toAdd=toAdd.getRightSibling();
		}
	}
	
	public void adoptChildren(ASTnode children) {
		if(this.leftmostChild!=null)
			this.leftmostChild.makeSiblings(children);
		else {
			ASTnode toAdd;
			if(children.getleftmostSibling()!=null)
				toAdd=children.getleftmostSibling();
			else
				toAdd=children;
			this.leftmostChild=toAdd;
			while(toAdd!=null) {  //this is not get rightsib()!=null cause  the current loop will handle current node
				toAdd.setParent(this);
				toAdd=toAdd.getRightSibling();
			}
		}
	}
	
	public ASTnode getRightSibling() {
		return rightSibling;
	}
	public String getValue() {
		return value;
	}
	public String getType() {
		return type;
	}
	public int getLine() {
		return line;
	}
	public String getBindType() {
		return bindType;
	}
	public void setBindType(String bindType) {
		this.bindType=bindType;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag=tag;
	}
	public ASTnode getParent() {
		return parent;
	}
	public ASTnode getleftmostSibling() {
		return leftmostSibling;
	}
	public ASTnode getleftmostChild() {
		return leftmostChild;
	}
	public ASTnode getChildren(int i) {
		ASTnode current=leftmostChild;
		while(i>0) {
			current=current.getRightSibling();
			i--;
		}
		return current;
	}
	public void setParent(ASTnode parent) {
		this.parent=parent;
	}
	public void setLeftmostSibling(ASTnode leftmostSibling) {
		this.leftmostSibling=leftmostSibling;
	}
	public void setRightSibling(ASTnode rightSibling) {
		this.rightSibling=rightSibling;
	}
	
	//assign3
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
	public void deleteSymbolTable() {
		this.symbolTable=null;
	}
	public SymbolTableRecord getSymbolTableRecord() {
		return symbolTableRecord;
	}
	
	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable=symbolTable;
	}
	
	public void setSymbolTableRecord(SymbolTableRecord symbolTableRecord) {
		this.symbolTableRecord=symbolTableRecord;
	}
	
	
	public String nodeInfo(){
		return("(type:"+type+",value:"+value+")");
	}
	public String toString() {
		if (this.value!="EPSILON"&&this.leftmostChild!=null) {
		String temp="Node: "+this.value;
		String temp1="";	
		temp=temp+"	Children:";
		//temp1="DetailInfo:"+nodeInfo()+("\n");		   //will Show Detail
		ASTnode current=this.getleftmostChild();
		while(current!=null) {
			temp=temp+"["+current.getValue()+"]";
			temp1=temp1+current.toString();
			current=current.getRightSibling();
		}
		temp=temp+"\n"+temp1;
		return temp;		
		}
		else
			return "";
	}
	
	
	public void accept(boolean auto,Visitor visitor) { //auto means depth-first or manually
		if(auto==true) {
			if (this.value!="EPSILON") {  //epsilon node won't accept any visitor
				if(this.getleftmostChild()!=null) { //composite node
					ASTnode current=this.getleftmostChild();
						while (current!=null) {
							current.accept(true,visitor);
							current=current.getRightSibling();
						}
						visitor.visit(this);
				}
				else if (this.getleftmostChild()==null) { //atomic node
					visitor.visit(this);
				}
			}			
		}
		else if(auto==false){
			visitor.visit(this);
		}

	}
	

	
	
	
	
}
