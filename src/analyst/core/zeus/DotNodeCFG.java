package analyst.core.zeus;

public class DotNodeCFG extends ClassData {
  public DotNodeCFG(final ClassData class_data) {
    super.type = class_data.type;
    super.name = class_data.name;
    super.fields = class_data.fields;
    super.methods = class_data.methods;
  }
}