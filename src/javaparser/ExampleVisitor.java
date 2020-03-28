package javaparser;

public class ExampleVisitor extends AbstractVisitor {
	// private final String file_name;

	public ExampleVisitor(final String file_name) {
		// this.file_name = file_name;
	}

	public Object visit(final CompilationUnit node, final Object __raw__) {
		propagate(node, __raw__);
		return __raw__;
	}

	public Object visit(final ClassDeclaration node, final Object __raw__) {
		Zeus.getSingleton().connectClassDatabase(node);
		propagate(node, new Data(TypeData.ClassType, null));
		Zeus.getSingleton().disconnectClassDatabase();
		return __raw__;
	}

	public Object visit(final NormalClassDeclaration node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassType)) {
			Zeus.getSingleton().connectClassDatabase().type = "class";
			propagate(node, new Data(TypeData.ClassMetadata, null));
		}
		return __raw__;
	}

	public Object visit(final TypeParameters node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			propagate(node, new Data(new TypePath("TypeParameters"), null));
		}
		return __raw__;
	}

	public Object visit(final ClassBody node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			propagate(node, new Data(TypeData.ClassBody, null));
		}
		return __raw__;
	}

	public Object visit(final EnumDeclaration node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassType)) {
			Zeus.getSingleton().connectClassDatabase().type = "enum";
			propagate(node, new Data(TypeData.ClassMetadata, null));
		}
		return __raw__;
	}

	public Object visit(final EnumBody node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			propagate(node, new Data(TypeData.ClassBody, null));
		}
		return __raw__;
	}

	public Object visit(final MemberDecl node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassBody)) {
			propagate(node, new Data(TypeData.ClassBody.Member, null));
		}
		return __raw__;
	}

	public Object visit(final FieldDeclaratorsRest node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			Zeus.getSingleton().connectClassDatabase().addField();
		}
		return __raw__;
	}

	public Object visit(final MethodDeclaratorRest node, final Object __raw__) {
		methodHandler(node, __raw__);
		return __raw__;
	}

	public Object visit(final VoidMethodDecl node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			Zeus.getSingleton().connectClassDatabase().inh.type = "void";
			propagate(node, __raw__);
		}
		return __raw__;
	}

	public Object visit(final VoidMethodDeclaratorRest node, final Object __raw__) {
		methodHandler(node, __raw__);
		return __raw__;
	}

	private void methodHandler(final SimpleNode node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			Zeus.getSingleton().connectClassDatabase().addMethod();
			propagate(node, new Data(TypeData.MethodMetadata, null));
			Zeus.getSingleton().connectClassDatabase().connectMethod().exit();
			Zeus.getSingleton().connectClassDatabase().connectMethod().end();
		}
	}

	public Object visit(final FormalParameters node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.MethodMetadata)) {
			propagate(node, new Data(TypeData.MethodMetadata.Parameters, null));
		}
		return __raw__;
	}

	public Object visit(final MethodBody node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.MethodMetadata)
				&& !(node.jjtGetParent() instanceof ConstructorDeclaratorRest)) {
			propagate(node, new Data(TypeData.MethodBody, null));
		}
		return __raw__;
	}

	public Object visit(final IfStatement node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.MethodBody)) {
			Zeus.getSingleton().connectClassDatabase().connectMethod().begin("if");
			propagate(node, new Data(TypeData.IfStatement, null));
			Zeus.getSingleton().connectClassDatabase().connectMethod().end();
		}
		return __raw__;
	}

	public Object visit(final ParExpression node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.IfStatement)) {
			Zeus.getSingleton().connectClassDatabase().connectMethod().addFlow("if", "IfCondition");
			Zeus.getSingleton().connectClassDatabase().connectMethod().saveCursor();
			// propagate(node, __raw__);
		} else {
			propagate(node, __raw__);
		}
		return __raw__;
	}

	public Object visit(final Statement node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.IfStatement)) {
			Zeus.getSingleton().connectClassDatabase().connectMethod().addFlow("if", "IfConditionTrue");
			Zeus.getSingleton().connectClassDatabase().connectMethod().exit();
			// propagate(node, __raw__);
		} else {
			propagate(node, __raw__);
		}
		return __raw__;
	}

	public Object visit(final ElseStatement node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.IfStatement)) {
			Zeus.getSingleton().connectClassDatabase().connectMethod().loadCursor();
			Zeus.getSingleton().connectClassDatabase().connectMethod().addFlow("if", "IfConditionFalse");
			Zeus.getSingleton().connectClassDatabase().connectMethod().exit();
			// propagate(node, __raw__);
		} else {
			propagate(node, __raw__);
		}
		return __raw__;
	}

	public Object visit(final Type node, final Object __raw__) {

		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			propagate(node, new Data(TypeData.ClassMetadata.Extends, null));
		}

		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			propagate(node, new Data(TypeData.ClassBody.Member.Type, null));
		}

		if (matchTypePath(__raw__, TypeData.MethodMetadata.Parameters)) {
			propagate(node, new Data(TypeData.MethodMetadata.Parameters.Type, null));
		}

		return __raw__;
	}

	public Object visit(final ReferenceType node, final Object __raw__) {
		node.jjtGetChild(0).jjtAccept(this, __raw__);
		return __raw__;
	}

	public Object visit(final TypeList node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			propagate(node, new Data(TypeData.ClassMetadata.Implements, null));
		}
		return __raw__;
	}

	public Object visit(final BasicType node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassBody.Member.Type)) {
			Zeus.getSingleton().connectClassDatabase().inh.type = node.jjtGetFirstToken().image;
		}
		return __raw__;
	}

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

		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			Zeus.getSingleton().connectClassDatabase().name = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.ClassMetadata.Extends)) {
			Zeus.getSingleton().connectClassDatabase().extnds = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.ClassMetadata.Implements)) {
			Zeus.getSingleton().connectClassDatabase().implments.add(identifier_name);
		}

		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			Zeus.getSingleton().connectClassDatabase().inh.name = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.ClassBody.Member.Type)) {
			Zeus.getSingleton().connectClassDatabase().inh.type = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.MethodMetadata.Parameters.Type)) {
			Zeus.getSingleton().connectClassDatabase().connectMethod().inh = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.MethodMetadata.Parameters)) {
			Zeus.getSingleton().connectClassDatabase().connectMethod().addEntry(identifier_name);
		}

		return __raw__;
	}

	private final TypeData TypeData = new TypeData();

	private class TypePath {

		protected final String __path__;

		private TypePath(final String path) {
			this.__path__ = path;
		}

		private TypePath(final String current_path, final String path) {
			this.__path__ = current_path + path;
		}
	}

	private class TypeData {

		private final TypePath ClassType = new TypePath("ClassType");

		private final ClassMetadata ClassMetadata = new ClassMetadata();

		private class ClassMetadata extends TypePath {

			private ClassMetadata() {
				super("ClassMetadata");
			}

			private final TypePath Extends = new TypePath(this.__path__, "Extends");
			private final TypePath Implements = new TypePath(this.__path__, "Implements");
		}

		private final ClassBody ClassBody = new ClassBody();

		private class ClassBody extends TypePath {

			private ClassBody() {
				super("ClassBody");
			}

			private final Member Member = new Member(this.__path__);

			private class Member extends TypePath {

				private Member(final String current_path) {
					super(current_path, "Member");
				}

				private final TypePath Type = new TypePath(this.__path__, "Type");
			}
		}

		private MethodMetadata MethodMetadata = new MethodMetadata();

		private class MethodMetadata extends TypePath {
			private MethodMetadata() {
				super("MethodMetadata");
			}

			private Parameters Parameters = new Parameters(this.__path__);

			private class Parameters extends TypePath {
				private Parameters(String current_path) {
					super(current_path, "Parameters");
				}

				private final TypePath Type = new TypePath(this.__path__, "Type");
			}
		}

		private MethodBody MethodBody = new MethodBody();

		private class MethodBody extends TypePath {
			private MethodBody() {
				super("MethodBody");
			}
		}

		private TypePath IfStatement = new TypePath("IfStatement");

	}

	private class Data {

		private final TypePath type;
		private final Object data;

		private Data(final TypePath type, final Object data) {
			this.type = type;
			this.data = data;
		}

	}

	private boolean matchTypePath(final Object o, final TypePath type) {
		if (o != null && ((Data) o).type != null && ((Data) o).type.__path__.equals(type.__path__))
			return true;
		return false;
	}

}
