package analyst.core;

import java.util.ArrayList;

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

	public Object visit(final TypeParameters node, final Object __raw__) {
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final EnumDeclaration node, final Object __raw__) {
		return declareClassMetadata(node, __raw__, "enum");
	}

	private Object declareClassMetadata(final SimpleNode node, final Object __raw__, final String type) {

		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			Zeus.singleton.connectClassDatabase().type = type;
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
		propagate(node, new Data(null, null, null));
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

	public Object visit(final FormalParameters node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.MethodMetadata)) {
			propagate(node, new Data(__raw__, Semantic.MethodMetadata.Parameters, null));
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
				((Data) __raw__).data = "default";
			}

		}

		propagate(node, new Data(null, Semantic.CaseStatement, null));
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

	public Object visit(final Expression node, final Object __raw__) {
		node.jjtGetChild(0).jjtAccept(this, __raw__);
		// right expression of assign statement
		// propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final Expression1 node, final Object __raw__) {
		node.jjtGetChild(0).jjtAccept(this, __raw__);
		// right expression of question mark
		// propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final Expression2 node, final Object __raw__) {
		// left expression of assign statement or just normal expression is here
		ArrayList<String> exprssion = new ArrayList<String>();
		propagate(node, new Data(Semantic.Expression, null, exprssion));
		String expression = "";
		for (String s : exprssion) {
			expression += s;
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

		else if (matchLexical(__raw__, Semantic.CaseStatement)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().begin("case", expression);
		}

		return __raw__;
	}

	// public Object visit(final Expression3 node, final Object __raw__) {
	// // TODO construct identifier variable here
	// propagate(node, new Data(null, null, null));
	// return __raw__;
	// }

	@SuppressWarnings("unchecked")
	public Object visit(final InfixOp node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.Expression)) {
			((ArrayList<String>) (((Data) __raw__).data)).add(node.jjtGetFirstToken().image);
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	@SuppressWarnings("unchecked")
	public Object visit(final PrefixOp node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.Expression)) {
			((ArrayList<String>) (((Data) __raw__).data)).add(node.jjtGetFirstToken().image);
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	@SuppressWarnings("unchecked")
	public Object visit(final PostfixOp node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.Expression)) {
			((ArrayList<String>) (((Data) __raw__).data)).add(node.jjtGetFirstToken().image);
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	@SuppressWarnings("unchecked")
	public Object visit(final Literal node, final Object __raw__) {
		if (matchContext(__raw__, Semantic.Expression)) {
			((ArrayList<String>) (((Data) __raw__).data)).add(((SimpleNode) node.jjtGetChild(0)).jjtGetFirstToken().image);
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final BlockStatements node, final Object __raw__) {
		String case_label = (String) ((Data) __raw__).data;
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

	public Object visit(final LocalVariableDeclarationStatement node, final Object __raw__) {

		if (matchContext(__raw__, Semantic.MethodBody)) {
			Zeus.singleton.connectClassDatabase().connectMethodDatabase().addFlow("action", "Variable Declaration");
			propagate(node, new Data(__raw__, Semantic.VariableDeclaration, null));
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final Type node, final Object __raw__) {

		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			propagate(node, new Data(__raw__, Semantic.ClassMetadata.Extends, null));
		}

		if (matchLexical(__raw__, Semantic.ClassBody.Member)) {
			propagate(node, new Data(__raw__, Semantic.ClassBody.Member.Type, null));
		}

		if (matchLexical(__raw__, Semantic.MethodMetadata.Parameters)) {
			propagate(node, new Data(__raw__, Semantic.MethodMetadata.Parameters.Type, null));
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final ReferenceType node, final Object __raw__) {
		node.jjtGetChild(0).jjtAccept(this, __raw__);
		// ReferenceType later part
		// propagate(node, new Data(null, null, null));
		return __raw__;
	}

	public Object visit(final TypeList node, final Object __raw__) {
		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			propagate(node, new Data(__raw__, Semantic.ClassMetadata.Implements, null));
		}

		else {
			propagate(node, new Data(null, null, null));
		}

		return __raw__;
	}

	public Object visit(final BasicType node, final Object __raw__) {
		if (matchLexical(__raw__, Semantic.ClassBody.Member.Type)) {
			Zeus.singleton.connectClassDatabase().declare(node.jjtGetFirstToken().image, null);
		}
		propagate(node, new Data(null, null, null));
		return __raw__;
	}

	@SuppressWarnings("unchecked")
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

		if (matchLexical(__raw__, Semantic.ClassMetadata)) {
			Zeus.singleton.connectClassDatabase().name = identifier_name;
		}

		else if (matchLexical(__raw__, Semantic.ClassMetadata.Extends)) {
			Zeus.singleton.connectClassDatabase().extnds = identifier_name;
		}

		else if (matchLexical(__raw__, Semantic.ClassMetadata.Implements)) {
			Zeus.singleton.connectClassDatabase().implments.add(identifier_name);
		}

		else if (matchLexical(__raw__, Semantic.ClassBody.Member)) {
			Zeus.singleton.connectClassDatabase().declare(null, identifier_name);
		}

		else if (matchLexical(__raw__, Semantic.ClassBody.Member.Type)) {
			Zeus.singleton.connectClassDatabase().declare(identifier_name, null);
		}

		else if (matchContext(__raw__, Semantic.Expression)) {
			((ArrayList<String>) (((Data) __raw__).data)).add(identifier_name);
		}

		propagate(node, new Data(null, null, null));
		return __raw__;
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

		private final ClassMetadata ClassMetadata = new ClassMetadata();

		private class ClassMetadata extends SemanticPath {

			private ClassMetadata() {
				super("ClassMetadata");
			}

			private final SemanticPath Extends = new SemanticPath(this.__path__, "Extends");
			private final SemanticPath Implements = new SemanticPath(this.__path__, "Implements");
		}

		private final ClassBody ClassBody = new ClassBody();

		private class ClassBody extends SemanticPath {

			private ClassBody() {
				super("ClassBody");
			}

			private final Member Member = new Member(this.__path__);

			private class Member extends SemanticPath {

				private Member(final String current_path) {
					super(current_path, "Member");
				}

				private final SemanticPath Type = new SemanticPath(this.__path__, "Type");
			}
		}

		private final MethodMetadata MethodMetadata = new MethodMetadata();

		private class MethodMetadata extends SemanticPath {
			private MethodMetadata() {
				super("MethodMetadata");
			}

			private final Parameters Parameters = new Parameters(this.__path__);

			private class Parameters extends SemanticPath {
				private Parameters(final String current_path) {
					super(current_path, "Parameters");
				}

				private final SemanticPath Type = new SemanticPath(this.__path__, "Type");
			}
		}

		private final MethodBody MethodBody = new MethodBody();

		private class MethodBody extends SemanticPath {
			private MethodBody() {
				super("MethodBody");
			}
		}

		private final SemanticPath IfStatement = new SemanticPath("IfStatement");
		private final SemanticPath WhileStatement = new SemanticPath("WhileStatement");
		private final SemanticPath DoStatement = new SemanticPath("DoStatement");
		private final SemanticPath ForStatement = new SemanticPath("ForStatement");
		private final SemanticPath SwitchStatement = new SemanticPath("SwitchStatement");
		private final SemanticPath CaseStatement = new SemanticPath("CaseStatement");
		private final SemanticPath VariableDeclaration = new SemanticPath("VariableDeclaration");
		private final SemanticPath Expression = new SemanticPath("Expression");
	}

	private class Data {

		private final SemanticPath context;
		private final SemanticPath lexical;
		private Object data;

		private Data(final SemanticPath context, final SemanticPath lexical, final Object data) {
			this.context = context;
			this.lexical = lexical;
			this.data = data;
		}

		private Data(final Object data_with_context, final SemanticPath lexical, final Object data) {
			this.context = ((Data) data_with_context).context;
			this.lexical = lexical;
			this.data = data;
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

}
