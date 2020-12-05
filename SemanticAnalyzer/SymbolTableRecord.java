package MoonCompiler.SemanticAnalyzer;

public class SymbolTableRecord {
	private String name;
	private String kind;
	private String type;
	private String scope; //to check where the entry is orginally from ,
	private int size=0;//assign 4
	private int offset=0;//assign 4
	private String tag;
	private SymbolTable link;
	
	public SymbolTableRecord(String name, String kind,String type,SymbolTable link) {
		this.name=name;
		this.kind=kind;
		this.type=type;
		this.link=link;		
		this.scope=null; //will be set when insert
	}
	
	public SymbolTableRecord(SymbolTableRecord clone) { //checked
		this.name=clone.getName();
		this.kind=clone.getKind();
		this.type=clone.getType();
		this.scope=clone.getScope();
		this.size=clone.getSize();
		if (clone.getLink()!=null)
			this.link=new SymbolTable(clone.getLink()); // clone a new linked table
	}
	
	public String getName() {
		return name;
	}
	
	public String getKind() {
		return kind;
	}
	
	public String getType() {
		return type;
	}
	public int getSize() {
		return size;
	}
	public int getOffset() {
		return offset;
	}
	public String getScope() {
		return scope;
	}
	public String getTag() {
		return tag;
	}
	public SymbolTable getLink() {
		return link;
	}
	public void setType(String type) {
		this.type=type;
	}

	public void setLink(SymbolTable link) {
		this.link=link;
	}
	public void setScope(String scope) {
		this.scope=scope;
	}
	public void setSize(int size) {
		this.size=size;
	}
	public void setOffset(int offset) {
		this.offset=offset;
	}
	public void setTag(String tag) {
		this.tag=tag;
	}
	public String toString() {
		if(this.link!=null)
			return  String.format("%-20s","k:"+this.getKind())+String.format("%-20s","|"+ this.getName())+String.format("%-50s","|"+"t:"+ this.getType())+String.format("%-20s","|"+"Size:"+ this.getSize())+String.format("%-20s","|"+"Offset:"+ this.getOffset())+String.format("%-20s","|"+"Tag:"+ this.getTag())+String.format("%-20s","|Link:"+this.getLink().getName());
		else {
			return String.format("%-20s","k:"+this.getKind())+String.format("%-20s", "|"+ this.getName())+String.format("%-50s","|"+"t:"+ this.getType())+String.format("%-20s","|"+"Size:"+ this.getSize())+String.format("%-20s","|"+"Offset:"+ this.getOffset())+String.format("%-20s","|"+"Tag:"+ this.getTag())+String.format("%-20s","|Link:null");
		}
	}
	
}
