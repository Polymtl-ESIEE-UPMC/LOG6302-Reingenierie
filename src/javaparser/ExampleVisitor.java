package javaparser;

public class ExampleVisitor extends AbstractVisitor {
	private final String file_name;

	public ExampleVisitor(final String file_name) {
		this.file_name = file_name;
	}

	public Object visit(final CompilationUnit node, final Object __raw__) {
		propagate(node, __raw__);
		return __raw__;
	}

	public Object visit(final ClassDeclaration node, final Object __raw__) {
		Zeus.getSingleton().connectDatabase(node);
		propagate(node, new Data(TypeData.ClassType, null));
		Zeus.getSingleton().disconnectDatabase();
		return __raw__;
	}

	public Object visit(final NormalClassDeclaration node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassType)) {
			Zeus.getSingleton().connectDatabase().type = "class";
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
			Zeus.getSingleton().connectDatabase().type = "enum";
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
			Zeus.getSingleton().connectDatabase().addField();
		}
		return __raw__;
	}

	public Object visit(final MethodDeclaratorRest node, final Object __raw__) {
		methodHandler(node, __raw__);
		return __raw__;
	}

	public Object visit(final VoidMethodDecl node, final Object __raw__) {
		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			Zeus.getSingleton().connectDatabase().inh.type = "void";
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
			Zeus.getSingleton().connectDatabase().addMethod();
		}
	}

	public Object visit(final Type node, final Object __raw__) {

		if (matchTypePath(__raw__, TypeData.ClassMetadata)) {
			propagate(node, new Data(TypeData.ClassMetadata.Extends, null));
		}

		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			propagate(node, new Data(TypeData.ClassBody.Member.Type, null));
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
			Zeus.getSingleton().connectDatabase().inh.type = node.jjtGetFirstToken().image;
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
			Zeus.getSingleton().connectDatabase().name = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.ClassMetadata.Extends)) {
			Zeus.getSingleton().connectDatabase().extnds = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.ClassMetadata.Implements)) {
			Zeus.getSingleton().connectDatabase().implments.add(identifier_name);
		}

		if (matchTypePath(__raw__, TypeData.ClassBody.Member)) {
			Zeus.getSingleton().connectDatabase().inh.name = identifier_name;
		}

		if (matchTypePath(__raw__, TypeData.ClassBody.Member.Type)) {
			Zeus.getSingleton().connectDatabase().inh.type = identifier_name;
		}

		return __raw__;
	}

	private final TypeData TypeData = new TypeData();

	private class TypePath {

		protected final String path;

		private TypePath(final String path) {
			this.path = path;
		}

		private TypePath(final String current_path, final String path) {
			this.path = current_path + path;
		}
	}

	private class TypeData {

		private final TypePath ClassType = new TypePath("ClassType");

		private final ClassMetadata ClassMetadata = new ClassMetadata();

		private class ClassMetadata extends TypePath {

			private ClassMetadata() {
				super("ClassMetadata");
			}

			private final TypePath Extends = new TypePath(this.path, "Extends");
			private final TypePath Implements = new TypePath(this.path, "Implements");
		}

		private final ClassBody ClassBody = new ClassBody();

		private class ClassBody extends TypePath {

			private ClassBody() {
				super("ClassBody");
			}

			private final Member Member = new Member(this.path);

			private class Member extends TypePath {

				private Member(final String current_path) {
					super(current_path, "Member");
				}

				private final TypePath Type = new TypePath(this.path, "Type");
			}
		}

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
		if (o != null && ((Data) o).type != null && ((Data) o).type.path.equals(type.path))
			return true;
		return false;
	}

}
