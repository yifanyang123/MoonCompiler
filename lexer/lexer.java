package MoonCompiler.lexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;




public class lexer {
	private PrintWriter pw=null;
	private BufferedReader br=null;
	private String s="temp";
	private Scanner kb = new Scanner(System.in);
	private int lineCounter=0;
	private int commentCounter=0;
	private String myfile;
	private token tempToken;
	private tokenTypeChecker myTemp=new tokenTypeChecker();
	private String comment="";
	private ArrayList<Object> spaceDivide;
	private ArrayList<token> tokenList;
	private ArrayList<token> errorList; 
	
	public lexer(String myfile){
		this.spaceDivide=new ArrayList<Object>();
		this.tokenList=new ArrayList<token>();
		this.errorList=new ArrayList<token>();
		this.myfile=myfile;
	}
	public void generate() {
		try 
		{
			br=new BufferedReader(new FileReader(myfile+".src"));
		} catch (FileNotFoundException e) 
		{
			System.out.println("Error: Cannot find the file");
			System.out.println("Program will terminate.");
		}
		catch (IOException e) 
		{
			System.out.println("Error: An error has occurred while reading from the  file. ");
			System.out.println("Program will terminate.");
			System.exit(0);		
		}
		while(s!=null) {
			try {
				s=br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (s!=null) {
				lineCounter++;
				s=addSpace(s,"/*");
				s=addSpace(s,"\\*/");
				s=addSpace(s,"//");
				s=addSpace(s,"<>");
				s=addSpace(s,"<=");
				s=addSpace(s,">=");
				s=addSpace(s,"<");
				s=addSpace(s,">");
				s=addSpace(s,"==");	
				s=addSpace(s,"=");
				s=addSpace(s,"+");
				s=addSpace(s,"-");
				s=addSpace(s,"*");
				s=addSpace(s,"/");
				s=addSpace(s,"(");
				s=addSpace(s,")");
				s=addSpace(s,"{");
				s=addSpace(s,"}");
				s=addSpace(s,"[");
				s=addSpace(s,"]");
				s=addSpace(s,";");
				s=addSpace(s,",");
				s=addSpace(s,".");
				s=addSpace(s,":");
				s=addSpace(s,"::");
				//System.out.println(s);   //Display lexer
				String[] temp=s.split("\\s+"); //split space
				for(int i=0;i<temp.length;i++) {
					if(!temp[i].isEmpty()) {
						spaceDivide.add(temp[i]);
						spaceDivide.add(lineCounter);
					}
				}
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		

		System.out.println(myTemp.isKeyWord("then"));
		for (int i=0;i<spaceDivide.size();i=i+2) {
			String current=spaceDivide.get(i).toString();
			int y = Integer.parseInt(spaceDivide.get(i+1).toString());
			if(myTemp.isKeyWord(current)) {
				if(myTemp.keyWordType(current)!="openbcmt"&&myTemp.keyWordType(current)!="closebcmt"&&myTemp.keyWordType(current)!="inlinecmt") {
					tempToken=new token(myTemp.keyWordType(current),current,y);
					tokenList.add(tempToken);
				}
				else if(myTemp.keyWordType(current)=="inlinecmt") {
					while(y==Integer.parseInt(spaceDivide.get(i+1).toString())) { //y is currentLine
						comment+=spaceDivide.get(i).toString()+" ";
						if(i+2<spaceDivide.size()&&(y==Integer.parseInt(spaceDivide.get(i+3).toString())))  //not swallow last token after that line
							i=i+2; 
						else
							break;
					}
					comment=comment.substring(0,comment.length()-1);
					tempToken=new token("inlinecmt",comment,y);
					tokenList.add(tempToken);
					comment="";
				}
				else if (myTemp.keyWordType(current)=="openbcmt") {
					do {
						comment+=spaceDivide.get(i).toString()+" ";
						if(myTemp.keyWordType(spaceDivide.get(i).toString())=="openbcmt")
							commentCounter+=1;
						if(myTemp.keyWordType(spaceDivide.get(i).toString())=="closebcmt")
							commentCounter-=1;
						if(i+2<spaceDivide.size())
							i=i+2;
						else
							break;
					}
				    while (commentCounter>0);
					i=i-2;
					comment=comment.substring(0,comment.length()-1);
					if (commentCounter==0) {
						tempToken=new token("blockcmt",comment,y);
						tokenList.add(tempToken);
					}
					if (commentCounter!=0) {
						tempToken=new token("incompleteComment",comment,y);
						errorList.add(tempToken);  //2.2.1
					}
					comment="";
				}
				else if (myTemp.keyWordType(current)=="closebcmt") {
					tempToken=new token("closebcmt",comment,y);
					tokenList.add(tempToken);
				}
			}
			else if(myTemp.isNum(current)) {
				if(myTemp.isInteger(current)) {
				    tempToken=new token("intNum",current,y);
					tokenList.add(tempToken);
				}
				else if(myTemp.isFloat(current)) {
				    tempToken=new token("floatNum",current,y);
					tokenList.add(tempToken);
				}
				else {
					tempToken=new token("invalidnum",current,y);
					errorList.add(tempToken);//2.2.1
				}
			}
			else if(myTemp.isIdentification(current)) {
				if(myTemp.isID(current)) {
				    tempToken=new token("id",current,y);
					tokenList.add(tempToken);
				}
				else {
					tempToken=new token("invalidid",current,y);
					errorList.add(tempToken);//2.2.1
				}
			}
			else {
				tempToken=new token("invalidChar",current,y);
				errorList.add(tempToken);//2.2.1
			}
		}
		try
		{
			pw = new PrintWriter(new FileOutputStream(myfile+".outlextokens", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}
		
		for (int i=0;i<tokenList.size();i++) {
			pw.println(tokenList.get(i));//2.3.1
		}
		pw.close();
		try
		{
			pw = new PrintWriter(new FileOutputStream(myfile+".outlexerrors", false));	// Notice that second parameter   
		}
		catch(FileNotFoundException e) 			// Since we are attempting to write to the file
		{							   			// exception is automatically thrown if file cannot be created. 
			System.out.println("Could not open/create the file to write to. "
					+ " Please check for problems such as directory permission"
					+ " or no available memory.");
			System.out.print("Program will terminate.");
			System.exit(0);			   		   
		}
		
		for (int i=0;i<errorList.size();i++) {
			pw.println(errorList.get(i));//2.2.2
		}
		pw.close();
		
	}

	public ArrayList<token> getToken(){
		return tokenList;
	}
	
	public String addSpace(String inputString,String divider) {
		inputString=" ".concat(inputString).concat(" ");
		String[] spaceString=null;
		if (divider==".") {   
			spaceString=inputString.split("((?<=\\.)|(?=\\.))");//split without remove
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("."))
	            	if(!Character.isDigit((spaceString[i-1].charAt(spaceString[i-1].length()-1)))||!Character.isDigit((spaceString[i+1].charAt(0)))) {
	            		spaceString[i]=" . ";
	            	}
	         }
		}
		else if (divider==("/")) {
			spaceString=inputString.split("((?<=\\/)|(?=\\/))");//split without remove
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("/"))
	            	if((!((spaceString[i-1].charAt(spaceString[i-1].length()-1)=='/')))&&!((spaceString[i+1].charAt(0))=='/')&&!((spaceString[i+1].charAt(0))=='*')&&!((spaceString[i-1].charAt(spaceString[i-1].length()-1))=='*')) {
	            		spaceString[i]=" / ";	            	
	            	}         
	        }
		}
		else if (divider==("=")) {
			spaceString=inputString.split("((?<=\\=)|(?=\\=))");//split without remove
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("="))
	            	if((!((spaceString[i-1].charAt(spaceString[i-1].length()-1)=='=')))&&!((spaceString[i+1].charAt(0))=='=')&&!((spaceString[i-1].charAt(spaceString[i-1].length()-1)=='<'))&&!((spaceString[i-1].charAt(spaceString[i-1].length()-1)=='>'))) {
	            		spaceString[i]=" = ";	            	
	            	}         
	        }
		}
		else if (divider==("*")) {
			spaceString=inputString.split("((?<=\\*)|(?=\\*))");//split without remove
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("*"))
	            	if(!((spaceString[i+1].charAt(0))=='/')&&!((spaceString[i-1].charAt(spaceString[i-1].length()-1))=='/')) {
	            		spaceString[i]=" * ";
	            	}
	         }         
		}
		else if (divider==(":")) {
			spaceString=inputString.split("((?<=:)|(?=:))");//split without remove
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals(":"))
	            	if(!(spaceString[i-1].charAt(spaceString[i-1].length()-1)==':')&&!(spaceString[i+1].charAt(0)==':')) {
	            		spaceString[i]=" : ";
	            	}
	         }         
		}
		
		else if (divider==("<")) {
			spaceString=inputString.split("((?<=<)|(?=<))");//split without remove
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("<"))
	            	if(!(spaceString[i+1].charAt(0)=='=')&&!(spaceString[i+1].charAt(0)=='>')) {
	            		spaceString[i]=" < ";
	            	}
	         }         
		}
		else if (divider==(">")) {
			spaceString=inputString.split("((?<=>)|(?=>))");//split without remove 、、就是接下来的Stiring[] 里还有他
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals(">"))
	            	if(!(spaceString[i+1].charAt(0)=='=')&&!(spaceString[i-1].charAt(spaceString[i-1].length()-1)=='<')) {
	            		spaceString[i]=" > ";
	            	}
	         }         
		}
		else if (divider==("+")) {
			spaceString=inputString.split("((?<=\\+)|(?=\\+))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("+"))
	            		spaceString[i]=" + ";
	         }
		}
		else if (divider==("(")) {
			spaceString=inputString.split("((?<=\\()|(?=\\())");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("("))
	            		spaceString[i]=" ( ";
	         }
		}
		else if (divider==(")")) {
			spaceString=inputString.split("((?<=\\))|(?=\\)))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals(")"))
	            		spaceString[i]=" ) ";
	         }
		}
		else if (divider==("[")) {
			spaceString=inputString.split("((?<=\\[)|(?=\\[))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("["))
	            		spaceString[i]=" [ ";
	         }
		}
		else if (divider==("]")) {
			spaceString=inputString.split("((?<=\\])|(?=\\]))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("]"))
	            		spaceString[i]=" ] ";
	         }
		}
		else if (divider==("{")) {
			spaceString=inputString.split("((?<=\\{)|(?=\\{))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("{"))    
	            		spaceString[i]=" { ";
	         }
		}
		else if (divider==("}")) {
			spaceString=inputString.split("((?<=\\})|(?=\\}))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals("}"))
	            		spaceString[i]=" } ";
	         }
		}
		else {
			spaceString=inputString.split("((?<="+divider+")|(?="+divider+"))");
	        for (int i = 0; i < spaceString.length; i++) {
	            if(spaceString[i].equals(divider))
	            		spaceString[i]=" "+divider+" ";
	         }
		}
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < spaceString.length; i++) {
            stringBuilder.append(spaceString[i]);
        }
        inputString= stringBuilder.toString();
		return inputString;
	}
}
