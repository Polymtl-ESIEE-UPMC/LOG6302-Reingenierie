package javaparser;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExampleVisitor extends AbstractVisitor
{
	static int ID = 0;
	private String filename;
	private String current_class;
	private String current_method;
	private FileOutputStream oFile;

	public ExampleVisitor(String filename) {
		this.filename = filename;

		try {
			File yourFile = new File("./result.csv");
			yourFile.createNewFile(); // if file already exists will do nothing 
			this.oFile = new FileOutputStream(yourFile, true); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class Metric {
		public int if_stmt = 0;
		public int vars = 0;
	}
	private HashMap<String, HashMap<String, Metric>> result = new HashMap<String, HashMap<String, Metric>>();

	public Object visit(CompilationUnit node, Object data){
		propagate(node, data);
		
		try { 
			for (String c : result.keySet()) {
    			for (String m : result.get(c).keySet()){
					oFile.write((ID + ";" + this.filename + ";" + c + ";" + m + ";" + result.get(c).get(m).if_stmt + ";" + result.get(c).get(m).vars + "\n").getBytes());
          ID++;
        }
			}
			oFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}

	public Object visit(ClassDeclaration node, Object data){
		current_class = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetFirstToken().image;
		propagate(node, data);
		current_class = "";
		return data;
	}

	public Object visit(MethodOrFieldDecl node, Object data){
		current_method = ((SimpleNode) node.jjtGetChild(1)).jjtGetFirstToken().image;
		HashMap<String, Metric> temp = new HashMap<String, Metric>();
		temp.put(current_method, new Metric());
		result.put(current_class, temp);
		propagate(node, data);
		current_method = "";
		return data;
	}

	public Object visit(IfStatement node, Object data){
		result.get(current_class).get(current_method).if_stmt++;
		propagate(node, data);
		return data;
	}

  public Object visit(LocalVariableDeclarationStatement node, Object data){
		result.get(current_class).get(current_method).vars++;
    propagate(node, data);
		return data;
	}

}
