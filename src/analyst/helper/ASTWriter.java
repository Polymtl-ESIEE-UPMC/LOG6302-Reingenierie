package analyst.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;

import javaparser.Node;

// This is a dev-tool module. It helps visualize the AST during development. Refer to README for "make ast" command
public class ASTWriter extends DotWriter {

  private HashMap<String, HashSet<String>> ast = new HashMap<String, HashSet<String>>();

  public static ASTWriter singleton = new ASTWriter();

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    String from = args[0];
    String to = args[1];
    int depth = Integer.parseInt(args[2]);

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
    ArrayStack<String> working_list = new ArrayStack<String>();
    /* Get partial AST */
    working_list.push(from);
    while (!working_list.isEmpty()) {
      String working_node = working_list.pop();
      required_ast.put(working_node, saved_ast.get(working_node));
      if (!required_ast.get(working_node).isEmpty() && !working_node.equals(to)) {
        for (String node : required_ast.get(working_node)) {
          if (required_ast.get(node) == null) {
            working_list.push(node);
          }
        }
      }
    }
    /* Remove path doesn't lead to required node */
    working_list.push(to);
    while (!working_list.isEmpty()) {
      String working_node = working_list.pop();
      for (String node : required_ast.keySet()) {
        if (required_ast.get(node).isEmpty() && !node.equals(working_node)) {
          working_list.push(node);
        } else if (!working_node.equals(to)) {
          required_ast.get(node).remove(working_node);
        }
      }
      required_ast.remove(working_node);
    }
    /* Compact AST based on depth argument */
    HashMap<String, HashSet<String>> compact_ast = new HashMap<String, HashSet<String>>();
    working_list.push(from);
    for (int i = 0; i < depth; i++) {
      ArrayStack<String> last_depth_nodes = new ArrayStack<String>();
      while (!working_list.isEmpty()) {
        String working_node = working_list.pop();
        compact_ast.put(working_node, required_ast.get(working_node));
        for (String node : compact_ast.get(working_node)) {
          if (compact_ast.get(node) == null && !node.equals(to))
            last_depth_nodes.push(node);
        }
      }
      working_list = last_depth_nodes;
    }
    while (!working_list.isEmpty()) {
      String working_node = working_list.pop();
      ArrayStack<String> another_working_list = new ArrayStack<String>();
      another_working_list.push(working_node);
      HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
      while (!another_working_list.isEmpty()) {
        String another_working_node = another_working_list.pop();
        visited.put(another_working_node, true);
        for (String node : required_ast.get(another_working_node)) {
          if (compact_ast.get(node) != null || node.equals(to)) {
            if (compact_ast.get(working_node) == null)
              compact_ast.put(working_node, new HashSet<String>());
            compact_ast.get(working_node).add(node);
          } else {
            if (visited.get(node) == null)
              another_working_list.push(node);
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
    if (this.ast.get(node.getClass().getSimpleName()) == null) {
      this.ast.put(node.getClass().getSimpleName(), new HashSet<String>());
    }
    if (node.jjtGetParent() != null) {
      this.ast.get(node.jjtGetParent().getClass().getSimpleName()).add(node.getClass().getSimpleName());
    }
  }

  public void done() {
    try {
      FileOutputStream fos = new FileOutputStream("./dev-data/AST.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(this.ast);
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
      super.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}