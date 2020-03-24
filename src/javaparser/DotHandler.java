package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DotHandler {

  private final boolean FEATURE_FLAG_UML = true;
  private final boolean FEATURE_FLAG_CFG = false;

  private class DotNode {

    private class MethodOrField {
      private final String name;
      private final String type;

      private MethodOrField(final String name, final String type) {
        this.name = name;
        this.type = type;
      }
    }

    private final String name;
    private boolean entry = false;
    private boolean exit = false;
    private final List<MethodOrField> field = new ArrayList<MethodOrField>();
    private final List<MethodOrField> method = new ArrayList<MethodOrField>();
    private List<DotNode> from = new ArrayList<DotNode>();
    private List<DotNode> to = new ArrayList<DotNode>();

    private DotNode(final String name) {
      this.name = name;
      this.entry = false;
    }

    private DotNode(final boolean entry, final String name) {
      this.name = name;
      this.entry = entry;
      if (entry)
        from = null;
    }

    private DotNode(final String name, final boolean exit) {
      this.name = name;
      this.exit = exit;
      if (exit)
        to = null;
    }

    private void addField(final String name, final String type) {
      field.add(new MethodOrField(name, type));
    }

    private void addMethod(final String name, final String type) {
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

  private class DotTree {
    private final HashMap<String, DotNode> dot_tree = new HashMap<String, DotNode>();

    private DotNode get(final String key) {
      if (this.dot_tree.get(key) == null)
        this.dot_tree.put(key, new DotNode(key));
      return this.dot_tree.get(key);
    }

    private void put(final boolean entry, final String key) {
      this.dot_tree.put(key, new DotNode(entry, key));
    }

    private void put(final String key, final boolean exit) {
      this.dot_tree.put(key, new DotNode(key, exit));
    }

    private Collection<DotNode> values() {
      return this.dot_tree.values();
    }

  }

  private final DotTree uml_tree = new DotTree();
  private final DotTree cfg_tree = new DotTree();

  private static DotHandler dot_handler_instance = new DotHandler();

  public static DotHandler getInstance() {
    return dot_handler_instance;
  }

  public SetRelation setRelationUML() {
    return new SetRelation("UML");
  }

  public Add add() {
    return new Add();
  }

  public void done() {
    for (final DotNode dot_node : this.uml_tree.values()) {
      if (dot_node.from.size() > 0) {
        new DotFile(dot_node);
      }
    }
  }

  class SetRelation {

    private DotTree __anonymous_tree__;

    private SetRelation(final String type) {
      switch (type) {
        case "CFG":
          this.__anonymous_tree__ = cfg_tree;
        default:
          this.__anonymous_tree__ = uml_tree;
      }
    }

    public From from(final String name) {
      return new From(this.__anonymous_tree__.get(name));
    }

    public From from() {
      return new FromEntry();
    }

    class From {

      private final DotNode from;

      private From(final DotNode from) {
        this.from = from;
      }

      public void to(final String name) {
        this.from.to.add(__anonymous_tree__.get(name));
        __anonymous_tree__.get(name).from.add(this.from);
      }

      public To to() {
        return new To();
      }

      class To {
        public void exit(final String name) {
          __anonymous_tree__.put(name, true);
          to(name);
        }
      }
    }

    class FromEntry extends From {
      private FromEntry() {
        super(null);
      }

      public From entry(final String name) {
        __anonymous_tree__.put(true, name);
        return new From(__anonymous_tree__.get(name));
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
      private final String name;
      private final String type;

      private Field(final String name, final String type) {
        this.name = name;
        this.type = type;
      }

      public void to(final String name) {
        uml_tree.get(name).addField(this.name, this.type);
      }
    }

    class Method {
      private final String name;
      private final String type;

      private Method(final String name, final String type) {
        this.name = name;
        this.type = type;
      }

      public void to(final String name) {
        uml_tree.get(name).addMethod(this.name, this.type);
      }
    }
  }

  private class DotFile {

    private FileOutputStream output_stream_uml_dot_file;
    private int indent = 0;
    private boolean new_line = true;

    private DotFile(final DotNode dot_node) {
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
      for (int i = 0; i < dot_node.from.size(); i++) {
        writeln(dot_node.from.get(i).name + " -> " + dot_node.name);
        newLine();
        writeNode(dot_node.from.get(i));
      }
      for (int i = 0; i < dot_node.to.size(); i++) {
        writeln(dot_node.name + " -> " + dot_node.to.get(i).name);
        newLine();
        writeNode(dot_node.to.get(i));
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

  private static class StringEditor {
    private String str;

    private StringEditor(final String str) {
      this.str = str;
    }

    private static StringEditor edit(final String str) {
      return new StringEditor(str);
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