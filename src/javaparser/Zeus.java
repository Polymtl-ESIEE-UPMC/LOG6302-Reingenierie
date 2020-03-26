package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Zeus {

  private final boolean FEATURE_FLAG_UML = true;
  private final boolean FEATURE_FLAG_CFG = false;

  class ClassData {

    class FieldData {

      public String type;
      public String name;

      private FieldData() {
      }

      private FieldData(final String type, final String name) {
        this.type = type;
        this.name = name;
      }
    }

    private class MethodData {

      private final String return_type;
      private final String name;

      private MethodData(final String return_type, final String name) {
        this.return_type = return_type;
        this.name = name;
      }
    }

    public String type;
    public String name;
    public String extnds;
    public List<String> implments = new ArrayList<String>();
    public FieldData inh = new FieldData();
    protected List<FieldData> fields = new ArrayList<FieldData>();
    protected List<MethodData> methods = new ArrayList<MethodData>();

    public void addField() {
      fields.add(new FieldData(this.inh.type, this.inh.name));
    }

    public void addMethod() {
      methods.add(new MethodData(this.inh.type, this.inh.name));
    }
  }

  private interface DotNode {
  }

  private class DotNodeUML extends ClassData implements DotNode {

    private boolean is_place_holder = false;
    private List<DotNodeUML> from = new ArrayList<DotNodeUML>();
    private final List<DotNodeUML> to = new ArrayList<DotNodeUML>();
    private final List<String> to_label = new ArrayList<String>();

    private DotNodeUML(final ClassData class_data) {
      this.type = class_data.type;
      this.name = class_data.name;
      this.fields = class_data.fields;
      this.methods = class_data.methods;
      this.to_label.addAll(class_data.implments);
      if (class_data.extnds != null)
        this.to_label.add(class_data.extnds);
    }

    private DotNodeUML(final String name) {
      this.name = name;
      this.is_place_holder = true;
    }

    public String toString() {
      String s = "label = \"{" + this.type + ": " + this.name + "|";
      for (int i = 0; i < this.fields.size(); i++) {
        s += "+ " + this.fields.get(i).name + " : " + this.fields.get(i).type + "\\l";
      }
      s += "|";
      for (int i = 0; i < this.methods.size(); i++) {
        s += "+ " + this.methods.get(i).name + "() : " + this.methods.get(i).return_type + "\\l";
      }
      s += "}\"";
      return s;
    }
  }

  private class DotNodeCFG implements DotNode {
    private String type;
    private String name;
    private final boolean entry = false;
    private final boolean exit = false;
    // private final List<FieldData> fields = new ArrayList<FieldData>();
    // private final List<MethodData> methods = new ArrayList<MethodData>();
    private final List<DotNodeCFG> from = new ArrayList<DotNodeCFG>();
    private final List<DotNodeCFG> to = new ArrayList<DotNodeCFG>();
  }

  private class DataBase {
    private final HashMap<ClassDeclaration, ClassData> class_database = new HashMap<ClassDeclaration, ClassData>();
    private final LinkedList<SimpleNode> sessions = new LinkedList<SimpleNode>();
  }

  private final DataBase database = new DataBase();
  private final HashMap<String, DotNodeUML> dot_nodes_uml = new HashMap<String, DotNodeUML>();

  private static Zeus zeus = new Zeus();

  public static Zeus getSingleton() {
    return zeus;
  }

  public ClassData connectDatabase(final ClassDeclaration node) {
    if (database.class_database.get(node) == null)
      database.class_database.put(node, new ClassData());
    database.sessions.addFirst(node);
    return database.class_database.get(node);
  }

  public ClassData connectDatabase() {
    if (database.sessions.getFirst() instanceof ClassDeclaration) {
      return connectDatabase((ClassDeclaration) database.sessions.pollFirst());
    }
    return null;
  }

  public void disconnectDatabase() {
    updateUML();
    database.sessions.removeFirst();
  }

  private void updateUML() {
    final DotNodeUML node = new DotNodeUML(connectDatabase());
    final DotNodeUML place_holder = dot_nodes_uml.put(node.name, node);
    if (place_holder != null)
      node.from = place_holder.from;

    for (int i = 0; i < node.to_label.size(); i++) {

      if (dot_nodes_uml.get(node.to_label.get(i)) == null)
        dot_nodes_uml.put(node.to_label.get(i), new DotNodeUML(node.to_label.get(i)));

      node.to.add(dot_nodes_uml.get(node.to_label.get(i)));
      dot_nodes_uml.get(node.to_label.get(i)).from.add(node);
    }
  }

  public void done() {
    for (final DotNodeUML dot_node : this.dot_nodes_uml.values()) {
      if (!dot_node.is_place_holder) {
        new DotFile(dot_node);
      }
    }
  }

  private class DotFile {

    private FileOutputStream output_stream_uml_dot_file;
    private int indent = 0;
    private boolean new_line = true;

    private DotFile(final DotNodeUML dot_node) {
      if (FEATURE_FLAG_UML) {
        createDotFile(dot_node);
        writeHeader();
        writeRelation(dot_node);
        writeNode(dot_node);
        end("}");
        try {
          this.output_stream_uml_dot_file.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }

    private void createDotFile(final DotNodeUML dot_node) {
      try {
        final File uml_dot_file = new File("./results/dot/" + dot_node.name + ".dot");
        uml_dot_file.getParentFile().mkdirs();
        uml_dot_file.createNewFile(); // if file already exists will do nothing
        this.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    private void writeHeader() {
      begin("digraph G {");
      newLine();

      writeln("fontname = \"Bitstream Vera Sans\"");
      writeln("fontsize = 8");
      newLine();

      begin("node [");
      writeln("fontname = \"Bitstream Vera Sans\"");
      writeln("fontsize = 8");
      writeln("shape = \"record\"");
      end("]");

      begin("edge [");
      writeln("fontname = \"Bitstream Vera Sans\"");
      writeln("fontsize = 8");
      end("]");
    }

    private void writeRelation(final DotNodeUML dot_node) {
      for (int i = 0; i < dot_node.from.size(); i++) {
        writeln(dot_node.from.get(i).name + " -> " + dot_node.name);
        newLine();
        writeNode(dot_node.from.get(i));
      }
      for (int i = 0; i < dot_node.to.size(); i++) {
        if (!dot_node.to.get(i).is_place_holder) {
          writeln(dot_node.name + " -> " + dot_node.to.get(i).name);
          newLine();
          writeNode(dot_node.to.get(i));
        }
      }
    }

    private void writeNode(final DotNodeUML dot_node) {
      begin(dot_node.name + " [");
      writeln(dot_node.toString());
      end("]");
    }

    private void begin(final String str) {
      writeln(str);
      this.indent++;
    }

    private void end(final String str) {
      this.indent--;
      writeln(str);
      newLine();
    }

    private void writeln(final String str) {
      write(str);
      write("\n");
      this.new_line = true;
    }

    private void write(final String str) {
      try {
        if (this.new_line) {
          this.output_stream_uml_dot_file.write((edit(str).indent(indent).done()).getBytes());
          this.new_line = false;
        } else {
          this.output_stream_uml_dot_file.write(str.getBytes());
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    private void newLine() {
      writeln("");
    }

    private StringEditor edit(final String str) {
      return new StringEditor(str);
    }

    private class StringEditor {
      private String str;

      private StringEditor(final String str) {
        this.str = str;
      }

      private StringEditor indent(final int n) {
        if (n < 0)
          throw new Error("Expect positive value in indent function of class CustomString, instead having " + n);
        for (int i = 0; i < n; i++) {
          this.str = "  " + this.str;
        }
        return this;
      }

      private String done() {
        return this.str;
      }
    }

  }

}