package MoonCompiler.codegenerator;

import java.util.Stack;

import MoonCompiler.SemanticAnalyzer.SymbolTable;
import MoonCompiler.SemanticAnalyzer.SymbolTableRecord;
import MoonCompiler.SemanticAnalyzer.Visitor;
import MoonCompiler.parser.ASTnode;

public class codeGenerationVisitor extends Visitor {
    private Stack<String> registerPool;
    private String moonExecCode   = new String();              // moon instructions part
    private String moonDataCode   = new String();              // moon data part
    private String mooncodeindent = new String("          ");
    private String outputfilename = new String();
    private static int i;
    private static int ifCounter;
    private static int whileCounter;
    public codeGenerationVisitor() {
    	registerPool=new Stack<String>();
    	for (Integer i = 12; i>=1; i--)
    		registerPool.push("r" + i.toString());
    	//r0 0
    	//r1-r11 common
    	//r12 offset
    	//r13 result
    	//r14 frame
    	//r15 jumpback
    }
    
    public String getResult() {
    	return moonExecCode+"\n"+moonDataCode;
    }
    public void visit(ASTnode node) {
    	switch(node.getType()) {
    		case "prog":{
    			node.getleftmostChild().accept(true, this);
    			node.getChildren(1).accept(false, this);
    			node.getChildren(2).accept(false, this);
    			break;
    		}
    		case "funcDefList":{
    			ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
				break;
    		}
    		case "funcDef" : {
				if(node.getSymbolTable()!=null&&node.getSymbolTable().getTableRecords().size()!=0) {  //main
					moonDataCode += mooncodeindent + "% funcDef var res\n";
					for(int i=0;i<node.getSymbolTable().getTableRecords().size();i++) {
						if(node.getSymbolTable().getTableRecords().get(i).getKind().equals("variable"))
						moonDataCode += String.format("%-20s" , node.getSymbolTable().getTableRecords().get(i).getTag()) + "res "+node.getSymbolTable().getTableRecords().get(i).getSize()+"\n";
					}
				}
    			// propagate accepting the same visitor to all the children
    			// this effectively achieves Depth-First AST Traversal
    			moonExecCode += mooncodeindent + "% processing function \n";
    			//create the tag to jump onto 
    			moonExecCode += String.format("%-25s",node.getTag());  //function statement block jump : fn
    			// copy the jumping-back address value in a tagged cell named "fname" appended with "link"
    			moonDataCode += String.format("%-25s", node.getTag() + "link") + "res 4\n";     //fnres res 4
    			moonExecCode += mooncodeindent + "sw " + node.getTag() + "link(r0),r15\n";  
    			// tagged cell for return value
    			// here assumed to be integer (limitation)
    			moonDataCode += String.format("%-25s", node.getTag() + "return") + "res 4\n";  
    			ASTnode currentfparam=node.getChildren(1).getleftmostChild();
    			int fcounter=0;
				while(currentfparam!=null&&!currentfparam.getValue().equals("EPSILON")) {
					moonDataCode += String.format("%-25s", node.getTag() + "p"+fcounter) + "res 4\n";     //fnres res 4
					currentfparam.setTag(node.getTag() + "p"+fcounter);
					for(int i=0;i<searchScope(node).getTableRecords().size();i++) {
						if(searchScope(node).getTableRecords().get(i).getName().equals(currentfparam.getChildren(1).getValue()))
							searchScope(node).getTableRecords().get(i).setTag(node.getTag() + "p"+fcounter);
					}
					fcounter++;
					currentfparam=currentfparam.getRightSibling();
				}
    			//generate the code for the function body
    			ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
    			// copy back the jumping-back address into r15
    			moonExecCode += mooncodeindent + "lw r15," + node.getTag() + "link(r0)\n";
    			// jump back to the calling function
    			moonExecCode += mooncodeindent + "jr r15\n";	
    			break;
    		}
    		case "fCall" : {
    			ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
    			String localregister1 = registerPool.pop();
    			node.setTag("tempfCall"+i);
    			i++;
    			// pass parameters directly in the function's local variables
    			// it is assumed that the parameters are the first n entries in the 
    			// function's symbol table 
    			// here we assume that the parameters are the size of a word, 
    			// which is not true for arrays and objects. 
    			// In those cases, a loop copying the values e.g. byte-by-byte is necessary

    			SymbolTableRecord tableentryofcalledfunction =searchFunction(node);
    			String fcallTag=tableentryofcalledfunction.getTag();
    			int indexofparam = 0;
    			moonExecCode += mooncodeindent + "% processing: function call to "  + fcallTag + " \n";
    			ASTnode currentChild=node.getChildren(1).getleftmostChild();
				while(currentChild!=null&&!currentChild.getValue().equals("EPSILON")) {
    				moonExecCode += mooncodeindent + "lw " + localregister1 + "," + searchTag(currentChild) + "(r0)\n";
    			    String nameofparam = fcallTag+"p"+indexofparam;
    				moonExecCode += mooncodeindent + "sw " + nameofparam + "(r0)," + localregister1 + "\n";
    				indexofparam++;
					currentChild=currentChild.getRightSibling();
				}

    			// jump to the called function's code
    			// here the name of the label is assumed to be the function's name
    			// a unique label generator is necessary in the general case (limitation)
    			moonExecCode += mooncodeindent + "jl r15," + fcallTag + "\n";
    			// copy the return value in a tagged memory cell
    			moonDataCode += mooncodeindent + "% space for function call expression factor\n";		
    			moonDataCode += String.format("%-20s", node.getTag()) + "res 4\n";
    			moonExecCode += mooncodeindent + "lw " + localregister1 + "," + fcallTag + "return(r0)\n";
    			moonExecCode += mooncodeindent + "sw " + node.getTag() + "(r0)," + localregister1 + "\n";
    			this.registerPool.push(localregister1);	
    			break;
    		}
    		case "var":{
    			if (node.getleftmostChild().getValue().equals("fCall"))
    				node.getleftmostChild().accept(false, this);
    			break;
    		}
    		

    		
    		case "returnStat" :{
    			// propagate accepting the same visitor to all the children
    			// this effectively achieves Depth-First AST Traversal
    			String localregister1 = this.registerPool.pop();
    			ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
				ASTnode currentParent=node;
				String fcallTag="";
				while(currentParent!=null&&!currentParent.getValue().equals("EPSILON")) {
					if(currentParent.getType().equals("funcDef")) //free and memberDef function
						 fcallTag=currentParent.getTag();
					currentParent=currentParent.getParent();
				}
    			// copy the result of the return value in a cell tagged with the name "function name" + "return", e.g. "f1return"
    			// get the function name from the symbol table
    			
    			moonExecCode += mooncodeindent + "% processing: return\n";
    			moonExecCode += mooncodeindent + "lw " + localregister1 + "," + searchTag(node.getleftmostChild()) + "(r0)\n";
    			moonExecCode += mooncodeindent + "sw "   + fcallTag + "return(r0)," + localregister1 + "\n";
    			this.registerPool.push(localregister1);	
    			
    			break;
    		}
    		
    		case "statBlock":{
    			if(node.getParent().getValue().equals("prog")) {
    				moonExecCode += mooncodeindent + "entry\n";
    				// make the stack frame pointer (address stored in r14) point 
    				// to the top address allocated to the moon processor 
    				moonExecCode += mooncodeindent + "addi r14,r0,topaddr\n";   //put topaddr to r14
    				if(node.getSymbolTable()!=null&&node.getSymbolTable().getTableRecords().size()!=0) {  //main
    					moonDataCode += mooncodeindent + "% main var res\n";
    					for(int i=0;i<node.getSymbolTable().getTableRecords().size();i++) {
    						moonDataCode += String.format("%-20s" , node.getSymbolTable().getTableRecords().get(i).getTag()) + "res "+node.getSymbolTable().getTableRecords().get(i).getSize()+"\n";
    					}
    				}
    				ASTnode current=node.getleftmostChild();
    				while(current!=null&&!current.getValue().equals("EPSILON")) {
    					current.accept(false,this);
    					current=current.getRightSibling();
    				}
    				// generate moon program's end point
    				moonDataCode += mooncodeindent + "% buffer space used for console output\n";
    				// buffer used by the lib.m subroutines
    				moonDataCode += String.format("%-20s" , "buf") + "res 20\n";
    				// halting point of the entire program
    				moonExecCode += mooncodeindent + "hlt\n";
    			}
    			else {
    				ASTnode current=node.getleftmostChild();
    				while(current!=null&&!current.getValue().equals("EPSILON")) {
    					current.accept(false,this);
    					current=current.getRightSibling();
    				}
    			}
    		break;
    		}
    		case "intNum":{
    			String localRegister = this.registerPool.pop();
    			ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
				node.setTag("tempNum"+i);
				i++;
				moonDataCode += String.format("%-20s",node.getTag()) + " res 4\n";
				moonExecCode += mooncodeindent + "addi " + localRegister + ",r0," + node.getValue() + "\n"; 
				moonExecCode += mooncodeindent + "sw " + node.getTag() + "(r0)," + localRegister + "\n";
				this.registerPool.push(localRegister);
				break;
    		}
    		case "indexList":{
    			int relativeOffset=0;
    			if(node.getleftmostChild()==null)
    				System.out.println(relativeOffset);
    			break;
    		}
    		case "assignStat":{ 
				ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
    			String localRegister = this.registerPool.pop();
    			String lhsTag=searchTag(node.getleftmostChild());
    			String rhsTag=searchTag(node.getChildren(1));
        		moonExecCode += mooncodeindent + "% processing: "  + lhsTag + " := " + rhsTag + "\n";
        		moonExecCode += mooncodeindent + "lw " + localRegister + "," + rhsTag + "(r0)\n";
        		moonExecCode += mooncodeindent + "sw " + lhsTag + "(r0)," + localRegister + "\n";
    			this.registerPool.push(localRegister);		
    			break;
    		}
    		
    		
    		case "writeStat":{
    			ASTnode current=node.getleftmostChild();
    			while(current!=null&&!current.getValue().equals("EPSILON")) {
    				current.accept(false,this);
    				current=current.getRightSibling();
    			}
    			String localRegister      = this.registerPool.pop();
    			
        		moonExecCode += mooncodeindent + "% processing: put("  + searchTag(node.getleftmostChild()) + ")\n";
            	moonExecCode += mooncodeindent + "lw " + localRegister + "," + searchTag(node.getleftmostChild()) + "(r0)\n";
    			moonExecCode += mooncodeindent + "sw -8(r14)," + localRegister + "\n";
    			moonExecCode += mooncodeindent + "addi " + localRegister + ",r0, buf\n";
    			moonExecCode += mooncodeindent + "sw -12(r14)," + localRegister + "\n";
    			moonExecCode += mooncodeindent + "jl r15, intstr\n";	
    			moonExecCode += mooncodeindent + "sw -8(r14),r13\n";
    			moonExecCode += mooncodeindent + "jl r15, putstr\n";
    			this.registerPool.push(localRegister);			
    			break;
    		}
    		case "readStat":{
    			ASTnode current=node.getleftmostChild();
    			while(current!=null&&!current.getValue().equals("EPSILON")) {
    				current.accept(false,this);
    				current=current.getRightSibling();
    			}
    			String localRegister      = this.registerPool.pop();
    			String localRegister2      = this.registerPool.pop();
    			String lhsTag=searchTag(node.getleftmostChild());
        		moonExecCode += mooncodeindent + "% processing: read("  + searchTag(node.getleftmostChild()) + ")\n";
        		moonExecCode += mooncodeindent + "getc " + localRegister + "\n";
        		moonExecCode += mooncodeindent + "subi " + localRegister2 + "," + localRegister +",48"+ "\n";    //cause ASCII 48 =   CHAR 0
        		moonExecCode += mooncodeindent + "sw "   + searchTag(node.getleftmostChild()) + "(r0), " + localRegister2 + "\n";
        		/*
        		moonExecCode += mooncodeindent + "jl r15, getstr\n"; //getStr to r13
        		moonExecCode += mooncodeindent + "sw -8(r14),r13\n"; //store result to strint param
        		moonExecCode += mooncodeindent + "jl r15, strint\n";	
            	moonExecCode += mooncodeindent + "lw " + localRegister + "," + "-8(r14)\n";
    			//moonExecCode += mooncodeindent + "sw -8(r14)," + localRegister + "\n";
    			//moonExecCode += mooncodeindent + "addi " + localRegister + ",r0, buf\n";
    			*/
    			
    			
    			//deallocate local register
    			this.registerPool.push(localRegister);		
    			this.registerPool.push(localRegister2);	
    			break;
    		}
    		case "addOp":{
				ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
				 String operator="";
				 switch (node.getValue()) {
                 case "+":
                     operator = "add ";
                     break;
                 case "-":
                     operator = "sub ";
                     break;
                 case "or":
                     operator = "or ";
                     break;
				 }
	    		String localRegister      = this.registerPool.pop();
	    		String leftChildRegister  = this.registerPool.pop();
	    		String rightChildRegister = this.registerPool.pop();
	    			// generate code
	    		moonExecCode += mooncodeindent + "% processing : " + searchTag(node) + " := " + searchTag(node.getleftmostChild()) + " + " + searchTag(node.getChildren(1)) + "\n";
	    		moonExecCode += mooncodeindent + "lw "  + leftChildRegister +  "," + searchTag(node.getleftmostChild()) + "(r0)\n";
	    		moonExecCode += mooncodeindent + "lw "  + rightChildRegister + "," + searchTag(node.getChildren(1)) + "(r0)\n";
	    		moonExecCode += mooncodeindent + operator + localRegister +      "," + leftChildRegister + "," + rightChildRegister + "\n"; 
	    		moonExecCode += mooncodeindent + "sw " + searchTag(node) + "(r0)," + localRegister + "\n";
	    			// deallocate the registers for the two children, and the current node
	    		this.registerPool.push(leftChildRegister);
	    		this.registerPool.push(rightChildRegister);
	    		this.registerPool.push(localRegister);
	    		break;
				}
    		case "multOp":{
				ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
				 String operator="";
				 switch (node.getValue()) {
                 case "*":
                     operator = "mul ";
                     break;
                 case "/":
                     operator = "div ";
                     break;
                 case "and":
                     operator = "and ";
                     break;
				 }
	    		String localRegister      = this.registerPool.pop();
	    		String leftChildRegister  = this.registerPool.pop();
	    		String rightChildRegister = this.registerPool.pop();
	    			// generate code
	    		moonExecCode += mooncodeindent + "% processing: " + searchTag(node) + " := " + searchTag(node.getleftmostChild()) + " + " + searchTag(node.getChildren(1)) + "\n";
	    		moonExecCode += mooncodeindent + "lw "  + leftChildRegister +  "," + searchTag(node.getleftmostChild()) + "(r0)\n";
	    		moonExecCode += mooncodeindent + "lw "  + rightChildRegister + "," + searchTag(node.getChildren(1)) + "(r0)\n";
	    		moonExecCode += mooncodeindent + operator + localRegister +      "," + leftChildRegister + "," + rightChildRegister + "\n"; 
	    		moonExecCode += mooncodeindent + "sw " + searchTag(node) + "(r0)," + localRegister + "\n";
	    			// deallocate the registers for the two children, and the current node
	    		this.registerPool.push(leftChildRegister);
	    		this.registerPool.push(rightChildRegister);
	    		this.registerPool.push(localRegister);
	    		break;
    		}
    		case "relExpr":{
				ASTnode current=node.getleftmostChild();
				while(current!=null&&!current.getValue().equals("EPSILON")) {
					current.accept(false,this);
					current=current.getRightSibling();
				}
				 String operator="";
				 switch (node.getChildren(1).getValue()) {
                 case "<":
                     operator = "clt ";
                     break;
                 case "<=":
                     operator = "cle ";
                     break;
                 case ">":
                     operator = "cgt ";
                     break;
                 case ">=":
                     operator = "cge ";
                     break;
                 case "==":
                     operator = "ceq ";
                     break;
                 case "<>":
                     operator = "cne ";
                     break;
				}
	    		String localRegister      = this.registerPool.pop();
	    		String leftChildRegister  = this.registerPool.pop();
	    		String rightChildRegister = this.registerPool.pop();
	    			// generate code
	    		moonExecCode += mooncodeindent + "% processing: " + searchTag(node) + " := " + searchTag(node.getleftmostChild()) + " + " + searchTag(node.getChildren(2)) + "\n";
	    		moonExecCode += mooncodeindent + "lw "  + leftChildRegister +  "," + searchTag(node.getleftmostChild()) + "(r0)\n";
	    		moonExecCode += mooncodeindent + "lw "  + rightChildRegister + "," + searchTag(node.getChildren(2)) + "(r0)\n";
	    		moonExecCode += mooncodeindent + operator + localRegister +      "," + leftChildRegister + "," + rightChildRegister + "\n"; 
	    		moonExecCode += mooncodeindent + "sw " + searchTag(node) + "(r0)," + localRegister + "\n";
	    			// deallocate the registers for the two children, and the current node
	    		this.registerPool.push(leftChildRegister);
	    		this.registerPool.push(rightChildRegister);
	    		this.registerPool.push(localRegister);
	    		break;
    		}
    		case "ifStat":{     
    			ifCounter++;
                node.getleftmostChild().accept(false,this);//	{code for expr yields tn as a result}	
                String localRegister1 = registerPool.pop();
                moonExecCode += mooncodeindent + "%processing ifStat\n";
                moonExecCode += mooncodeindent + "lw " + localRegister1 + ", " + searchTag(node.getleftmostChild()) + "(r0)\n";
                moonExecCode += mooncodeindent + "bz " + localRegister1 + ", else" + ifCounter + "\n";  
                node.getChildren(1).accept(false,this);//{code for statblock1}
                moonExecCode +=  mooncodeindent + "j endif" + ifCounter + "\n";
                moonExecCode += "else" + ifCounter;
                node.getChildren(2).accept(false,this);//{code for statblock2}
                moonExecCode += "endif" + ifCounter+ "\n";
                registerPool.push(localRegister1);
                ifCounter--;
    			break;
    		}
    		case "whileStat":{
    			whileCounter++;
    			moonExecCode += mooncodeindent + "%processing whileStat\n";
    			moonExecCode += "gowhile" + whileCounter;    			
                node.getleftmostChild().accept(false,this);//	{{code for a<b yields tn as a result}}	
                String localRegister1 = registerPool.pop();
                moonExecCode += mooncodeindent + "lw " + localRegister1 + ", " + searchTag(node.getleftmostChild()) + "(r0)\n";
                moonExecCode += mooncodeindent + "bz " + localRegister1 + ", endwhile" + whileCounter + "\n";  
                node.getChildren(1).accept(false,this);
                moonExecCode +=  mooncodeindent + "j gowhile" + whileCounter + "\n";
                moonExecCode += "endwhile" + whileCounter;
                whileCounter--;
    			break;
    		}
            case "not": {
            	node.getleftmostChild().accept(true,this);
                String localRegister1 = registerPool.pop();
                String localRegister2 = registerPool.pop();
                moonExecCode += mooncodeindent + "lw "  + localRegister1 + ", " + searchTag(node.getChildren(0)) + "(r0)\n";
                moonExecCode += mooncodeindent + "not " + localRegister2 + ", " + localRegister1 + "\n";
                moonExecCode += mooncodeindent + "sw "  + searchTag(node) + "(r0), " + localRegister2 + "\n";
                registerPool.push(localRegister2);
                registerPool.push(localRegister1);
                break;
            }
            case "sign":{
            	node.getleftmostChild().accept(false,this);
                String localRegister = registerPool.pop();
                String localRegister2= registerPool.pop();
				String operator="";
				switch (node.getValue()) {
                case "+":
                    operator = "add ";
                    break;
                case "-":
                    operator = "sub ";
                    break;
				 }
	    			// generate code
	    		moonExecCode += mooncodeindent + "% processing sign: "+searchTag(node.getleftmostChild())+" \n";
	    		moonExecCode += mooncodeindent + "lw "  + localRegister +  "," + searchTag(node.getleftmostChild()) + "(r0)\n";
	    		moonExecCode += mooncodeindent + operator + localRegister2 + ",r0," + localRegister + "\n"; 
	    		moonExecCode += mooncodeindent + "sw " + searchTag(node) + "(r0)," + localRegister2 + "\n";
	    			// deallocate the registers for the two children, and the current node
                break;
            }
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    }

	public int findOffSet(ASTnode node) {
		int offset=1;
		switch(node.getType()) {
		case "var":{
			if(node.getleftmostChild().getType().equals("dataMember")){
				if(node.getChildren(1)==null) { //only datamember
					ASTnode indexList=node.getleftmostChild().getChildren(1);
					ASTnode current=indexList.getleftmostChild();
					if(current==null)
						moonExecCode += mooncodeindent + "sw "  + "r12" +  "," + "r0\n"; //r12 =r0 =0
					else {  //array
						/*
						moonExecCode += mooncodeindent + "lw "  + "r12" +  "," + searchTag(current) + "(r0)\n"; //load first as basic
						while(current!=null&&!current.getValue().equals("EPSILON")) {
							if(current.getRightSibling()!=null) {
								String register1=registerPool.pop();
								moonExecCode += mooncodeindent + "addi " + "r12" + ",r0," + node.getValue() + "\n"; 
								moonExecCode += mooncodeindent + "lw "  + register1 +  "," + searchTag(current) + "(r0)\n";
								moonExecCode += mooncodeindent + "mul " + "r12" +      "," + "r12" + "," + register1 + "\n"; 
								offset=offset*basicSize*Integer.parseInt(current.getValue()); 
							}
								
							else
								offset=offset+basicSize*Integer.parseInt(current.getValue());//lastIndex
							current=current.getRightSibling();
						}
						*/
					}

				}
				else if(node.getChildren(1).getType().equals("dataMember")) {	//class.data		
				}
				else if(node.getChildren(1).getType().equals("fCall")) {	//class.funcall		
				}						
			}
			else if (node.getleftmostChild().getType().equals("fCall")) {  //free func
				
			}
			break;
			}
		}
		return offset;
	}
	public SymbolTableRecord searchFunction(ASTnode node) {
		for(int i=0;i<getGlobal(node).getTableRecords().size();i++) {
			if(getGlobal(node).getTableRecords().get(i).getName().equals(node.getleftmostChild().getValue())&&getGlobal(node).getTableRecords().get(i).getKind().equals("function")) {
				String[] funcArr=getGlobal(node).getTableRecords().get(i).getType().split(":");
				//System.out.println(funcArr[1].equals(" "));
				//function with no parameter,still have :
				//function            |test             |void:               

				String[] paraArr=funcArr[1].split(", ");
				if(!paraArr[0].equals(" ")) {
					paraArr[0]=paraArr[0].substring(1, paraArr[0].length());
				}//remove first space
				
				if(paraArr[0].equals(" ")&&node.getChildren(1).getleftmostChild()==null) {//parameter is empty  
					return getGlobal(node).getTableRecords().get(i);
				}
				else 
				{
					int childrenCounter=0;
					ASTnode current=node.getChildren(1).getChildren(0);
					while(current!=null&&!current.getValue().equals("EPSILON")) {
						childrenCounter++;
						current=current.getRightSibling();
					}
					if(childrenCounter==paraArr.length) 
					{ //second check, has same number of params
						boolean reachEnd=true;
						for (int j=0;j<paraArr.length;j++) {
							//System.out.println(paraArr[j]);
							//System.out.println(node.getChildren(1).getChildren(j).getBindType().replaceAll("[0-9]",""));
							if(!paraArr[j].equals(node.getChildren(1).getChildren(j).getBindType().replaceAll("[0-9]",""))) { //one parameter doesn't match
								reachEnd=false;//why replace all num,   parameter is integer[], arr type  is integer[7]
								break;
							}
						}			
						if(reachEnd==true) { //have same name ,have same paramLength, have same params 
							return getGlobal(node).getTableRecords().get(i);
						}							
					}
				
				}
			}
		};
			return null;
	}
	public SymbolTableRecord searchRecord(String name,SymbolTable symbolTable) {
		for(int i=0;i<symbolTable.getTableRecords().size();i++) {
			if(name.equals(symbolTable.getTableRecords().get(i).getName()))
				return symbolTable.getTableRecords().get(i);
		}
		return null;
	}
	public String searchTag(ASTnode node) {
		switch(node.getType()) {
		case "var":{
			if(node.getleftmostChild().getType().equals("dataMember")){
				if(node.getChildren(1)==null) { //only datamember
					SymbolTableRecord lhs=searchScope(node).searchNoFuncName(node.getleftmostChild().getleftmostChild().getValue());
					return lhs.getTag();
				}
				else if(node.getChildren(1).getType().equals("dataMember")) {			
				}
				else if(node.getChildren(1).getType().equals("fCall")) {

				}					
			}
			else if (node.getleftmostChild().getType().equals("fCall")) {
				return node.getChildren(0).getTag();
			}
			break;
		}
		case "addOp":{
			return node.getTag();
		}
		case "multOp":{
			return node.getTag();
		}
		case "relExpr" :{
			return node.getTag();
		}
		case "not" :{
			return node.getTag();
		}
		case "sign" :{
			return node.getTag();
		}
		case "intNum" :{
			return node.getTag();
		}
		
		}
		return null;
		
	}
}
