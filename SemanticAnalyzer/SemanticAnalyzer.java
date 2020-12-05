package MoonCompiler.SemanticAnalyzer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import MoonCompiler.parser.ASTtree;

public class SemanticAnalyzer {
	private ASTtree myast;
	private String filePath;
	protected static boolean Success=true;
	public SemanticAnalyzer(ASTtree ast,String filePath) {
		myast=ast;
		this.filePath=filePath;
	}
	
	public boolean getSuccess() {
		return Success;
	}
	
	public void generate() {
		boolean display=false;
		PrintWriter pw=null;
		try
		{
			 pw = new PrintWriter(new FileOutputStream(filePath+".outSymbTab", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}
		PrintStream out = null;
		PrintStream stdout = System.out;
		if(display==false) {
			try {
				out = new PrintStream(new FileOutputStream(filePath+".semanticError"));
			} catch (FileNotFoundException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}		
			System.setOut(out);
		}
		/*
		SymbolTable sb1=new SymbolTable("global",null);
		SymbolTable sb2=new SymbolTable("Polynomial",null);
		SymbolTableRecord tb1=new SymbolTableRecord("Polynomial","class",null,sb2);
		SymbolTableRecord fc1=new SymbolTableRecord("function1","function","int k1",null);
		SymbolTableRecord fc2=new SymbolTableRecord("function1","function","int k2",null);
		SymbolTableRecord v1=new SymbolTableRecord("a1","variable",null,null);
		sb1.insert(tb1);
		sb2.insert(fc1);
		sb2.insert(fc2);
		sb2.insert(v1);
		SymbolTable sb3=new SymbolTable("linear",sb2);
		SymbolTableRecord tb2=new SymbolTableRecord("linear","class",null,sb3);
		sb1.insert(tb2);
		sb3.insert(new SymbolTableRecord(fc1));//overwrite function
		sb3.insert(new SymbolTableRecord(v1));//overwrite variable
		SymbolTable sb4=new SymbolTable(sb1);// clone
		SymbolTable sb5=new SymbolTable("a",sb3);
		sb2=new SymbolTable("Polynomial",sb5);//Circular class
		System.out.println(sb5.toString());
		*/
		//System.out.println(myast.getNodeStack().peek().toString());
		
		
		myast.getNodeStack().peek().accept(true,new SymTabCreationVisitor());
		myast.getNodeStack().peek().accept(true,new SymTabCreationVisitorPhase2());
		myast.getNodeStack().peek().accept(true,new TypeCheckingVisitor());
		//System.out.println(myast.getNodeStack().peek().getSymbolTable().toString());
		pw.println(myast.getNodeStack().peek().getSymbolTable().toString());
		pw.close();
		System.setOut(stdout);
	}
}
