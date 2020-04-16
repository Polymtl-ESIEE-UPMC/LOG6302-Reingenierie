package analyst.core.zeus;

import java.util.HashSet;

/* mirror de ClassData pour l'affichage */
public class DotNodeUML extends ClassData {

  public boolean is_place_holder = false;
  public HashSet<DotNodeUML> froms = new HashSet<DotNodeUML>();
  public final HashSet<DotNodeUML> tos = new HashSet<DotNodeUML>();
  public final HashSet<String> to_labels = new HashSet<String>();

  public DotNodeUML(final ClassData class_data) {
    this.type = class_data.type;
    this.name = class_data.name;
    this.fields = class_data.fields;
    this.methods = class_data.methods;
    this.to_labels.addAll(class_data.implments);
    if (class_data.extnds != null)
      this.to_labels.add(class_data.extnds);
  }

  public DotNodeUML(final String name) {
    this.name = name;
    this.is_place_holder = true;
  }

  public String toString() {
    String s = "{" + this.type + ": " + this.name + "|";
    for (int i = 0; i < this.fields.size(); i++) {
      s += "+ " + this.fields.get(i).name + " : " + this.fields.get(i).type + "\\l";
    }
    s += "|";
    for (int i = 0; i < this.methods.size(); i++) {
      s += "+ " + this.methods.get(i).name + "() : " + this.methods.get(i).return_type + "\\l";
    }
    s += "}";
    return s;
  }
}