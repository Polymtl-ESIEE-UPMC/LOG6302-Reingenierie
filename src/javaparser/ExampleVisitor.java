package javaparser;

import java.util.ArrayList;
import java.util.List;

public class ExampleVisitor extends AbstractVisitor {
	// private String filename;
	public DotHandler dot_handler_instance = DotHandler.getInstance();

	public ExampleVisitor(String filename) {
		// this.filename = filename;
	}

	public Object visit(CompilationUnit node, Object data) {
		propagate(node, data);
		return data;
	}

	public Object visit(ClassDeclaration node, Object data) {
		String class_name = getImage(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetFirstToken());

		for (int i = 1; i < node.jjtGetChild(0).jjtGetNumChildren() - 1; i++) {
			if (node.jjtGetChild(0).jjtGetChild(i) instanceof Type) {
				DotHandler.getInstance().writeln(class_name + " -> " + getImage(
						((SimpleNode) node.jjtGetChild(0).jjtGetChild(i).jjtGetChild(0).jjtGetChild(0)).jjtGetFirstToken()));
			} else if (node.jjtGetChild(0).jjtGetChild(i) instanceof TypeList) {
				for (int j = 1; j < node.jjtGetChild(0).jjtGetChild(i).jjtGetNumChildren(); j++) {
					DotHandler.getInstance().writeln(class_name + " -> " + getImage(
							((SimpleNode) node.jjtGetChild(0).jjtGetChild(i).jjtGetChild(j).jjtGetChild(0)).jjtGetFirstToken()));
				}
			}
		}
		DotHandler.getInstance().newLine();

		data = new Data();
		node.jjtGetChild(0).jjtGetChild(node.jjtGetChild(0).jjtGetNumChildren() - 1).jjtAccept(this, data);

		DotHandler.getInstance().begin(class_name + " [");
		DotHandler.getInstance().write("label = \"{" + class_name + "|");
		for (int i = 0; i < ((Data) data).field.size(); i++) {
			DotHandler.getInstance()
					.write("+ " + ((Data) data).field.get(i).name + " : " + ((Data) data).field.get(i).type + "\\l");
		}
		DotHandler.getInstance().write("|");
		for (int i = 0; i < ((Data) data).method.size(); i++) {
			DotHandler.getInstance()
					.write("+ " + ((Data) data).method.get(i).name + "() : " + ((Data) data).method.get(i).type + "\\l");
		}
		DotHandler.getInstance().writeln("}\"");
		DotHandler.getInstance().end("]");

		return data;
	}

	public Object visit(MethodOrFieldDecl node, Object data) {
		String type;
		if (node.jjtGetChild(0) instanceof BasicType) {
			type = getImage(((SimpleNode) node.jjtGetChild(0)).jjtGetFirstToken());
		} else {
			type = getImage(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetFirstToken());
		}
		if (node.jjtGetChild(2).jjtGetChild(0) instanceof MethodDeclaratorRest) {
			((Data) data).method.add(new Data(getImage(((SimpleNode) node.jjtGetChild(1)).jjtGetFirstToken()), type));
		} else {
			((Data) data).field.add(new Data(getImage(((SimpleNode) node.jjtGetChild(1)).jjtGetFirstToken()), type));
		}
		propagate(node, data);
		return data;
	}

	private String getImage(Token token) {
		if (token.image.equals("Node"))
			return "AnotherNodeBecauseDotFileHasNodeTokenInSyntax";
		else if (token.image.equals("Inner$Class"))
			return "InnerClass";
		else
			return token.image;
	}

	class Data {
		public String name = null;
		public String type = null;

		public List<Data> field = null;
		public List<Data> method = null;

		public Data(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public Data() {
			this.field = new ArrayList<Data>();
			this.method = new ArrayList<Data>();
		}
	}

}
