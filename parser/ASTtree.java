package MoonCompiler.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import MoonCompiler.lexer.token;

public class ASTtree {
	private Stack<ASTnode> progBuffer; //classList, funcList,statblock
	private Stack<ASTnode> cfListbuffer; //several classDecl, funcDef
	private Stack<ASTnode> classDeclBuffer;//all info need for one classDecl;
	private Stack<ASTnode> inheritMemberBuffer;//inherit and memberdecl, for classDecl
	private Stack<ASTnode> varFunBuffer;//varDecl or fundecl for memberDecl, or funcdef
	private Stack<ASTnode> dimBuffer;//dimnum
	private Stack<ASTnode> paramBuffer;//all info need for one param,and several params
	private Stack<ASTnode> statBlockMemberBuffer;// all stat and vardecl for one statblock
	private Stack<ASTnode> statVarBuffer; //buffer to store info for one stat or varDecl
	private Stack<ASTnode> exprBuffer;// buffer for expr, var , fcall, factor,datamember,aparams,indexlist
	private Stack<ASTnode> relBuffer; // buffer the relexpr of while block
	private Stack<ASTnode> loopBuffer;// buffer for all statements in loop or if
	private Stack<ASTnode> statBlockBuffer; //buffer to store statblock for if and While
	private ASTnode addOp=null;
	private ASTnode multOp=null;
	private ASTnode sign=null;
	private int repeatCounter=0;
	private boolean statBlockBoolean=true;
	private ArrayList<Stack<ASTnode>> bufferList;
	ASTtree(){
		this.progBuffer=new Stack<ASTnode>();
		this.cfListbuffer=new Stack<ASTnode>();
		this.classDeclBuffer=new Stack<ASTnode>();
		this.inheritMemberBuffer=new Stack<ASTnode>();		
		this.varFunBuffer=new Stack<ASTnode>();
		this.dimBuffer=new Stack<ASTnode>();
		this.paramBuffer=new Stack<ASTnode>();
		this.statBlockMemberBuffer=new Stack<ASTnode>();
		this.statVarBuffer=new Stack<ASTnode>();
		this.exprBuffer=new Stack<ASTnode>();
		this.relBuffer=new Stack<ASTnode>();
		this.loopBuffer=new Stack<ASTnode>();
		this.statBlockBuffer=new Stack<ASTnode>();
		this.bufferList=new ArrayList<Stack<ASTnode>>();
		bufferList.add(progBuffer);  //0 for progBuffer
		bufferList.add(cfListbuffer);	//1
		bufferList.add(classDeclBuffer); //2
		bufferList.add(inheritMemberBuffer); //3
		bufferList.add(varFunBuffer); //4
		bufferList.add(dimBuffer); //5
		bufferList.add(paramBuffer); //6
		bufferList.add(statBlockMemberBuffer); //7
		bufferList.add(statVarBuffer); //8
		bufferList.add(exprBuffer); //9
		bufferList.add(relBuffer); //10
		bufferList.add(loopBuffer); //11
		bufferList.add(statBlockBuffer);//12
		
	}
	
	public void makeNode(String type,String value,int line,int purposeBuffer) {
		ASTnode temp=new ASTnode(type,value,line);
		bufferList.get(purposeBuffer).push(temp);
	}
	

	
	public void makeFamily(ASTnode operator,int childNum,int sourceBuffer,int purposeBuffer) { //fixed size makefamily,this cach is mudi
			if(childNum>1)
				for(int i=0;i<childNum-1;i++) {
					ASTnode temp=bufferList.get(sourceBuffer).pop();
					bufferList.get(sourceBuffer).peek().makeSiblings(temp);  //if there are 5 children, we should do this loop 4 times because we won't pop peek
				}
			ASTnode head=bufferList.get(sourceBuffer).pop(); //last node to pop, if child num=1, it will be only child
			ASTnode current=head.getRightSibling();
			head.setLeftmostSibling(head);//head will be left most children
			while(current!=null) {			//all sibling set leftmost
				current.setLeftmostSibling(head);
				current=current.getRightSibling();
			}
			operator.adoptChildren(head);
			bufferList.get(purposeBuffer).push(operator);				
	}
	
