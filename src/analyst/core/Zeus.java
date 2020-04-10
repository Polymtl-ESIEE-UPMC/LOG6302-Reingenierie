package analyst.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import analyst.helper.*;

public class Zeus {

  private final boolean FEATURE_FLAG_UML = true;
  private final boolean FEATURE_FLAG_CFG = true;
  private final boolean LOG = false;

  // Structure de donnee de la declaration des class
  class ClassData {

    private class FieldData {

      private String type;
      private String name;

      private FieldData() {
      }

      private FieldData(final String type, final String name) {
        this.type = type;
        this.name = name;
      }
    }

    class MethodData {

      private class Flow {
        private final String id = UUID.get();
        private final String type; // condition, entry, end,...etc
        private final String name;
        private boolean alive = true;;
        private final ArrayList<Flow> nexts = new ArrayList<Flow>();

        private Flow(final String type, final String name) {
          this.type = type;
          this.name = name;
        }
      }

      private final String id = UUID.get();
      private final String return_type;
      private final String name;

      private Flow current_cursor = new Flow("begin", "entry");
      private final ArrayStack<Flow> saved_cursors = new ArrayStack<Flow>();
      private final ArrayStack<Flow> current_begin = new ArrayStack<Flow>(); /*
                                                                              * stack des points d'entree des structure
                                                                              * de control
                                                                              */
      private final ArrayStack<Flow> current_end = new ArrayStack<Flow>(); /*
                                                                            * stack des points de sortie des structure
                                                                            * de control. Au debut de developpement, il
                                                                            * existe seulement le current_exit, car on a
                                                                            * toujours besoin d'un exit, mais pourquoi
                                                                            * un entry ? le loop peut etre fait avec les
                                                                            * saved_cursor. Mais pour simplifier la
                                                                            * logique, je decide d'avoir aussi le
                                                                            * current_begin, c'est plus facile a
                                                                            * comprendre
                                                                            */
      private final ArrayList<Flow> flows = new ArrayList<Flow>(); // structure de l'arbre utilise pour l'affichage

      private MethodData(final String return_type, final String name) {
        this.return_type = return_type;
        this.name = name;
        this.current_begin.push(this.current_cursor);
        this.current_end.push(new Flow("end", "exit"));
        this.flows.add(this.current_cursor);
      }

      public MethodData addFlow(final String type, final String name) {
        final Flow next_flow = new Flow(type, name);
        jumpTo(next_flow);
        this.flows.add(next_flow);
        log(new Throwable().getStackTrace()[0].getMethodName() + " " + name);
        return this;
      }

      public MethodData saveCursor() {
        this.saved_cursors.push(this.current_cursor);
        log(new Throwable().getStackTrace()[0].getMethodName() + " " + this.current_cursor.name);
        return this;
      }

      public MethodData loadCursor() {
        this.current_cursor = this.saved_cursors.pop();
        log(new Throwable().getStackTrace()[0].getMethodName() + " " + this.current_cursor.name);
        return this;
      }

      public MethodData begin(final String type) {
        String erasure_type;

        switch (type) {
          case "while":
          case "do":
          case "for":
            erasure_type = "loop";
            break;
          default:
            if (type.contains("case"))
              erasure_type = "condition";
            else
              erasure_type = type;
        }

        addFlow(erasure_type, type + "Begin");
        final Flow new_internal_end = new Flow(erasure_type, type + "End");
        this.current_begin.push(this.current_cursor);
        this.current_end.push(new_internal_end);
        this.flows.add(new_internal_end);
        log(new Throwable().getStackTrace()[0].getMethodName() + " " + type);
        return this;
      }

      public MethodData loop() {
        for (int i = 0; i < this.current_begin.size(); i++) {
          if (this.current_begin.asStack().get(i).type.equals("loop")) {
            blockFlowThenLinkTo(this.current_begin.asStack().get(i));
            break;
          }
        }
        log(new Throwable().getStackTrace()[0].getMethodName());
        return this;
      }

      public MethodData end() {
        jumpTo(this.current_end.peek());
        log(new Throwable().getStackTrace()[0].getMethodName());
        return this;
      }

      public MethodData end(final String type) {
        switch (type) {
          case "return":
            blockFlowThenLinkTo(this.current_end.get(0));
            break;
          case "break":
            for (int i = 0; i < this.current_end.size(); i++) {
              if (this.current_end.asStack().get(i).type.equals("loop")
                  || this.current_end.asStack().get(i).type.equals("switch")) {
                blockFlowThenLinkTo(this.current_end.asStack().get(i));
                break;
              }
            }
            break;
          default:
        }
        log(new Throwable().getStackTrace()[0].getMethodName());
        return this;
      }

