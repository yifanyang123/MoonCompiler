package MoonCompiler.codegenerator;

import MoonCompiler.SemanticAnalyzer.SymbolTable;
import MoonCompiler.SemanticAnalyzer.Visitor;
import MoonCompiler.parser.ASTnode;

public class TableSizeVisitor extends Visitor{
		public void visit(ASTnode node) {
			switch(node.getType()) {
				case "statBlock":{
					if(node.getSymbolTable()!=null&&node.getSymbolTable().getTableRecords().size()!=0) {  //main
						addTableSize(node.getSymbolTable(),getGlobal(node));
						addOffSet(node.getSymbolTable());
					}
					break;
				}
				case "funcDef":{
					addTableSize(node.getSymbolTable());
					addOffSet(node.getSymbolTable());
					break;
				}
				case "classDecl":{
					addTableSize(node.getSymbolTable());
					addOffSet(node.getSymbolTable());
					break;
				}
			}
	
		}

		
		private void addTableSize(SymbolTable symbolTable) {
			for(int i=0;i<symbolTable.getTableRecords().size();i++) {
				int basicSize=0;
				String type=(symbolTable.getTableRecords().get(i).getType());
				String[] typearr=type.split("\\[");   // integer | []   
				if(typearr[0].equals("integer")) {
					//symbolTable.getTableRecords().get(i).setSize(4);
					basicSize=4;
				}
				else if(typearr[0].equals("float")) {
					//symbolTable.getTableRecords().get(i).setSize(4);
					basicSize=8;
				}
				else {
					for(int k=0;k<symbolTable.getTableRecords().size();k++) {
						
						if(symbolTable.getTableRecords().get(k).getName().equals(symbolTable.getTableRecords().get(i).getType()))
							System.out.println(symbolTable.getTableRecords().get(k).getName());
					}
					System.out.println();
				}
				int dimension=typearr.length-1;
				int arraySize=1;
				for(int j=0;j<dimension;j++) {
					arraySize=arraySize*(Character.getNumericValue(typearr[j+1].charAt(0)));						
				}
				symbolTable.getTableRecords().get(i).setSize(basicSize*arraySize);	
			}
		}
		
		private void addTableSize(SymbolTable symbolTable,SymbolTable global) {
			for(int i=0;i<symbolTable.getTableRecords().size();i++) {
				int basicSize=0;
				String type=(symbolTable.getTableRecords().get(i).getType());
				String[] typearr=type.split("\\[");   // integer | []   
				if(typearr[0].equals("integer")) {
					//symbolTable.getTableRecords().get(i).setSize(4);
					basicSize=4;
				}
				else if(typearr[0].equals("float")) {
					//symbolTable.getTableRecords().get(i).setSize(4);
					basicSize=8;
				}
				else {
					for(int k=0;k<global.getTableRecords().size();k++) {	
						if(global.getTableRecords().get(k).getKind().equals("class")&&global.getTableRecords().get(k).getName().equals(typearr[0])) {
							basicSize=global.getTableRecords().get(k).getLink().getScopeOffset();
							//System.out.println(basicSize);
						}
							
					}
				}
				int dimension=typearr.length-1;
				int arraySize=1;
				for(int j=0;j<dimension;j++) {
					arraySize=arraySize*(Character.getNumericValue(typearr[j+1].charAt(0)));						
				}
				symbolTable.getTableRecords().get(i).setSize(basicSize*arraySize);	
			}
		}
		private void addOffSet(SymbolTable symbolTable) {
			int accum=0;
			for(int i=0;i<symbolTable.getTableRecords().size();i++) {			
				accum+=symbolTable.getTableRecords().get(i).getSize();
				symbolTable.getTableRecords().get(i).setOffset(accum);	
			}
			symbolTable.setScopeOffset(accum);
		}
		
		private void generateTag(SymbolTable symbolTable) {
			
		}
		
		
}
