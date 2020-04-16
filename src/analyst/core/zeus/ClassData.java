package analyst.core.zeus;

import java.util.HashSet;

import analyst.helper.ArrayStack;

public class ClassData {

  public String type; /* class or enum or whatever */
  public String name; /* class name */
  public String extnds;
  public HashSet<String> implments = new HashSet<String>();
  public ArrayStack<FieldData> fields = new ArrayStack<FieldData>();
  public ArrayStack<MethodData> methods = new ArrayStack<MethodData>();

  /*
   * every new declaration is field by default, until we know its a function
   * declaration
   */
  public void declare(final String type, final String name) {
    if (type != null) {
      fields.push(new FieldData(type, name));
    } else if (name != null) {
      fields.peek().declareName(name);
    }
  }

  /* we see the parenthese, now we know its a method */
  public void declareMethod() {
    final FieldData it_turns_out_to_be_a_method = fields.pop();
    methods.push(new MethodData(it_turns_out_to_be_a_method.type, it_turns_out_to_be_a_method.name));
  }

  /* continue adding details to current method declaration */
  public MethodData connectMethodDatabase() {
    return methods.peek();
  }
}