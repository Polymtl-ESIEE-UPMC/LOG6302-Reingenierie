package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DotHandler {

  private final boolean FEATURE_UML = true;
  private final boolean FEATURE_CFG = false;

  private static DotHandler dot_handler_instance = new DotHandler();

  class DotNode {

    class MethodOrField {
      public String name;
      public String type;

      public MethodOrField(final String name, final String type) {
        this.name = name;
        this.type = type;
      }
    }

    public String name;
    public List<MethodOrField> field = new ArrayList<MethodOrField>();
    public List<MethodOrField> method = new ArrayList<MethodOrField>();
    public List<DotNode> children = new ArrayList<DotNode>();
    public List<DotNode> parents = new ArrayList<DotNode>();

    public DotNode(final String name) {
      this.name = name;
    }

    public void addField(final String name, final String type) {
      field.add(new MethodOrField(name, type));
    }

    public void addMethod(final String name, final String type) {
      method.add(new MethodOrField(name, type));
    }

    public String toString() {
      String s = "label = \"{" + this.name + "|";
      for (int i = 0; i < this.field.size(); i++) {
        s += "+ " + this.field.get(i).name + " : " + this.field.get(i).type + "\\l";
      }
      s += "|";
      for (int i = 0; i < this.method.size(); i++) {
        s += "+ " + this.method.get(i).name + "() : " + this.method.get(i).type + "\\l";
      }
      s += "}\"";
      return s;
    }
  }

  class DotNodes {
    private final HashMap<String, DotNode> dot_nodes = new HashMap<String, DotNode>();

    public DotNode get(String key) {
      if (this.dot_nodes.get(key) == null)
        this.dot_nodes.put(key, new DotNode(key));
      return this.dot_nodes.get(key);
    }

    public void put(String key, DotNode node) {
      this.dot_nodes.put(key, node);
    }

    public Collection<DotNode> values() {
      return this.dot_nodes.values();
    }
  }

  private final DotNodes dot_nodes = new DotNodes();

  public static DotHandler getInstance() {
    return dot_handler_instance;
  }

  public SetRelation setRelation() {
    return new SetRelation();
  }

  public Add add() {
    return new Add();
  }

  public void done() {
    for (final DotNode dot_node : this.dot_nodes.values()) {
      if (dot_node.children.size() > 0) {
        new DotFile(dot_node);
      }
    }
  }

  class SetRelation {

    public From from(final String name) {
      return new From(dot_nodes.get(name));
    }

    class From {

      private DotNode from;

      public From(DotNode from) {
        this.from = from;
      }

      public void to(final String name) {
        this.from.parents.add(dot_nodes.get(name));
        dot_nodes.get(name).children.add(this.from);
      }
    }
  }

  class Add {
    public Field field(final String name, final String type) {
      return new Field(name, type);
    }

    public Method method(final String name, final String type) {
      return new Method(name, type);
    }

    class Field {
      private String name;
      private String type;

      public Field(final String name, final String type) {
        this.name = name;
        this.type = type;
      }

      public void to(final String name) {
        dot_nodes.get(name).addField(this.name, this.type);
      }
    }

    class Method {
      private String name;
      private String type;

      public Method(final String name, final String type) {
        this.name = name;
        this.type = type;
      }

      public void to(final String name) {
        dot_nodes.get(name).addMethod(this.name, this.type);
      }
    }
  }

  class DotFile {

    private FileOutputStream output_stream_uml_dot_file;
    private int indent = 0;
    private boolean new_line = true;

    public DotFile(final DotNode dot_node) {
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

    private void createDotFile(final DotNode dot_node) {
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

    private void writeRelation(final DotNode dot_node) {
      for (int i = 0; i < dot_node.children.size(); i++) {
        writeln(dot_node.children.get(i).name + " -> " + dot_node.name);
        newLine();
        writeNode(dot_node.children.get(i));
      }
      for (int i = 0; i < dot_node.parents.size(); i++) {
        writeln(dot_node.name + " -> " + dot_node.parents.get(i).name);
        newLine();
        writeNode(dot_node.parents.get(i));
      }
    }

    private void writeNode(final DotNode dot_node) {
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
          this.output_stream_uml_dot_file.write((StringEditor.edit(str).indent(indent).done()).getBytes());
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

  }

  static class StringEditor {
    private String str;

    private StringEditor(final String str) {
      this.str = str;
    }

    public static StringEditor edit(final String str) {
      return new StringEditor(str);
    }

    public StringEditor indent(final int n) {
      if (n < 0)
        throw new Error("Expect positive value in indent function of class CustomString, instead having " + n);
      for (int i = 0; i < n; i++) {
        this.str = "  " + this.str;
      }
      return this;
    }

    public String done() {
      return this.str;
    }
  }

}