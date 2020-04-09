package analyst;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javaparser.Node;

// This is a dev-tool module. It helps visualize the AST during development. Refer to README for "make ast" command
public class ASTWriter {

  private FileOutputStream output_stream_uml_dot_file;
  private int indent = 0;
  private boolean new_line = true;
  private HashMap<String, HashSet<String>> relation = new HashMap<String, HashSet<String>>();

  public static ASTWriter singleton = new ASTWriter();

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    String from = args[0];
    String to = args[1];
    HashMap<String, HashSet<String>> all_relation = null;
    try {
      FileInputStream fis = new FileInputStream("./dev-data/AST.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      all_relation = (HashMap<String, HashSet<String>>) ois.readObject();
      ois.close();
      fis.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return;
    } catch (ClassNotFoundException c) {
      System.out.println("Class not found");
      c.printStackTrace();
      return;
    }

    ASTWriter ast_writer = new ASTWriter();
    ast_writer.createDotFile();
    ast_writer.writeHeader();

    HashMap<String, HashSet<String>> required_relation = new HashMap<String, HashSet<String>>();
    ArrayList<String> working_list = new ArrayList<String>();
    working_list.add(from);
    while (!working_list.isEmpty()) {
      String working_node = working_list.remove(0);
      required_relation.put(working_node, all_relation.get(working_node));
      if (!required_relation.get(working_node).isEmpty() && !working_node.equals(to)) {
        for (String node : required_relation.get(working_node)) {
          if (required_relation.get(node) == null) {
            working_list.add(node);
          }
        }
      }
    }
    working_list.add(to);
    while (!working_list.isEmpty()) {
      String working_node = working_list.remove(0);
      for (String node : required_relation.keySet()) {
        if (required_relation.get(node).isEmpty() && !node.equals(working_node)) {
          working_list.add(node);
        } else if (!working_node.equals(to)) {
          required_relation.get(node).remove(working_node);
        }
      }
      required_relation.remove(working_node);
    }
    for (String fr : required_relation.keySet()) {
      for (String t : required_relation.get(fr)) {
        ast_writer.writeln(fr + " -> " + t);
      }
    }

    ast_writer.end("}");
    try {
      ast_writer.output_stream_uml_dot_file.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  public void register(Node node) {
    relation.put(node.getClass().getSimpleName(), new HashSet<String>());
    if (node.jjtGetParent() != null) {
      if (relation.get(node.jjtGetParent().getClass().getSimpleName()) == null) {
        relation.put(node.jjtGetParent().getClass().getSimpleName(), new HashSet<String>());
      }
      relation.get(node.jjtGetParent().getClass().getSimpleName()).add(node.getClass().getSimpleName());
    }
  }

  public void done() {
    try {
      FileOutputStream fos = new FileOutputStream("./dev-data/AST.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(relation);
      oos.close();
      fos.close();
      System.out.printf("Serialized HashMap data is saved in hashmap.ser");
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void createDotFile() {
    try {
      final File uml_dot_file = new File("./dev-data/AST.dot");
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
        this.output_stream_uml_dot_file.write((openStringEditor(str).indent(indent).close()).getBytes());
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

  private StringEditor openStringEditor(String str) {
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

    private String close() {
      return this.str;
    }
  }

}