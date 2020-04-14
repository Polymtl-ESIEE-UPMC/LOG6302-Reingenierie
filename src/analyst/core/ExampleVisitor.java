package analyst.core;

import analyst.helper.ArrayStack;
import javaparser.*;

public class ExampleVisitor extends AbstractVisitor {
	// private final String file_name;

	public ExampleVisitor(final String file_name) {
		// this.file_name = file_name;
	}

	public Object visit(final ClassDeclaration node, final Object __raw__) {
		Zeus.singleton.declareClass();
		propagate(node, new Data(null, Semantic.ClassMetadata, null));
		Zeus.singleton.disconnectClassDatabase();
		return __raw__;
	}

	public Object visit(final NormalClassDeclaration node, final Object __raw__) {
		return declareClassMetadata(node, __raw__, "class");
	}

	public Object visit(final EnumDeclaration node, final Object __raw__) {
		return declareClassMetadata(node, __raw__, "enum");
	}

	private Object declareClassMetadata(final SimpleNode node, final Object __raw__, final String type) {

		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			Zeus.singleton.connectClassDatabase().type = type;
			Zeus.singleton.connectClassDatabase().name = getReturnValue(node.jjtGetChild(0).jjtAccept(this, __raw__));
			propagate(node, __raw__);
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final ClassBody node, final Object __raw__) {
		return declareClassBody(node, __raw__);
	}

	public Object visit(final EnumBody node, final Object __raw__) {
		return declareClassBody(node, __raw__);
	}

	private Object declareClassBody(final SimpleNode node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			propagate(node, new Data(Semantic.ClassBody, Semantic.ClassBody, null));
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final MethodOrFieldDecl node, final Object __raw__) {
		return declareClassMember(node, __raw__, "");
	}

	public Object visit(final VoidMethodDecl node, final Object __raw__) {
		return declareClassMember(node, __raw__, "void");
	}

