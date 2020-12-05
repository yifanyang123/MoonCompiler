package MoonCompiler.SemanticAnalyzer;

import MoonCompiler.parser.ASTnode;


public class SymTabCreationVisitorPhase2 extends Visitor {
	public void visit(ASTnode node) {		
		switch(node.getType()) {
		case "classDecl":{
			ASTnode currentMember=node.getChildren(2).getleftmostChild();
			while(currentMember!=null&&!currentMember.getValue().equals("EPSILON")) {
				if(currentMember.getSymbolTableRecord().getKind().equals("function")) {
					ASTnode currentVariable=node.getChildren(2).getleftmostChild();
					while(currentVariable!=null&&!currentVariable.getValue().equals("EPSILON")) {
						if(currentVariable.getSymbolTableRecord().getKind().equals("variable")&&currentMember.getSymbolTableRecord().getLink()!=null) {
							currentMember.getSymbolTableRecord().getLink().getTableRecords().add(currentVariable.getSymbolTableRecord());  //won't send clone, function change should change data Member//won't insert, will change scope
						}
							
						currentVariable=currentVariable.getRightSibling();
					}
				}
				currentMember=currentMember.getRightSibling();
			}
			break;
		}
		

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		}
		
		
				
	}
}

