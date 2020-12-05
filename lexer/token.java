package MoonCompiler.lexer;

public class token {
	
	String type;
	String data;
	int location;
	public token() {
		type="";
		data = "";
		
	}
	
	public token(String type, String data, int location) {
		this.type = type;
		this.data =data;
		this.location = location;
	}
	public int getLocation() {
		return location;
	}
	public String getType() {
		return type;
	}
	
	public String getData() {
		return data;
		
	}
	
	
	//gives token type and data associated to it
	public String toString() {
		return "["+type+","+data+","+location+"]";
	}
}
