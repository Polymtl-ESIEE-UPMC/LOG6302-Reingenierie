package javaparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Zeus {

  private final boolean FEATURE_FLAG_UML = false;
  private final boolean FEATURE_FLAG_CFG = true;

  private static int __id__ = -1;

  private int genID() {
    __id__++;
    return __id__;
  }

  // Structure de donnee de la declaration des class
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

    class MethodData {

      private class Flow {
        private final int id = genID();
        private String type; // condition, entry, end,...etc
        private String name;
        private String target; // details pour plus tard
        private boolean alive = true;;
        private ArrayList<Flow> next = new ArrayList<Flow>();

        private Flow(String type, String name) {
          this.type = type;
          this.name = name;
        }
      }

      private final String return_type;
      private final String name;
      private final int id = genID();
      private ArrayList<FieldData> entries = new ArrayList<FieldData>(); // les INs de methode, pas encore vraiment
                                                                         // utilise
      public String inh; // inh attribute

      private ArrayList<Flow> flows = new ArrayList<Flow>(); // structure de l'arbre utilise pour l'affichage
      private Flow current_cursor = new Flow("entry", "entry");
      private LinkedList<Flow> saved_cursors = new LinkedList<Flow>();
      private LinkedList<Flow> current_begin = new LinkedList<Flow>(); // queue des points d'entree des structure de
                                                                       // control
      private LinkedList<Flow> current_end = new LinkedList<Flow>(); // queue des points de sortie des structure de
                                                                     // sortie
      // au debut de developpement, car on a toujours besoin d'un exit, mais pourquoi
      // un entry ? le loop peut etre fait avec
      // les saved_cursor. Puis pour simplifier, je decide d'avoir aussi le
      // current_begin, c'est plus facie a comprendre

      private MethodData(final String return_type, final String name) {
        this.return_type = return_type;
        this.name = name;
        this.flows.add(this.current_cursor);
        this.current_begin.addFirst(this.current_cursor);
        this.current_end.addFirst(new Flow("end", "end"));
      }

      public void addEntry(String name) {
        entries.add(new FieldData(this.inh, name));
      }

      public MethodData saveCursor() {
        this.saved_cursors.addFirst(this.current_cursor);
        return this;
      }

      public MethodData loadCursor() {
        this.current_cursor = this.saved_cursors.pollFirst();
        return this;
      }

      public MethodData enter(String type) {
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
        Flow new_internal_end = new Flow(erasure_type, type + "End");
        this.flows.add(new_internal_end);
        this.current_begin.addFirst(this.current_cursor);
        this.current_end.addFirst(new_internal_end);
        return this;
      }

      public MethodData loop() {
        for (int i = 0; i < this.current_begin.size(); i++) {
          if (this.current_begin.get(i).type.equals("loop")) {
            blockFlowThenLinkTo(this.current_begin.get(i));
            break;
          }
        }
        return this;
      }

      public MethodData end() {
        jumpTo(this.current_end.getFirst());
        return this;
      }

      public MethodData end(String type) {
        switch (type) {
          case "return":
            blockFlowThenLinkTo(this.current_end.getLast());
            break;
          case "break":
            for (int i = 0; i < this.current_end.size(); i++) {
              if (this.current_end.get(i).type.equals("loop") || this.current_end.get(i).type.equals("switch")) {
                blockFlowThenLinkTo(this.current_end.get(i));
                break;
              }
            }
            break;
          default:
        }
        return this;
      }

      private void blockFlowThenLinkTo(Flow flow) {
        Flow dead_flow = this.current_cursor;
        jumpTo(flow);
        this.current_cursor = dead_flow;
        this.current_cursor.alive = false;
      }

      // on a fini de parsing le structure de controle, on enleve les points d'entree
      // et sortie
      public void exit() {
        this.current_begin.removeFirst();
        this.current_end.removeFirst();
      }

      public MethodData addFlow(String type, String name) {
        Flow next_flow = new Flow(type, name);
        this.flows.add(next_flow);
        jumpTo(next_flow);
        return this;
      }

      private void jumpTo(Flow next_flow) {
        if (this.current_cursor.alive)
          this.current_cursor.next.add(next_flow);
        this.current_cursor = next_flow;
      }

      public String toString() {
        String s = this.name + "(): " + this.return_type + "|";
        for (int i = 0; i < this.entries.size(); i++) {
          s += "+ " + this.entries.get(i).name + " : " + this.entries.get(i).type + "\\l";
        }
        return s;
      }
    }

    public String type;
    public String name;
    public String extnds;
    public ArrayList<String> implments = new ArrayList<String>();
    public FieldData inh = new FieldData();
    public ArrayList<FieldData> fields = new ArrayList<FieldData>();
    public ArrayList<MethodData> methods = new ArrayList<MethodData>();

    public void addField() {
      fields.add(new FieldData(this.inh.type, this.inh.name));
    }

    public void addMethod() {
      methods.add(new MethodData(this.inh.type, this.inh.name));
    }

    public MethodData connectMethod() {
      return methods.get(methods.size() - 1);
    }
  }

  // structure de node pour l'affichage UML
  private class DotNodeUML extends ClassData {

    private boolean is_place_holder = false;
    private ArrayList<DotNodeUML> from = new ArrayList<DotNodeUML>();
    private final ArrayList<DotNodeUML> to = new ArrayList<DotNodeUML>();
    private final ArrayList<String> to_label = new ArrayList<String>();

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

  // Le queue des structures de donnee des class. Pourquoi pas de LinkedList ? Car
  // au debut je ne voulais pas
  // vraiment enlever la class, il faut le conserver pour l'affichage a la fin. En
  // effet, avec une structure
  // dedie pour l'affichage qui est synchronise, ce mechanisme n'est plus
  // necessaire, mais il existe encore
  private class ClassDatabase {
    private final HashMap<ClassDeclaration, ClassData> __core__ = new HashMap<ClassDeclaration, ClassData>();
    private final LinkedList<SimpleNode> sessions = new LinkedList<SimpleNode>();
  }

  private final ClassDatabase class_database = new ClassDatabase();
  private final HashMap<String, DotNodeUML> dot_tree_uml = new HashMap<String, DotNodeUML>();
  private final HashMap<String, DotNodeCFG> dot_tree_cfg = new HashMap<String, DotNodeCFG>();

  private static Zeus zeus = new Zeus();

  public static Zeus getSingleton() {
    return zeus;
  }

  public ClassData connectClassDatabase(final ClassDeclaration node) {
    if (class_database.__core__.get(node) == null)
      class_database.__core__.put(node, new ClassData());
    class_database.sessions.addFirst(node);
    return class_database.__core__.get(node);
  }

  public ClassData connectClassDatabase() {
    if (class_database.sessions.getFirst() instanceof ClassDeclaration) {
      return connectClassDatabase((ClassDeclaration) class_database.sessions.pollFirst());
    } else {
      try {
        throw new Exception("ClassDatabase can only be connected from ClassDeclaration node");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  // quand on fini la declaration on sync sur la structure de .dot
  public void disconnectClassDatabase() {
    if (FEATURE_FLAG_UML)
      updateUML();
    if (FEATURE_FLAG_CFG)
      udpateCFG();
    class_database.sessions.removeFirst();
  }

  private void updateUML() {
    final DotNodeUML node = new DotNodeUML(connectClassDatabase());
    final DotNodeUML place_holder = dot_tree_uml.put(node.name, node);
    if (place_holder != null)
      node.from = place_holder.from;

    for (int i = 0; i < node.to_label.size(); i++) {

      if (dot_tree_uml.get(node.to_label.get(i)) == null)
        dot_tree_uml.put(node.to_label.get(i), new DotNodeUML(node.to_label.get(i)));

      node.to.add(dot_tree_uml.get(node.to_label.get(i)));
      dot_tree_uml.get(node.to_label.get(i)).from.add(node);
    }
  }

  private void udpateCFG() {
    DotNodeCFG node = new DotNodeCFG(connectClassDatabase());
    dot_tree_cfg.put(node.name, node);
  }

  // la fonction done qui est appele dans le parser, une fois il termine tous les
  // parsings
  public void done() {
    if (FEATURE_FLAG_UML)
      (new DotTreeProcessor()).saveAsUML(dot_tree_uml);
    if (FEATURE_FLAG_CFG)
      (new DotTreeProcessor()).saveAsCFG(dot_tree_cfg);
  }

  private class DotTreeProcessor {

    private FileOutputStream output_stream_uml_dot_file;
    private int indent = 0;
    private boolean new_line = true;

    private void saveAsUML(HashMap<String, DotNodeUML> dot_tree_uml) {
      for (final DotNodeUML dot_node : dot_tree_uml.values()) {
        if (!(dot_node).is_place_holder) {
          saveNode(dot_node, () -> {
            for (int i = 0; i < dot_node.from.size(); i++) {
              writeln(dot_node.from.get(i).name + " -> " + dot_node.name);
              newLine();
              writeLabel(dot_node.from.get(i).name, dot_node.from.get(i).toString());
            }
            for (int i = 0; i < dot_node.to.size(); i++) {
              if (!dot_node.to.get(i).is_place_holder) {
                writeln(dot_node.name + " -> " + dot_node.to.get(i).name);
                newLine();
                writeLabel(dot_node.to.get(i).name, dot_node.to.get(i).toString());
              }
            }
            writeLabel(dot_node.name, dot_node.toString());
            return null;
          });
        }
      }
    }

    private void saveAsCFG(HashMap<String, DotNodeCFG> dot_tree_cfg) {
      for (final DotNodeCFG dot_node : dot_tree_cfg.values()) {
        saveNode(dot_node, () -> {
          for (int i = 0; i < dot_node.methods.size(); i++) {
            writeln(dot_node.methods.get(i).id + " -> " + dot_node.methods.get(i).flows.get(0).id);
            writeLabel("" + dot_node.methods.get(i).id, dot_node.methods.get(i).toString());
            writeLabel("" + dot_node.methods.get(i).flows.get(0).id, dot_node.methods.get(i).flows.get(0).name,
                dot_node.methods.get(i).flows.get(0).type);
            for (int j = 0; j < dot_node.methods.get(i).flows.size(); j++) {
              for (int k = 0; k < dot_node.methods.get(i).flows.get(j).next.size(); k++) {
                writeln(dot_node.methods.get(i).flows.get(j).id + " -> "
                    + dot_node.methods.get(i).flows.get(j).next.get(k).id);
                writeLabel("" + dot_node.methods.get(i).flows.get(j).id, dot_node.methods.get(i).flows.get(j).name,
                    dot_node.methods.get(i).flows.get(j).type);
                writeLabel("" + dot_node.methods.get(i).flows.get(j).next.get(k).id,
                    dot_node.methods.get(i).flows.get(j).next.get(k).name,
                    dot_node.methods.get(i).flows.get(j).next.get(k).type);
              }
            }
          }
          return null;
        });
      }
    }

    private void writeLabel(String id, String label) {
      begin(id + " [");
      writeln("label = \"{" + label + "}\"");
      end("]");
    }

    private void writeLabel(String id, String label, String type) {
      switch (type) {
        case "entry":
        case "end":
          begin(id + " [");
          writeln("label = \"" + label + "\"");
          writeln("shape = \"" + "doublecircle" + "\"");
          end("]");
          break;
        case "condition":
          begin(id + " [");
          writeln("label = \"" + label + "\"");
          writeln("shape = \"" + "diamond" + "\"");
          end("]");
          break;
        case "loop":
          begin(id + " [");
          writeln("label = \"" + label + "\"");
          writeln("shape = \"" + "ellipse" + "\"");
          end("]");
          break;
        case "label":
          begin(id + " [");
          writeln("label = \"" + label + "\"");
          writeln("shape = \"" + "none" + "\"");
          end("]");
          break;
        case "switch":
          begin(id + " [");
          writeln("label = \"" + label + "\"");
          writeln("shape = \"" + "tripleoctagon" + "\"");
          end("]");
          break;
        default:
          begin(id + " [");
          writeln("label = \"{" + label + "}\"");
          end("]");
      }
    }

    private void saveNode(ClassData dot_node, java.util.function.Supplier<Object> writeBody) {
      createDotFile(dot_node);
      writeHeader();
      writeBody.get();
      end("}");
      try {
        this.output_stream_uml_dot_file.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    private void createDotFile(ClassData dot_node) {
      String type = (dot_node instanceof DotNodeUML) ? "uml" : (dot_node instanceof DotNodeCFG) ? "cfg" : "";
      try {
        final File uml_dot_file = new File("./results/" + type + "/dot/" + dot_node.name + ".dot");
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

}