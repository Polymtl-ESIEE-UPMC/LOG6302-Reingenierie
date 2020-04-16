package analyst.core.zeus;

import java.util.HashSet;

import analyst.helper.ArrayStack;

public class ClassData {

  public String type;
  public String name;
  public String extnds;
  public HashSet<String> implments = new HashSet<String>();
  public ArrayStack<FieldData> fields = new ArrayStack<FieldData>();
  public ArrayStack<MethodData> methods = new ArrayStack<MethodData>();

  public void declare(final String type, final String name) {
    if (type != null) {
      fields.push(new FieldData(type, name));
    } else if (name != null) {
      fields.peek().declareName(name);
    }
  }

  public void declareMethod() {
    final FieldData it_turns_out_to_be_a_method = fields.pop();
    methods.push(new MethodData(it_turns_out_to_be_a_method.type, it_turns_out_to_be_a_method.name));
  }

  public MethodData connectMethodDatabase() {
    return methods.peek();
  }
}