	public void clearCache(ASTnode operator,int sourceBuffer,int purposeBuffer) {		//anyway the purpose is nodestack,the caching is clear which buffer
		Stack<ASTnode> purpose =bufferList.get(purposeBuffer);
		Stack<ASTnode> source =bufferList.get(sourceBuffer);
		ASTnode head=new ASTnode("EPSILON","EPSILON",0);
		while(!source.isEmpty()) {
			ASTnode temp=source.pop();
			if(source.isEmpty()) {
				head=temp;// if pop this is empty. means it is last node
				break;
			}	
			source.peek().makeSiblings(temp);
		}
		ASTnode current=head.getRightSibling();
		head.setLeftmostSibling(head);//head will be left most children
		while(current!=null) {			//all sibling set leftmost
			current.setLeftmostSibling(head);
			current=current.getRightSibling();
		}
		operator.adoptChildren(head);
		purpose.push(operator);	
	}
	
	
	void doSemanticAction(int semanticAction, token token) {
        switch (semanticAction) {
        case 1:
            makeNode(token.getType(), token.getData(),token.getLocation(),2); //make node to classDeclBufferBuffer
            break;        
        case 2:
        	ASTnode classList=new ASTnode("classList","classList",token.getLocation());
        	clearCache(classList,1,0); //clear all decl send classList to progBuffer
        	break;
        case 3:
        	ASTnode funcDefList=new ASTnode("funcDefList","funcDefList",token.getLocation());
        	clearCache(funcDefList,1,0); //clear all funcDef send funcList to prog
        	break;
        case 4:// hai yao jia yige dongxi ,xianzaizhiyou classList, funcDefList
        	ASTnode prog=new ASTnode("prog","prog",token.getLocation());
        	clearCache(prog,0,0); //prog has all list ,make family
        	break;
        case 5://checked
        	ASTnode classDecl=new ASTnode("classDecl","classDecl",token.getLocation());
        	makeFamily(classDecl,3,2,1);//use cfList buffer to cache all class Decl, so purpose is 1,source is 2
        	break;
        case 6:
        	ASTnode inherList=new ASTnode("inherList","inherList",token.getLocation());
        	clearCache(inherList,3,2); //clear inheritMemberBuffer, output inheritlist to classBuffer
        	break;
        case 7:
        	makeNode(token.getType(), token.getData(),token.getLocation(),3); //inherit everything to inheritbuffer
            break;
        case 8:
        	ASTnode membList=new ASTnode("membList","membList",token.getLocation());
        	clearCache(membList,3,2); //clear inheritMemberBuffer, output memblist to classBuffer
        	break;
        case 9:
        	ASTnode varDecl=new ASTnode("varDecl","varDecl",token.getLocation());
        	clearCache(varDecl,4,3); //clear varDeclbuffer, output to memblist
        	break;
        case 10:
        	ASTnode funcDecl=new ASTnode("funcDecl","funcDecl",token.getLocation());
        	clearCache(funcDecl,4,3); //clear FuncDeclbuffer, output to memblist
        	break;
        case 11:
        	makeNode(token.getType(), token.getData(),token.getLocation(),4); //make node to varFunBuffer
            break;    
        case 12:
        	ASTnode dimList=new ASTnode("dimList","dimList",token.getLocation());
        	clearCache(dimList,5,4); //clear dimensionbuffer, output dimlist to varBuffer
        	break;
        case 13: 
            makeNode(token.getType(), token.getData(),token.getLocation(),5); //make dimmension num to dimbuffer //shared by fparam and varDecl
            break;   
        case 14: 
            makeNode(token.getType(), token.getData(),token.getLocation(),6); //make id type to paramBuffer
            break; 
        case 15:
        	ASTnode dimlist=new ASTnode("dimList","dimList",token.getLocation());
        	clearCache(dimlist,5,6); //clear dimensionbuffer, output dimlist to parambuffer
        	break;
        case 16:
        	ASTnode fparam=new ASTnode("fparam","fparam",token.getLocation());
        	makeFamily(fparam,3,6,6); //take type,id ,dimlist and restore to parambuffer
        	break;
        case 17:
        	ASTnode fparamList=new ASTnode("fparamList","fparamList",token.getLocation());
        	clearCache(fparamList,6,4); //send paramList to funcdecl
        	break;
        case 18://unchecked, need to be 5
        	ASTnode funcDef=new ASTnode("funcDef","funcDef",token.getLocation());
        	clearCache(funcDef,4,1);//use cfList buffer to cache all funcdef, so source is 4,purpose is 1
        	break;
        case 19:
        	ASTnode statBlock=new ASTnode("statBlock","statBlock",token.getLocation());
            if(bufferList.get(4).isEmpty()) //means it is main statblock
            	clearCache(statBlock,7,0);
            else
            	clearCache(statBlock,7,4);//clear statOrVardecl, send statBlock to varFuncBuffer,(part of funcDef)
            break;
        case 20:
        	makeNode(token.getType(), token.getData(),token.getLocation(),8); //basic info for varDecl/statement in statBlock //statVarBuffer
        	break;
        case 21:
        	ASTnode Dimlist=new ASTnode("dimList","dimList",token.getLocation());
        	clearCache(Dimlist,5,8); //clear dimensionbuffer, output dimlist to statVarBuffer
        	break;
        case 22:
        	ASTnode vardecl=new ASTnode("varDecl","varDecl",token.getLocation());
        	clearCache(vardecl,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	break;
        case 23: //
        	ASTnode ifStat=new ASTnode("ifStat","ifStat",token.getLocation());      	
        	ASTnode tempState1=statBlockBuffer.pop();
        	ASTnode tempState2=statBlockBuffer.pop();
        	statVarBuffer.push(relBuffer.pop());
        	statVarBuffer.push(tempState2);
        	statVarBuffer.push(tempState1);
        	clearCache(ifStat,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	break;
        case 24:
        	ASTnode readStat=new ASTnode("readStat","readStat",token.getLocation());
        	clearCache(readStat,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	break;
        case 25:
        	ASTnode writeStat=new ASTnode("writeStat","writeStat",token.getLocation());
        	clearCache(writeStat,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	break;
        case 26:
        	ASTnode returnStat=new ASTnode("returnStat","returnStat",token.getLocation());
        	clearCache(returnStat,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	break;
        case 27:
        	ASTnode whileStat=new ASTnode("whileStat","whileStat",token.getLocation());
        	ASTnode tempstate=statBlockBuffer.pop();
        	statVarBuffer.push(relBuffer.pop());
        	statVarBuffer.push(tempstate);
        	clearCache(whileStat,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	break;
        case 28:
        	//ASTnode functionCall=new ASTnode("functionCall","functionCall",token.getLocation());
        	//clearCache(functionCall,8,7); //clear varStatbuffer, output varDecl to statVarBuffer
        	statBlockMemberBuffer.push(statVarBuffer.pop());
        	break;
        case 29:
        	ASTnode assignStat=new ASTnode("assignStat","assignStat",token.getLocation());
        	clearCache(assignStat,9,7); //clear var and expr in exprBuffer, output varDecl to statVarBuffer
        	break;
        case 30: 
        	makeFamily(multOp,2,9,9);
        	break;
        case 31:
        	ASTnode relExpr=new ASTnode("relExpr","relExpr",token.getLocation());
        	makeFamily(relExpr,3,9,9); // expr,relop,expr
        	break;
        case 32:
        	makeNode(token.getType(), token.getData(),token.getLocation(),9); //send relop num id to  expr
        	break;
        case 33:
        	addOp=new ASTnode("addOp",token.getData(),token.getLocation()); //string //new change assignment3
        	break;
        case 34:
        	makeFamily(addOp,2,9,9);
        	break;
        case 35:
        	statVarBuffer.push(exprBuffer.pop());
        	break;
        //factor:
        //num we use 32
        //arithexpr we ignore because aritheexpr will generate a node itself
        //not factor , use 38 
        case 36: //temp make factor
        	makeNode("factor","factor",token.getLocation(),9);
        	break;
        case 37:
        	multOp=new ASTnode("multOp",token.getData(),token.getLocation()); //string   //new change not sure!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        	break;
        case 38:
        	ASTnode not=new ASTnode("not","not",token.getLocation());
        	makeFamily(not,1,9,9);
        	break;
        case 39:
        	sign=new ASTnode("sign",token.getData(),token.getLocation()); //new change
        	break;
        case 40:
        	makeFamily(sign,1,9,9);
        	break;
        case 41: //veeeeeeeeerrrrrrrry baaaad , for professor tool reason, can only repeat once or twice
        	ASTnode Var=new ASTnode("var","var",token.getLocation());
        	makeFamily(Var,2,9,9); //repeat once
        	break;
        case 42:  //use repeatCounter generated by 45 to create indexList
        	if(repeatCounter==0)
        		makeNode("indexList","indexList",token.getLocation(),9); //not sure whether make this node if no index
        	else if (repeatCounter>0){
            	ASTnode indexList=new ASTnode("indexList","indexList",token.getLocation());
            	makeFamily(indexList,repeatCounter,9,9);
        	}
        	repeatCounter=0;
        	break;
        case 43:
        	System.out.println(repeatCounter);  //aparams cannot generate repeatCounter
        	if(repeatCounter==0)
        		makeNode("aParams","aParams",token.getLocation(),9);
        	else if (repeatCounter>0){
        		ASTnode aParams=new ASTnode("aParams","aParams",token.getLocation());
        		makeFamily(aParams,repeatCounter,9,9);
        	}
        	repeatCounter=0;
        	break;
        case 44: //verrrrrrrrrrrrrrrrrrrrrry baaaaaaaaad!
        	ASTnode var=new ASTnode("var","var",token.getLocation());
        	makeFamily(var,1,9,9); //repeat once
        	break;
        case 45://after rept-indice and aparams, should generate expr expr expr ..id //make ast great again!!!!!!!!!!!!!!!!!
        	Stack<ASTnode> tempStack=new Stack<ASTnode>();
        	while(!exprBuffer.peek().getType().equals("endpoint")) { //pop and count until id
        		tempStack.push(exprBuffer.pop()); // id peek wont be pop
        		repeatCounter++;
        	}
        	if(exprBuffer.peek().getType().equals("endpoint"))
        		exprBuffer.pop();
        	while(!tempStack.isEmpty()) { //push back
        		exprBuffer.push(tempStack.pop());
        	}
        	break;
        case 46:
        	ASTnode dataMember=new ASTnode("dataMember","dataMember",token.getLocation());
        	makeFamily(dataMember,2,9,9); //id and rept-indice
        	break;
        case 47:
        	ASTnode fCall=new ASTnode("fCall","fCall",token.getLocation());
        	makeFamily(fCall,2,9,9); //id and aparams
        	break;
        case 48:
        	makeNode("endpoint","endpoint",token.getLocation(),7);
        	break;
        case 49:
        	relBuffer.push(exprBuffer.pop());
        	break;
        case 50:
        	Stack<ASTnode> reversedStack=new Stack<ASTnode>();
        	while(!statBlockMemberBuffer.peek().getType().equals("endpoint")) {
        		reversedStack.push(statBlockMemberBuffer.pop());//push every statement above endpoint to loopBuffer     
        	}
        	while(!reversedStack.isEmpty()) {
        		loopBuffer.push(reversedStack.pop());
        	}
        	statBlockMemberBuffer.pop();//pop endpoint
        	statBlock=new ASTnode("statBlock","statBlock",token.getLocation());
        	clearCache(statBlock,11,12);
        	break;
        case 51:
        	makeNode("endpoint","endpoint",token.getLocation(),9);
        	break;
        }   
	}
	public Stack<ASTnode> getNodeStack() {
		return progBuffer;
	}
	

	
}








