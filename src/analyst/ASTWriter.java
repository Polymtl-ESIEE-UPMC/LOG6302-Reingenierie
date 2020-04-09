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
  private HashMap<String, HashSet<String>> ast = new HashMap<String, HashSet<String>>();

  public static ASTWriter singleton = new ASTWriter();

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    String from = args[0];
    String to = args[1];
    int depth;
    depth = Integer.parseInt(args[2]);

    /* Read AST from file */
    HashMap<String, HashSet<String>> saved_ast = null;
    try {
      FileInputStream fis = new FileInputStream("./dev-data/AST.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      saved_ast = (HashMap<String, HashSet<String>>) ois.readObject();
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
    /* ****************************** */

    HashMap<String, HashSet<String>> required_ast = new HashMap<String, HashSet<String>>();
    ArrayList<String> working_list = new ArrayList<String>();
    /* Get partial AST */
    working_list.add(from);
    while (!working_list.isEmpty()) {
      String working_node = working_list.remove(0);
      required_ast.put(working_node, saved_ast.get(working_node));
      if (!required_ast.get(working_node).isEmpty() && !working_node.equals(to)) {
        for (String node : required_ast.get(working_node)) {
          if (required_ast.get(node) == null) {
            working_list.add(node);
          }
        }
      }
    }
    /* Remove path doesn't lead to required node */
    working_list.add(to);
    while (!working_list.isEmpty()) {
      String working_node = working_list.remove(0);
      for (String node : required_ast.keySet()) {
        if (required_ast.get(node).isEmpty() && !node.equals(working_node)) {
          working_list.add(node);
        } else if (!working_node.equals(to)) {
          required_ast.get(node).remove(working_node);
        }
      }
      required_ast.remove(working_node);
    }
    /* Compact AST based on depth argument */
    HashMap<String, HashSet<String>> compact_ast = new HashMap<String, HashSet<String>>();
    working_list.add(from);
    for (int i = 0; i < depth; i++) {
      ArrayList<String> last_depth_nodes = new ArrayList<String>();
      while (!working_list.isEmpty()) {
        String working_node = working_list.remove(0);
        compact_ast.put(working_node, required_ast.get(working_node));
        for (String node : compact_ast.get(working_node)) {
          if (compact_ast.get(node) == null && !node.equals(to))
            last_depth_nodes.add(node);
        }
      }
      working_list = last_depth_nodes;
    }
    while (!working_list.isEmpty()) {
      String working_node = working_list.remove(0);
      ArrayList<String> another_working_list = new ArrayList<String>();
      another_working_list.add(working_node);
      HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
      while (!another_working_list.isEmpty()) {
        String another_working_node = another_working_list.remove(0);
        visited.put(another_working_node, true);
        for (String node : required_ast.get(another_working_node)) {
          if (compact_ast.get(node) != null || node.equals(to)) {
            if (compact_ast.get(working_node) == null)
              compact_ast.put(working_node, new HashSet<String>());
            compact_ast.get(working_node).add(node);
          } else {
            if (visited.get(node) == null)
              another_working_list.add(node);
          }
        }
      }
    }

    /* Write requried AST to .dot file */
    singleton.createDotFile();
    singleton.writeHeader();

    for (String fr : compact_ast.keySet()) {
      for (String t : compact_ast.get(fr)) {
        singleton.writeln(fr + " -> " + t);
      }
    }

    singleton.end("}");
    try {
      singleton.output_stream_uml_dot_file.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  public void register(Node node) {
    if (ast.get(node.getClass().getSimpleName()) == null) {
      ast.put(node.getClass().getSimpleName(), new HashSet<String>());
    }
    if (node.jjtGetParent() != null) {
      ast.get(node.jjtGetParent().getClass().getSimpleName()).add(node.getClass().getSimpleName());
    }
  }

  public void done() {
    try {
      FileOutputStream fos = new FileOutputStream("./dev-data/AST.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(ast);
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