      /*
       * on a fini de parsing le structure de controle, on enleve les points d'entree
       * et sortie
       */
      public void exit() {
        this.current_begin.pop();
        this.current_end.pop();
        log(new Throwable().getStackTrace()[0].getMethodName());
      }

      private void blockFlowThenLinkTo(final Flow flow) {
        final Flow dead_flow = this.current_cursor;
        jumpTo(flow);
        this.current_cursor = dead_flow;
        this.current_cursor.alive = false;
      }

      private void jumpTo(final Flow next_flow) {
        if (this.current_cursor.alive)
          this.current_cursor.nexts.add(next_flow);
        this.current_cursor = next_flow;
      }

      public String toString() {
        final String s = this.name + "(): " + this.return_type + "|";
        return s;
      }
    }

    public String type;
    public String name;
    public String extnds;
    public HashSet<String> implments = new HashSet<String>();
    public ArrayStack<FieldData> fields = new ArrayStack<FieldData>();
    public ArrayStack<MethodData> methods = new ArrayStack<MethodData>();

    public void declare(final String type, final String name) {
      try {
        if (type != null) {
          fields.push(new FieldData(type, name));
        } else if (name != null) {
          if (fields.peek().name == null) {
            fields.peek().name = name;
          } else {
            throw new Exception("Try to declare field without type");
          }
        }
      } catch (final Exception e) {
        e.printStackTrace();
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

  // structure de node pour l'affichage UML
  private class DotNodeUML extends ClassData {

    private boolean is_place_holder = false;
    private HashSet<DotNodeUML> froms = new HashSet<DotNodeUML>();
    private final HashSet<DotNodeUML> tos = new HashSet<DotNodeUML>();
    private final HashSet<String> to_labels = new HashSet<String>();

    private DotNodeUML(final ClassData class_data) {
      this.type = class_data.type;
      this.name = class_data.name;
      this.fields = class_data.fields;
      this.methods = class_data.methods;
      this.to_labels.addAll(class_data.implments);
      if (class_data.extnds != null)
        this.to_labels.add(class_data.extnds);
    }

    private DotNodeUML(final String name) {
      this.name = name;
      this.is_place_holder = true;
    }

    public String toString() {
      String s = this.type + ": " + this.name + "|";
      for (int i = 0; i < this.fields.size(); i++) {
        s += "+ " + this.fields.get(i).name + " : " + this.fields.get(i).type + "\\l";
      }
      s += "|";
      for (int i = 0; i < this.methods.size(); i++) {
        s += "+ " + this.methods.get(i).name + "() : " + this.methods.get(i).return_type + "\\l";
      }
      return s;
    }
  }

  // structure de node pour l'affichage CFG
  private class DotNodeCFG extends ClassData {
    private DotNodeCFG(final ClassData class_data) {
      super.type = class_data.type;
      super.name = class_data.name;
      super.fields = class_data.fields;
      super.methods = class_data.methods;
    }
  }

  // Le queue des structures de donnee des class
  private final ArrayStack<ClassData> class_database = new ArrayStack<ClassData>();
  private final HashMap<String, DotNodeUML> dot_tree_uml = new HashMap<String, DotNodeUML>();
  private final HashMap<String, DotNodeCFG> dot_tree_cfg = new HashMap<String, DotNodeCFG>();

  public static Zeus singleton = new Zeus();

  public ClassData declareClass() {
    this.class_database.push(new ClassData());
    return this.class_database.peek();
  }

  public ClassData connectClassDatabase() {
    return this.class_database.peek();
  }

  // quand on fini la declaration on sync sur la structure de .dot
  public void disconnectClassDatabase() {
    if (FEATURE_FLAG_UML)
      updateUML();
    if (FEATURE_FLAG_CFG)
      udpateCFG();
    this.class_database.pop();
  }

  private void updateUML() {
    final DotNodeUML node = new DotNodeUML(connectClassDatabase());
    final DotNodeUML place_holder = this.dot_tree_uml.put(node.name, node);
    if (place_holder != null)
      node.froms = place_holder.froms;

    for (final String to_label : node.to_labels) {
      if (this.dot_tree_uml.get(to_label) == null)
        this.dot_tree_uml.put(to_label, new DotNodeUML(to_label));

      node.tos.add(dot_tree_uml.get(to_label));
      this.dot_tree_uml.get(to_label).froms.add(node);
    }
  }

  private void udpateCFG() {
    final DotNodeCFG node = new DotNodeCFG(connectClassDatabase());
    this.dot_tree_cfg.put(node.name, node);
  }

  // la fonction done qui est appele dans le parser, une fois il termine tous les
  // parsings
  public void done() {
    if (FEATURE_FLAG_UML)
      (new DotTreeWriter()).saveAsUML(this.dot_tree_uml);
    if (FEATURE_FLAG_CFG)
      (new DotTreeWriter()).saveAsCFG(this.dot_tree_cfg);
  }

  private void log(String s) {
    if (LOG) {
      System.out.println(s);
      new Exception("Logging").printStackTrace();
    }
  }

  private class DotTreeWriter extends DotWriter {

    private void saveAsUML(final HashMap<String, DotNodeUML> dot_tree_uml) {
      for (final DotNodeUML dot_node : dot_tree_uml.values()) {
        if (!(dot_node).is_place_holder) {
          saveNode(dot_node, () -> {
            for (final DotNodeUML from : dot_node.froms) {
              super.writeln(from.name + " -> " + dot_node.name);
              super.newLine();
              super.writeLabel(from.name, from.toString());
            }
            for (final DotNodeUML to : dot_node.tos) {
              if (!to.is_place_holder) {
                super.writeln(dot_node.name + " -> " + to.name);
                super.newLine();
                super.writeLabel(to.name, to.toString());
              }
            }
            super.writeLabel(dot_node.name, dot_node.toString());
            return null;
          });
        }
      }
    }

    private void saveAsCFG(final HashMap<String, DotNodeCFG> dot_tree_cfg) {
      for (final DotNodeCFG dot_node : dot_tree_cfg.values()) {
        saveNode(dot_node, () -> {
          for (int i = 0; i < dot_node.methods.size(); i++) {
            super.writeln(dot_node.methods.get(i).id + " -> " + dot_node.methods.get(i).flows.get(0).id);
            super.writeLabel(dot_node.methods.get(i).id, dot_node.methods.get(i).toString());
            writeLabel(dot_node.methods.get(i).flows.get(0).id, dot_node.methods.get(i).flows.get(0).name,
                dot_node.methods.get(i).flows.get(0).type);
            for (int j = 0; j < dot_node.methods.get(i).flows.size(); j++) {
              for (int k = 0; k < dot_node.methods.get(i).flows.get(j).nexts.size(); k++) {
                super.writeln(dot_node.methods.get(i).flows.get(j).id + " -> "
                    + dot_node.methods.get(i).flows.get(j).nexts.get(k).id);
                writeLabel(dot_node.methods.get(i).flows.get(j).id, dot_node.methods.get(i).flows.get(j).name,
                    dot_node.methods.get(i).flows.get(j).type);
                writeLabel(dot_node.methods.get(i).flows.get(j).nexts.get(k).id,
                    dot_node.methods.get(i).flows.get(j).nexts.get(k).name,
                    dot_node.methods.get(i).flows.get(j).nexts.get(k).type);
              }
            }
          }
          return null;
        });
      }
    }

    private void writeLabel(final String id, final String label, final String type) {
      switch (type) {
        case "begin":
        case "end":
          super.begin(id + " [");
          super.writeln("label = \"" + label + "\"");
          super.writeln("shape = \"" + "doublecircle" + "\"");
          super.end("]");
          break;
        case "condition":
          super.begin(id + " [");
          super.writeln("label = \"" + label + "\"");
          super.writeln("shape = \"" + "diamond" + "\"");
          super.end("]");
          break;
        case "loop":
          super.begin(id + " [");
          super.writeln("label = \"" + label + "\"");
          super.writeln("shape = \"" + "ellipse" + "\"");
          super.end("]");
          break;
        case "label":
          super.begin(id + " [");
          super.writeln("label = \"" + label + "\"");
          super.writeln("shape = \"" + "none" + "\"");
          super.end("]");
          break;
        case "switch":
          super.begin(id + " [");
          super.writeln("label = \"" + label + "\"");
          super.writeln("shape = \"" + "tripleoctagon" + "\"");
          super.end("]");
          break;
        default:
          super.begin(id + " [");
          super.writeln("label = \"{" + label + "}\"");
          super.end("]");
      }
    }

    private void saveNode(final ClassData dot_node, final java.util.function.Supplier<Object> writeBody) {
      createDotFile(dot_node);
      super.writeHeader();
      writeBody.get();
      super.end("}");
      try {
        super.output_stream_uml_dot_file.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    private void createDotFile(final ClassData dot_node) {
      final String type = (dot_node instanceof DotNodeUML) ? "uml" : (dot_node instanceof DotNodeCFG) ? "cfg" : "";
      try {
        final File uml_dot_file = new File("./results/" + type + "/dot/" + dot_node.name + ".dot");
        uml_dot_file.getParentFile().mkdirs();
        uml_dot_file.createNewFile(); // if file already exists will do nothing
        super.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

  }

}