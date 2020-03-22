package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DotHandler {

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

  private final HashMap<String, DotNode> dot_nodes = new HashMap<String, DotNode>();
  private DotNode __temp__ = null;

  public static DotHandler getInstance() {
    return dot_handler_instance;
  }

  public DotHandler setRelation() {
    return this;
  }

  public DotHandler from(final String name) {
    this.__temp__ = this.getNode(name);
    return this;
  }

  public void to(final String name) {
    this.__temp__.parents.add(this.getNode(name));
    this.getNode(name).children.add(this.__temp__);
    this.__temp__ = null;
  }

  public DotNode getNode(final String name) {
    if (this.dot_nodes.get(name) == null)
      this.dot_nodes.put(name, new DotNode(name));
    return this.dot_nodes.get(name);
  }

  public void finish() {
    for (final DotNode dot_node : this.dot_nodes.values()) {
      if (dot_node.children.size() > 0) {
        new DotFile(dot_node);
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
        writeln(dot_node.children.get(i) + " -> " + dot_node.name);
        writeNode(dot_node.children.get(i));
      }
      for (int i = 0; i < dot_node.parents.size(); i++) {
        writeln(dot_node.name + " -> " + dot_node.parents.get(i));
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
          this.output_stream_uml_dot_file
              .write((CustomString.makeCustomString(str).indent(indent).finish()).getBytes());
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

  static class CustomString {
    private String str;

    private CustomString(final String str) {
      this.str = str;
    }

    public static CustomString makeCustomString(final String str) {
      return new CustomString(str);
    }

    public CustomString indent(int n) {
      if (n < 0)
        throw new Error("Expect positive value in indent function of class CustomString, instead having " + n);
      for (int i = 0; i < n; i++) {
        this.str = "  " + this.str;
      }
      return this;
    }

    public String finish() {
      return this.str;
    }
  }

}