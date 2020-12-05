package MoonCompiler.SemanticAnalyzer;

import MoonCompiler.parser.ASTnode;

public abstract class Visitor {
	public void visit(ASTnode node) {};
	public SymbolTable searchScope(ASTnode node) {
		ASTnode current=node;
		while(current!=null&&!current.getValue().equals("EPSILON")) {
			if(current.getType().equals("classDecl"))
				return current.getSymbolTableRecord().getLink();
			if(current.getType().equals("funcDef")&&current.getSymbolTableRecord()!=null) //free and memberDef function
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
	

}
