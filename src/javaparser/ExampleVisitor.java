package javaparser;

public class ExampleVisitor extends AbstractVisitor {
	// private String filename;
	public DotHandler dot_handler_instance = DotHandler.getInstance();

	public ExampleVisitor(String filename) {
		// this.filename = filename;
		dot_handler_instance.init();
	}

	// TODO: Read theory about UML
	public Object visit(CompilationUnit node, Object data) {
		propagate(node, data);
		return data;
	}

	public Object visit(ClassDeclaration node, Object data) {
		propagate(node, data);
		return data;
	}

	public Object visit(MethodOrFieldDecl node, Object data) {
		propagate(node, data);
		return data;
	}

	public Object visit(IfStatement node, Object data) {
		propagate(node, data);
		return data;
	}

	public Object visit(LocalVariableDeclarationStatement node, Object data) {
		propagate(node, data);
		return data;
	}

}
