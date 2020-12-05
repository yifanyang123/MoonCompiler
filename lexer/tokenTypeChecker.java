package MoonCompiler.lexer;

public class tokenTypeChecker {
	private String keyWord[] = {"==","<>","<",">","<=",">=","+","-","*","/","=",
			"(",")","{","}","[","]",";",",",".",":","::",
			"//","/*","*/",
			"for","expr","if","then","else","while","class","integer","float","do","end","public","private","or","and","not","read","write","return","main","inherits","local","void"};
	public boolean isNum(String s) {
		if(s.matches("\\d*|\\d*\\.\\d*(e(\\+|\\-)?\\d*)?")) {
			return true;
		}
		else
			return false;
	}
	//2.1.1
	public boolean isInteger(String s) {
		if(s.matches("[1-9]\\d*|0")) {
			return true;
		}
		else
			return false;
	}
	//2.1.2
	public boolean isFloat(String s) {
		if(s.matches("(0|[1-9]\\d*)\\.(\\d*[1-9]|0)(e(\\+|\\-)?([1-9]\\d*|0))?")) {
			return true;
		}
		else
			return false;
	}
	public boolean isIdentification(String s) {
		if(s.matches("\\w*")&&!isInteger(s)&&!s.matches("_")) {
			return true;
		}
		else
			return false;
	}
	public  boolean isID(String s) {
		if(s.matches("[a-zA-Z]\\w*"))
			return true;
		else
			return false;
	};
	public boolean isKeyWord(String s){
        for(int i = 0;i < keyWord.length;i++)
        {
            if(keyWord[i].equals(s))
                return true;
        }
        return false;
	}
	public String keyWordType(String s) {
		switch (s) {
		 case "/*":
			 return "openbcmt";
		 case "*/":
			 return "closebcmt";
		 case "//":
			 return "inlinecmt";	 
		 case "==":
			 return "eq";
		 case "=":
			 return "=";
		 case "<>":
			 return "neq";
		 case "for":
			 return "for";
		 case "expr":
			 return "expr";
		 case "<=":
			 return "leq";
		 case ">=":
			 return "geq";
		 case "<":
			 return "lt";
		 case ">":
			 return "gt";
		 case "+":
			 return "+";
		 case "-":
			 return "-";
		 case "*":
			 return "*";
		 case "/":
			 return "/";
		 case "(":
			 return "(";
		 case ")":
			 return ")";
		 case "{":
			 return "{";
		 case "}":
			 return "}";
		 case "[":
			 return "[";
		 case "]":
			 return "]";
		 case ";":
			 return ";";
		 case ",":
			 return ",";
		 case ".":
			 return ".";
		 case "::":
			 return "sr";
		 case ":":
			 return ":";
		 case "integer":
			 return "integer";
		 case "if":
			 return "if";
		 case "do":
			 return "do";
		 case "end":
			 return "end";
		 case "then":
			 return "then";
		 case "else":
			 return "else";
		 case "public":
			 return "public";
		 case "while":
			 return "while";
		 case "private":
			 return "private";
		 case "class":
			 return "class";
		 case "or":
			 return "or";
		 case "and":
			 return "and";
		 case "read":
			 return "read";
		 case "float":
			 return "float";
		 case "not":
			 return "not";
		 case "inherits":
			 return "inherits";
		 case "write":
			 return "write";
		 case "local":
			 return "local";
		 case "return":
			 return "return";
		 case "main":
			 return "main";
		 case "void":
			 return "void";
		 default:
			 return "false";
		}
	}
}

