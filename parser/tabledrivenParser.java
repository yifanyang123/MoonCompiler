package MoonCompiler.parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;

import MoonCompiler.lexer.token;

public class tabledrivenParser {
	private ArrayList<token> inputToken;
	private Hashtable parsingTable;
	private Stack<String> parser;
	private HashSet terminal;
	private HashSet nonTerminal;
	private Hashtable First;
	private Hashtable Follow;
	private String filePath;
	private boolean success;
	private int CurrentInput=0;
	private ASTtree myAST;
	public tabledrivenParser(ArrayList<token> inputToken,Hashtable parsingTable,HashSet terminal,HashSet nonTerminal,String filePath,Hashtable first,Hashtable follow) {
		this.inputToken=inputToken;
		this.parsingTable=parsingTable;
		this.inputToken.add(new token("$","$",999));
		this.parser=new Stack<String>();
		this.terminal=terminal;
		this.nonTerminal=nonTerminal;
		this.filePath=filePath;
		this.First=first;
		this.Follow=follow;
		this.success=true;
		this.myAST=new ASTtree(); 
	}
	public void parse() {
		PrintWriter pw=null;
		PrintWriter errorWriter=null;
		PrintWriter astTreeWriter=null;
		try
		{
			 pw = new PrintWriter(new FileOutputStream(filePath+".outderivation", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}
		try
		{
			 astTreeWriter = new PrintWriter(new FileOutputStream(filePath+".outAST", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}
		try
		{
			errorWriter = new PrintWriter(new FileOutputStream(filePath+".outSyntaxError", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}

		parser.push("$");
		parser.push("<START>");
		
		/*
		ASTnode temp1=new ASTnode("num","1");
		ASTnode temp2=new ASTnode("num","2");
		ASTnode temp3=new ASTnode("operand","+");
		ASTnode temp4=new ASTnode("list","list");
		Stack tempStack=myAST.getNodeStack();
		tempStack.push(temp1);
		tempStack.push(temp2);
		myAST.makeFamily(temp3,2,true);
		myAST.makeFamily(temp4);
		System.out.println(temp4.toString());
		System.out.println(temp2.getleftmostSibling());
		System.out.println(temp1.getleftmostSibling());
		System.out.println(temp3.getleftmostChild().getRightSibling().toString());
		System.out.println(temp3.toString());
		*/
		token currentToken=inputToken.get(CurrentInput);
		
		while (currentToken.getType()!="$"){
			currentToken=inputToken.get(CurrentInput);
			//System.out.println(parser.toString());
			//==============================================================================================================================
			//Important!!!!!!!!!!!!!!!!!!!!!!!!!:
			//Reference: I use the basic idea from https://github.com/y785wang/COMP_6421_Compiler to generate my astTree  
			//To be specefic, the idea is adding integer manually into rules, the integer means corresponding semantic action
			//But the detail code are written by myself
			//There are two main differences 
			//The first is the structure of node ,I add link to leftmostSibling, so my code have more complex addSibling,adoptChildren, and my code support combine two node_link)
			//The second is my ASTtree, I use DISTRIBUTED buffer instead of single stack to store children nodes, when all children nodes are ready, the father node will adopt all children. 
			//which makes my semantic action are completely different, and easier to debug the rules.
			//Yifan Yang 40038814
			//2020/03/02
			//==============================================================================================================================
			if (currentToken.getType()=="inlinecmt"||currentToken.getType()=="blockcmt") {  //skip comment token
				CurrentInput++;
				continue;
			}
			//ast part
			
			String tempAction=parser.peek();  //check whether it is int, so you can know whether it is semantic action
			int semanticAction;
            try {
                semanticAction = Integer.parseInt(tempAction);
                if (success) {  //no error occured till now
                    myAST.doSemanticAction(semanticAction, currentToken);                 	
                }
                parser.pop();
                
                continue;
            } catch (NumberFormatException e) {
                System.out.print("");
            }
            
            //===========================================================================================================================
			//System.out.println(currentToken.getType()); //to ensure you can get end 
			if (terminal.contains(parser.peek().replaceAll("\\'",""))) {
				if (parser.peek().replaceAll("\\'","").equals(currentToken.getType())) {  //terminal=terminal
					//System.out.println(parser.peek()+"matched!!!");
					parser.pop();
					CurrentInput++;
				}
				else {
					skipError(errorWriter);
					//System.out.println("terminalerror");
					success=false;					
				}
			}
			else if (nonTerminal.contains(parser.peek())) {				
				if(("error")!=(parsingTable.get(currentToken.getType().concat(parser.peek())))) {
					String currentKey=currentToken.getType().concat(parser.peek());
					/*
					if (parsingTable.get(currentKey)==null) {
						i++;
						continue;					//for test use
					}*/
					String currentRule=(String) parsingTable.get(currentKey);				
					//System.out.println(parsingTable.get("main<prog>"));
					String[] rhs=currentRule.split(" ::= ");
					String[] temp=rhs[1].split("\\s+");
					parser.pop();
					//System.out.println(temp.length);
					for (int j=temp.length-1;j>=0;j--) {
						if(temp[j].compareTo("EPSILON")!=0) {
						parser.push(temp[j]);					
						}
					}
					/*System.out.println(currentRule);
					System.out.println("current STACK:"+parser.toString());
					System.out.println("current input:"+inputToken.get(CurrentInput).getType());
					System.out.println("nextInput:"+inputToken.get(CurrentInput+1).getType()); //for debug*/
					pw.println(currentRule);
					pw.println("current STACK:"+parser.toString());
					pw.println("current input:"+inputToken.get(CurrentInput).getType());
					pw.println("nextInput:"+inputToken.get(CurrentInput+1).getType());
				}
				else {
					skipError(errorWriter);
					//System.out.println("nonterminal error");
					success=false;
				}

			}
			//System.out.println("can reach end");

	}
	if(success==true) {
		System.out.println("successful");
		astTreeWriter.println(myAST.getNodeStack().peek().toString());
	}	
	else if(success==false)
		pw.println("unsuccessful");
	pw.close();
	errorWriter.close();
	astTreeWriter.close();
	}
	
	
	
	public void skipError(PrintWriter errorWriter) {

		token currentToken=inputToken.get(CurrentInput);
		boolean terminalFlag = false;
		String currentTop=null;		
		HashSet follow=new HashSet();
		HashSet first=new HashSet();
		if(parser.peek().charAt(0)=='\'')
		{
			terminalFlag=true;
			currentTop=parser.peek().replaceAll("\\'","");
			//System.out.println(currentTop);
			first.add(currentTop);
		}
		else if(parser.peek().charAt(0)=='<'){
			terminalFlag=false;
			currentTop=parser.peek();
			follow=(HashSet)Follow.get(currentTop);
			first=(HashSet)First.get(currentTop);
		} //Ã»ÓÐdollar	
		System.out.println("Syntax Error At !!!!!!!!!!!:"+currentToken);
		errorWriter.println("Syntax Error At:"+currentToken);
		
		if(currentToken.getType()=="$") {
			while(parser.peek()!="$") {
				errorWriter.println("input stack reaches end! parser pop:"+parser.peek());
				parser.pop();
			}
			//pw.close();
		}
		else if (terminalFlag==true) {
			parser.pop();
		}
		else if (terminalFlag==false) {
			if(!(first.contains("EPSILON"))&&follow.contains(currentToken.getType())) //sync
				parser.pop();				
			else 
				CurrentInput++;									
			}
		//school version
		/*if(currentToken.getType()=="$"||(terminalFlag==false&&follow.contains(currentToken.getType()))) {
			parser.pop();
		}
		else 
			while(!first.contains(currentToken.getType())||(first.contains("EPSILON")&&!follow.contains(currentToken.getType()))) {
				//System.out.println(CurrentInput);
				CurrentInput++;				
			}		
		*/	
		//System.out.println("continue"); //for test use
		
	}
	
	
	public ASTtree getASTtree() {
		return myAST;
	}
	
	
	
	
	
	
}
