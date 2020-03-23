package javaparser;

import javaparser.DotHandler.DotNode;

public class ExampleVisitor extends AbstractVisitor {
	// private String filename;
	public DotHandler dot_handler_instance = DotHandler.getInstance();

	public ExampleVisitor(final String filename) {
		// this.filename = filename;
	}

	public Object visit(final CompilationUnit node, final Object data) {
		propagate(node, data);
		return data;
	}

	public Object visit(final ClassDeclaration node, final Object data) {
		final String class_name = getImage(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetFirstToken());

		for (int i = 1; i < node.jjtGetChild(0).jjtGetNumChildren() - 1; i++) {
			if (node.jjtGetChild(0).jjtGetChild(i) instanceof Type
					|| node.jjtGetChild(0).jjtGetChild(i) instanceof TypeList) {
				for (int j = 1; j < node.jjtGetChild(0).jjtGetChild(i).jjtGetNumChildren(); j++) {
					final String parent_name = getImage(
							((SimpleNode) node.jjtGetChild(0).jjtGetChild(i).jjtGetChild(j).jjtGetChild(0)).jjtGetFirstToken());
					DotHandler.getInstance().setRelation().from(class_name).to(parent_name);
				}
			}
		}

		propagate(node, class_name);
		return data;
	}

	public Object visit(final MethodOrFieldDecl node, final Object data) {
		String type;
		if (node.jjtGetChild(0) instanceof BasicType) {
			type = getImage(((SimpleNode) node.jjtGetChild(0)).jjtGetFirstToken());
		} else {
			type = getImage(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetFirstToken());
		}
		if (node.jjtGetChild(2).jjtGetChild(0) instanceof MethodDeclaratorRest) {
			DotHandler.getInstance().add().method(getImage(((SimpleNode) node.jjtGetChild(1)).jjtGetFirstToken()), type)
					.to((String) data);
		} else {
			DotHandler.getInstance().add().field(getImage(((SimpleNode) node.jjtGetChild(1)).jjtGetFirstToken()), type)
					.to((String) data);
		}
		propagate(node, data);
		return data;
	}

	public Object visit(final VoidMethodDeclaratorRest node, final Object data) {
		DotHandler.getInstance().add()
				.method(getImage(((SimpleNode) node.jjtGetParent().jjtGetChild(0)).jjtGetFirstToken()), "void");
		return data;
	}

	private String getImage(final Token token) {
		if (token.image.equals("Node"))
			return "AnotherNodeBecauseDotFileHasNodeTokenInSyntax";
		else if (token.image.equals("Inner$Class"))
			return "InnerClass";
		else
			return token.image;
	}

}
