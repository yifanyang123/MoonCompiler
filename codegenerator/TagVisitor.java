package MoonCompiler.codegenerator;

import MoonCompiler.SemanticAnalyzer.SemanticAnalyzer;
import MoonCompiler.SemanticAnalyzer.SymbolTable;
import MoonCompiler.SemanticAnalyzer.SymbolTableRecord;
import MoonCompiler.SemanticAnalyzer.Visitor;
import MoonCompiler.parser.ASTnode;

public class TagVisitor extends Visitor {
	static int i;
	public void visit(ASTnode node) {
		switch(node.getType()) {
		case "addOp":{ //assignment3 new change ,check astnode case 33   + - or		
			String name="tadd"+i;
			node.setTag(searchScope(node).getName()+name);
			node.setSymbolTableRecord(new SymbolTableRecord(name, "variable", node.getBindType(), null));
			searchScope(node).insert(node.getSymbolTableRecord());
			i++;
			break;
		}
		case "multOp":{ //assignment3 new change ,check astnode case 37   * / and	
			String name="tmul"+i;
			node.setTag(searchScope(node).getName()+name);
			node.setSymbolTableRecord(new SymbolTableRecord(name, "variable", node.getBindType(), null));
			searchScope(node).insert(node.getSymbolTableRecord());
			i++;
		break;
		}
		case "relExpr": {//special one, should get second child
			String name="trel"+i;
			node.setTag(searchScope(node).getName()+name);
			node.setSymbolTableRecord(new SymbolTableRecord(name, "variable", node.getBindType(), null));
			searchScope(node).insert(node.getSymbolTableRecord());
			i++;
		break;
		}
		case "not": {//special one, should get second child
			String name="tnot"+i;
			node.setTag(searchScope(node).getName()+name);
			node.setSymbolTableRecord(new SymbolTableRecord(name, "variable", node.getBindType(), null));
			searchScope(node).insert(node.getSymbolTableRecord());
			i++;
		break;
		}
		case "sign": {//special one, should get second child
			String name="tsign"+i;
			node.setTag(searchScope(node).getName()+name);
			node.setSymbolTableRecord(new SymbolTableRecord(name, "variable", node.getBindType(), null));
			searchScope(node).insert(node.getSymbolTableRecord());
			i++;
		break;
		}
		case "statBlock":{ //now only for main 
			if(node.getSymbolTable()!=null&&node.getSymbolTable().getTableRecords().size()!=0) {  //main
				addTableTag(node.getSymbolTable());
			}
		break;
		}
		case "funcDef":{
			String name=node.getleftmostChild().getValue();
			String[] fparamType=node.getSymbolTableRecord().getType().split(": ");
			String[] divideFparam=fparamType[1].split(", ");
			for(int i=0;i<divideFparam.length;i++) {
				name=name+divideFparam[i];
			}
			node.setTag(name);
			node.getSymbolTableRecord().setTag(name);
			if(node.getSymbolTable()!=null&&node.getSymbolTable().getTableRecords().size()!=0) {  //main
				addTableTag(node.getSymbolTable());
			}
			break;
		}
		}
	}
	
	
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
	private void addTableTag(SymbolTable symbolTable) {
		for(int i=0;i<symbolTable.getTableRecords().size();i++) {
			if(symbolTable.getTableRecords().get(i).getKind().equals("variable"))
				symbolTable.getTableRecords().get(i).setTag(symbolTable.getName()+symbolTable.getTableRecords().get(i).getName());
		}
	}
}
