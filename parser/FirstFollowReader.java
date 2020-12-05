package MoonCompiler.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class FirstFollowReader {
	private PrintWriter pw=null;
	private BufferedReader br=null;
	private String s="temp";
	private String k="temp";
	private String g="temp";
	private Hashtable First ;
	private Hashtable Follow;
	private HashSet nonTerminal;
	private HashSet terminal;
	private String myfile;
	private ArrayList<String> rules;
	public FirstFollowReader(String myfile){
		First= new Hashtable();
		Follow=new Hashtable();
		nonTerminal=new HashSet();
		terminal=new HashSet();
		rules=new ArrayList<String>();
		this.myfile=myfile;
	}
	
	public void generate() 
	{
		try 
		{
			br=new BufferedReader(new FileReader(myfile+".grm.first"));
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
				HashSet elementSet=new HashSet();
				String[] temp=s.split("= ",2);
				temp[0]=temp[0].replaceAll("FIRST|\\(|\\)", "");
				if(!temp[0].isEmpty())
				nonTerminal.add(temp[0]);
				if(temp.length==2) {
					temp[1]=temp[1].substring(1, temp[1].length() - 1);
					temp[1]=temp[1].replaceAll("\\, ", " ");
					String[] element=temp[1].split(" ");
					for(int i=0;i<element.length;i++) {
						if (element[i].compareTo("EPSILON")!=0&&!element[i].isEmpty()) {
							element[i]=element[i].substring(1, element[i].length()-1);
							elementSet.add(element[i]);
						}
						else
							elementSet.add(element[i]);
						if (element[i]!="EPSILON")
						terminal.add(element[i]);
					}
					First.put(temp[0], elementSet);
				}
			}
		}
		try {
			br.close();
		} catch (IOException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		//------------------------------------generate follow
		try 
		{
			br=new BufferedReader(new FileReader(myfile+".grm.follow"));
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
		while(k!=null) {
			try {
				k=br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (k!=null) {
				HashSet elementSet1=new HashSet();
				String[] temp=k.split("= ",2);
				temp[0]=temp[0].replaceAll("FOLLOW|\\(|\\)", "");
				if(!temp[0].isEmpty())
				nonTerminal.add(temp[0]);
				if(temp.length==2) {
					temp[1]=temp[1].substring(1, temp[1].length() - 1);
					temp[1]=temp[1].replaceAll("\\, ", " ");
					String[] element=temp[1].split(" ");
					for(int i=0;i<element.length;i++) {
							element[i]=element[i].replaceAll("\\'", "");
							elementSet1.add(element[i]);
							if (element[i]!="EPSILON"&&!element[i].isEmpty())
							terminal.add(element[i]);
					}
					Follow.put(temp[0], elementSet1);
				}
			}
		}
		try {
			br.close();
		} catch (IOException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		//--------------------------------------------------------read rule
		try 
		{
			br=new BufferedReader(new FileReader(myfile+".grm"));
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
		while(g!=null) {
			try {
				g=br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (g!=null) {
				if(!g.isEmpty())
				rules.add(g);
				}
			}
		try {
			br.close();
		} catch (IOException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
	}
	

	public Hashtable getFirst() {
		return First;
	}
	public Hashtable getFollow() {
		return Follow;
	}
	public ArrayList<String> getRule() {
		return rules;
	}
	public HashSet getTerminal() {
		return terminal;
	}
	public HashSet getNonTerminal() {
		return nonTerminal;
	}
}