	private Object declareClassMember(final SimpleNode node, final Object __raw__, final String type) {

		if (matchContext(__raw__, Semantic.ClassBody)) {
			if (type.equals("void"))
				Zeus.singleton.connectClassDatabase().declare("void", null);
			propagate(node, new Data(null, Semantic.ClassBody.Member, null));
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final FieldDeclaratorsRest node, final Object __raw__) {
		return __raw__;
	}

	public Object visit(final MethodDeclaratorRest node, final Object __raw__) {
		return declareLastMemberAsMethod(node, __raw__);
	}

	public Object visit(final VoidMethodDeclaratorRest node, final Object __raw__) {
		return declareLastMemberAsMethod(node, __raw__);
	}

	private Object declareLastMemberAsMethod(final SimpleNode node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.ClassBody.Member)) {
			Zeus.singleton.connectClassDatabase().declareMethod();
			propagate(node, new Data(__raw__, Semantic.MethodMetadata, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().exit();
		}

		else {
			propagate(node, new Data(null, null, null));
		}
		return __raw__;
	}

	public Object visit(final MethodBody node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.MethodMetadata)) {
			propagate(node, new Data(Semantic.MethodBody, Semantic.MethodBody, null));
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	/* SECTION Flow Control */
	public Object visit(final IfStatement node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			propagate(node, new Data(__raw__, Semantic.IfStatement, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().exit();
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final ElseStatement node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.IfStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().loadCursor().addFlow("label", "False");
			propagate(node, new Data(__raw__, null, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end();
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final NoElseStatement node, final Object __raw__) {
		if (matchLexical(__raw__, Semantic.IfStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().loadCursor().addFlow("label", "False").end();
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final WhileStatement node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			propagate(node, new Data(__raw__, Semantic.WhileStatement, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().exit();
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final DoStatement node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("do", "");
			propagate(node, new Data(__raw__, Semantic.DoStatement, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().exit();
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final ForStatement node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			propagate(node, new Data(__raw__, Semantic.ForStatement, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().exit();
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final ForControl node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.ForStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("for", "").saveCursor().addFlow("label",
					"True");
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final SwitchStatement node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			propagate(node, new Data(__raw__, Semantic.SwitchStatement, null));
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().exit();
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final SwitchLabel node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.SwitchStatement)) {

			if (node.jjtGetNumChildren() == 0) {
				Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("case", "default");
				((Data) __raw__).value.push("default");
			}

		}

		propagate(node, new Data(null, Semantic.SwitchStatement.CaseStatement, null));
		return __raw__;
	}

	public Object visit(final ReturnStatement node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("action", "return").end("return");
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final BreakStatement node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("action", "break").end("break");
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final ContinueStatement node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("action", "continue").loop();
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}
	/* ************ SECTION END *************** */

	public Object visit(final BlockStatements node, final Object __raw__) {
		String case_label = ((Data) __raw__).value.pop();
		case_label = case_label != null ? case_label : "";

		if (matchLexical(__raw__, Semantic.SwitchStatement)) {
			if (!case_label.equals("default")) {
				Zeus.singleton.connectClassDatabase().connectMethodDatabase().saveCursor().addFlow("label", "True");
			}
		}

		propagate(node, new Data(__raw__, null, null));

		if (matchLexical(__raw__, Semantic.SwitchStatement)) {
			if (!case_label.equals("default")) {
				Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().loadCursor().addFlow("label", "False");
			}
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end().exit();
		}

		return __raw__;
	}

	public Object visit(final Statement node, final Object __raw__) {

		propagate(node, new Data(__raw__, null, null));

		if (matchLexical(__raw__, Semantic.IfStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().end();
		}

		else if (matchLexical(__raw__, Semantic.WhileStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().loop().loadCursor().addFlow("label", "False");
		}

		else if (matchLexical(__raw__, Semantic.ForStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().loop().loadCursor().addFlow("label", "False");
		}

		return __raw__;
	}

	/* SECTION Local Variable Declaration */

	public Object visit(final VariableDeclarators node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("action", "Variable Declaration");
			propagate(node, new Data(__raw__, Semantic.VariableDeclaration, null));
		} else {
			propagate(node, new Data(null, null, null));
		}
		return __raw__;
	}

	public Object visit(final VariableInitializer node, final Object __raw__) {
		if (matchLexical(__raw__, Semantic.VariableDeclaration)) {
			propagate(node, new Data(Semantic.VariableDeclaration.Expression, Semantic.VariableDeclaration.Expression, null));
		}
		return __raw__;
	}
	/* ************ SECTION END *************** */

	/* SECTION Expression */
	public Object visit(final StatementExpression node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("action", "Assign Statement");
			propagate(node, new Data(null, Semantic.AssignStatement, null));
		} else {
			propagate(node, new Data(null, null, null));
		}
		return __raw__;
	}

	public Object visit(final Expression node, final Object __raw__) {
		String expression = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			expression += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}
		return expression;
	}

	public Object visit(final ExpressionRest node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.AssignStatement)) {
			propagate(node, new Data(Semantic.AssignStatement.Expression, Semantic.AssignStatement.Expression, null));
		} else {
			propagate(node, __raw__);
		}

		return __raw__;
	}

	public Object visit(final Expression1 node, final Object __raw__) {
		String expression = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			expression += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}
		return expression;
	}

	public Object visit(final Expression1Rest node, final Object __raw__) {
		String expression = " ? ";
		expression += getReturnValue(node.jjtGetChild(0).jjtAccept(this, __raw__));
		expression += " : ";
		expression += getReturnValue(node.jjtGetChild(1).jjtAccept(this, __raw__));
		return expression;
	}

	public Object visit(final Expression2 node, final Object __raw__) {
		/*
		 * left expression of assign statement or just normal expression or right
		 * expression of local declaration is here
		 */
		String expression = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			expression += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}

		if (matchLexical(__raw__, Semantic.IfStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("if", expression).saveCursor()
					.addFlow("label", "True");
		}

		else if (matchLexical(__raw__, Semantic.WhileStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("while", expression).saveCursor()
					.addFlow("label", "True");
		}

		else if (matchLexical(__raw__, Semantic.DoStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("condition", "while " + expression)
					.saveCursor().addFlow("label", "True").loop().loadCursor().addFlow("label", "False");
		}

		else if (matchLexical(__raw__, Semantic.SwitchStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("switch", expression);
		}

		else if (matchLexical(__raw__, Semantic.SwitchStatement.CaseStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("case", expression);
		}

		else if (matchLexical(__raw__, Semantic.AssignStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().genVar(expression);
		}

		else if (matchLexical(__raw__, Semantic.AssignStatement.Expression)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().genExpression(expression);
		}

		else if (matchLexical(__raw__, Semantic.VariableDeclaration.Expression)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().genExpression(expression);
		}

		return expression;
	}

	public Object visit(final Expression2Rest node, final Object __raw__) {
		String expression = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			expression += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}
		return expression;
	}

	public Object visit(final InfixOp node, final Object __raw__) {
		return node.jjtGetFirstToken().image;
	}

	public Object visit(final Expression3 node, final Object __raw__) {
		String expression = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			expression += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}
		return expression;
	}

	public Object visit(final PrefixOp node, final Object __raw__) {
		return node.jjtGetFirstToken().image;
	}

	public Object visit(final Primary node, final Object __raw__) {
		// construct identifier variable here
		String variable = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			variable += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}

		if (matchContext(__raw__, Semantic.VariableDeclaration.Expression)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().use(variable);
		}

		else if (matchContext(__raw__, Semantic.AssignStatement.Expression)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().use(variable);
		}

		return variable;
	}

	public Object visit(final Literal node, final Object __raw__) {
		String literal = ((SimpleNode) node.jjtGetChild(0)).jjtGetFirstToken().image;
		literal = literal.replace("\"", "\\\"");
		return literal;
	}

	public Object visit(final IdentifierSuffix node, final Object __raw__) {
		if (matchLexical(__raw__, Semantic.AssignStatement)) {
			String expression = "";
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				expression += getReturnValue(
						node.jjtGetChild(i).jjtAccept(this, new Data(Semantic.AssignStatement.Expression, null, null)));
			}
			return expression;
		} else {
			propagate(node, __raw__);
			return __raw__;
		}
	}

	public Object visit(final Arguments node, final Object __raw__) {
		String arguments = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			arguments += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__)) + " ";
		}
		return " ( " + arguments + ")";
	}

	public Object visit(final PostfixOp node, final Object __raw__) {
		return node.jjtGetFirstToken().image;
	}

	/* ************ SECTION END *************** */

	/* SECTION Type */
	public Object visit(final TypeList node, final Object __raw__) {

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			String reference_type = getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
			if (matchLexical(__raw__, Semantic.ClassMetadata)) {
				Zeus.singleton.connectClassDatabase().implments.add(reference_type);
			}
		}

		return __raw__;
	}

	public Object visit(final Type node, final Object __raw__) {

		String type = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			type += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}

		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			Zeus.singleton.connectClassDatabase().extnds = type;
		}

		else if (matchLexical(__raw__, Semantic.ClassBody.Member)) {
			Zeus.singleton.connectClassDatabase().declare(type, null);
		}

		return __raw__;
	}

	public Object visit(final BasicType node, final Object __raw__) {
		return node.jjtGetFirstToken().image;
	}

	public Object visit(final ReferenceType node, final Object __raw__) {
		String reference_type = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			reference_type += getReturnValue(node.jjtGetChild(i).jjtAccept(this, __raw__));
		}
		return reference_type;
	}

	/* ************ SECTION END *************** */

	public Object visit(final Identifier node, final Object __raw__) {

		final java.util.function.Function<Token, String> getImage = (token) -> {
			if (token.image.equals("Node"))
				return "AnotherNodeBecauseDotFileHasNodeTokenInSyntax";
			else if (token.image.equals("Inner$Class"))
				return "InnerClass";
			else
				return token.image;
		};

		final String identifier_name = getImage.apply(node.jjtGetFirstToken());

		if (matchLexical(__raw__, Semantic.ClassBody.Member)) {
			Zeus.singleton.connectClassDatabase().declare(null, identifier_name);
		}

		if (matchLexical(__raw__, Semantic.VariableDeclaration)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().genVar(identifier_name);
		}

		return identifier_name;
	}

	private final SemanticData Semantic = new SemanticData();

	private class SemanticPath {

		protected final String __path__;

		private SemanticPath(final String path) {
			this.__path__ = path;
		}

		private SemanticPath(final String current_path, final String path) {
			this.__path__ = current_path + path;
		}
	}

	private class SemanticData {

		private final SemanticPath ClassMetadata = new SemanticPath("ClassMetadata");

		private final ClassBody ClassBody = new ClassBody();

		private class ClassBody extends SemanticPath {

			private ClassBody() {
				super("ClassBody");
			}

			private final SemanticPath Member = new SemanticPath("Member");
		}

		private final MethodMetadata MethodMetadata = new MethodMetadata();

		private class MethodMetadata extends SemanticPath {
			private MethodMetadata() {
				super("MethodMetadata");
			}

			private final Parameters Parameters = new Parameters(super.__path__);

			private class Parameters extends SemanticPath {
				private Parameters(final String current_path) {
					super(current_path, "Parameters");
				}

				private final SemanticPath Type = new SemanticPath(super.__path__, "Type");
			}
		}

		private final SemanticPath MethodBody = new SemanticPath("MethodBody");
		private final SemanticPath IfStatement = new SemanticPath("IfStatement");
		private final SemanticPath WhileStatement = new SemanticPath("WhileStatement");
		private final SemanticPath DoStatement = new SemanticPath("DoStatement");
		private final SemanticPath ForStatement = new SemanticPath("ForStatement");
		private final SwitchStatement SwitchStatement = new SwitchStatement();

		private class SwitchStatement extends SemanticPath {
			private SwitchStatement() {
				super("SwitchStatement");
			}

			private final SemanticPath CaseStatement = new SemanticPath(super.__path__, "CaseStatement");
		}

		private final VariableDeclaration VariableDeclaration = new VariableDeclaration();

		private class VariableDeclaration extends SemanticPath {
			private VariableDeclaration() {
				super("VariableDeclaration");
			}

			private final SemanticPath Expression = new SemanticPath(super.__path__, "Expression");
		}

		private final AssignStatement AssignStatement = new AssignStatement();

		private class AssignStatement extends SemanticPath {
			private AssignStatement() {
				super("AssignStatement");
			}

			private final SemanticPath Expression = new SemanticPath(super.__path__, "Expression");
		}

	}

	private class Data {

		private final SemanticPath context;
		private final SemanticPath lexical;
		private final ArrayStack<String> value;

		private Data(final SemanticPath context, final SemanticPath lexical, final ArrayStack<String> value) {
			this.context = context;
			this.lexical = lexical;
			this.value = value != null ? value : new ArrayStack<String>();
		}

		private Data(final Object data_with_context, final SemanticPath lexical, final ArrayStack<String> value) {
			this.context = data_with_context != null ? ((Data) data_with_context).context : null;
			this.lexical = lexical;
			this.value = value != null ? value : new ArrayStack<String>();
		}

		private Data(final SemanticPath context, final Object data_with_lexical, final ArrayStack<String> value) {
			this.context = context;
			this.lexical = data_with_lexical != null ? ((Data) data_with_lexical).lexical : null;
			this.value = value != null ? value : new ArrayStack<String>();
		}
	}

	private boolean matchContext(final Object o, final SemanticPath context) {
		if (o != null && ((Data) o).context != null && ((Data) o).context.__path__.equals(context.__path__))
			return true;
		return false;
	}

	private boolean matchLexical(final Object o, final SemanticPath type) {
		if (o != null && ((Data) o).lexical != null && ((Data) o).lexical.__path__.equals(type.__path__))
			return true;
		return false;
	}

	private String getReturnValue(Object o) {
		if (o instanceof String)
			return (String) o;
		return "";
	}

}
