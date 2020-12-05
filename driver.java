package MoonCompiler;
import java.io.*;
import java.util.*;

import MoonCompiler.SemanticAnalyzer.SemanticAnalyzer;
import MoonCompiler.SemanticAnalyzer.SymbolTable;
import MoonCompiler.SemanticAnalyzer.SymbolTableRecord;
import MoonCompiler.codegenerator.CodeGenerator;
import MoonCompiler.lexer.lexer;
import MoonCompiler.lexer.token;
import MoonCompiler.parser.FirstFollowReader;
import MoonCompiler.parser.parsingTable;
import MoonCompiler.parser.tabledrivenParser;


public class driver {
	
	public static void main(String[] args) {
		Scanner kb = new Scanner(System.in);
		ArrayList<token> tokenList=new ArrayList<token>();
		String filePath = new File("").getAbsolutePath()+"\\src\\MoonCompiler";
		String mySrc=filePath+"\\moonCode\\simplemain";
		String myRules=filePath+"\\rule\\final";
		lexer myLexer=new lexer(mySrc);
		
		//lexer
		myLexer.generate();
		tokenList=myLexer.getToken();
		
		//parser
		FirstFollowReader myReader=new FirstFollowReader(myRules);
		myReader.generate();
		
		parsingTable parsingTable=new parsingTable(myReader.getTerminal(),myReader.getNonTerminal(),myReader.getRule(),myReader.getFirst(),myReader.getFollow());
		parsingTable.generateTable();	
		
		Hashtable myTable=parsingTable.getParsingTable();
		//parsingTable.display();
		tabledrivenParser parser=new tabledrivenParser(tokenList,myTable,myReader.getTerminal(),myReader.getNonTerminal(),mySrc,myReader.getFirst(),myReader.getFollow());
		parser.parse();
	
		//semantic analysis
		SemanticAnalyzer semanticAnalyzer=new SemanticAnalyzer(parser.getASTtree(),mySrc);
		semanticAnalyzer.generate();
	
		//code generation
		if(semanticAnalyzer.getSuccess()) {
			CodeGenerator codeGenerator=new CodeGenerator(parser.getASTtree(),mySrc);
			codeGenerator.generate();
		}
		else
			System.out.println("There is error, wont generate code");
		
		/*
		System.out.println(tokenList);
		
		//Hashtable myFirst=myReader.getFirst(); //test first
		//System.out.println(myFirst.toString());
		
		//Hashtable myFollow=myReader.getFollow(); //test first
		//System.out.println(myFollow.toString());
		
		//HashSet terminal=myReader.getTerminal(); //test terminal
		//terminal.forEach(i -> System.out.println(i)); 
		//HashSet nonTerminal=myReader.getNonTerminal(); //test terminal
		//nonTerminal.forEach(i -> System.out.println(i)); 
		/*
		ArrayList<String> rules= myReader.getRule();
		for(int i=0;i<rules.size();i++)
		System.out.println(rules.get(i));
		*/
	
		//parsingTable.display();

		
		
		
	}
}



