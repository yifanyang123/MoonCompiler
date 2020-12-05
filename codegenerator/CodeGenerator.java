package MoonCompiler.codegenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import MoonCompiler.parser.ASTtree;

public class CodeGenerator {
	private ASTtree myast;
	private String filePath;
	public CodeGenerator(ASTtree ast,String filePath) {
		myast=ast;
		this.filePath=filePath;
	}
	
	public void generate() {
		PrintWriter pw=null;
		try
		{
			 pw = new PrintWriter(new FileOutputStream(filePath+".m", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}
		
		myast.getNodeStack().peek().accept(true, new TagVisitor());
		myast.getNodeStack().peek().accept(true,new TableSizeVisitor());
		codeGenerationVisitor temp=new codeGenerationVisitor();
		myast.getNodeStack().peek().accept(false,temp);	
		//System.out.println(myast.getNodeStack().peek().getSymbolTable().toString());
		pw.println(temp.getResult());
		pw.close();
	}
	
	
	
}
