package analyst.core.zeus;

public class Definition {
  public final String id;
  public final String variable;
  public String expression;
  public final String flow;

  public Definition(final String id, final String variable, final String flow) {
    this.id = id;
    this.variable = variable;
    this.flow = flow;
  }

  public void assignExpression(final String expression) {
    try {
      if (this.expression == null) {
        this.expression = expression;
      } else {
        throw new Exception("Assign expression to no variable");
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}