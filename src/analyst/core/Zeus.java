package analyst.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

      private final String type;
      private String name;

      private FieldData(final String type, final String name) {
        this.type = type;
        this.name = name;
      }

      private void declareName(final String name) {
        try {
          if (this.name == null) {
            this.name = name;
          } else {
            throw new Exception("Try to declare field without type");
          }
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    }

    class MethodData {

      private int definition_id = 0;

      private String genDefinitionID() {
        int temp = this.definition_id;
        this.definition_id++;
        return "d" + temp;
      }

      private class Flow {
        private final String id = UUID.get();
        private final String type; // condition, entry, end,...etc
        private final String name;
        private boolean alive = true;
        private final HashMap<String, Flow> predecessors = new HashMap<String, Flow>();
        private final HashMap<String, Flow> successors = new HashMap<String, Flow>();
        private Flow immediate_dominator;
        private Flow immediate_post_dominator;
        private final HashMap<String, String> transition = new HashMap<String, String>();
        private final ArrayStack<Definition> gen = new ArrayStack<Definition>();
        private final ArrayList<Definition> kill = new ArrayList<Definition>();
        private final ArrayList<Definition> in = new ArrayList<Definition>();
        private final ArrayList<Definition> out = new ArrayList<Definition>();

        private Flow(final String type, final String name) {
          this.type = type;
          this.name = name;
        }

        public String toString() {
          String s = "{flow " + this.id + ": " + this.name + "|";
          if (this.gen.isEmpty()) {
            s += "GEN = [ ]\\l";
          } else {
            for (Definition def : this.gen) {
              s += "GEN " + def.id + ": " + def.variable + "=" + def.expression + "\\l";
            }
          }
          s = s.replace("<", "\\<").replace(">", "\\>");
          s += "|";
          s += "KILL = [ ";
          for (Definition def : this.kill) {
            s += def.id + " ";
          }
          s += " ]\\l";
          s += "|";
          s += "IN = [ ";
          for (Definition def : this.in) {
            s += def.id + " ";
          }
          s += " ]\\l";
          s += "|";
          s += "OUT = [ ";
          for (Definition def : this.out) {
            s += def.id + " ";
          }
          s += " ]\\l";
          s += "|";
          s += "Immediate dominator: " + "flow " + this.immediate_dominator.id + "\\l";
          s += "|";
          s += "Immediate post-dominator: " + "flow " + this.immediate_post_dominator.id + "\\l}";
          return s;
        }
      }

      private class Definition {
        private final String id = genDefinitionID();
        private final String variable;
        private String expression;

        private Definition(final String variable) {
          this.variable = variable;
        }

        private void assignExpression(final String expression) {
          try {
            if (this.expression == null) {
              this.expression = expression;
            } else {
              throw new Exception("Assign expression to no variable");
            }
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }

      private final String id = UUID.get();
      private final String return_type;
      private final String name;

      private Flow current_cursor;
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
      private final HashMap<String, Flow> flows = new HashMap<String, Flow>(); // structure de l'arbre utilise pour
                                                                               // l'affichage
      private final HashMap<String, ArrayList<Definition>> definitions = new HashMap<String, ArrayList<Definition>>();

      private MethodData(final String return_type, final String name) {
        this.return_type = return_type;
        this.name = name;
        begin("entry", "");
      }

      public void genVar(final String variable) {
        Definition new_definition = new Definition(variable);
        this.current_cursor.gen.push(new_definition);
        if (!definitions.containsKey(new_definition.variable))
          definitions.put(new_definition.variable, new ArrayList<Definition>());
        definitions.get(new_definition.variable).add(new_definition);
      }

      public void genExpression(final String expression) {
        this.current_cursor.gen.peek().assignExpression(expression);
      }

      public MethodData addFlow(final String type, final String name) {
        final Flow next_flow = new Flow(type, name);
        jumpTo(next_flow);
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

      public MethodData begin(final String type, final String info) {

        final Flow new_begin;
        final Flow new_end;

        switch (type) {
          case "entry":
            new_begin = new Flow("begin", "entry");
            new_end = new Flow("end", "exit");
            break;
          case "if":
            new_begin = new Flow("condition", "if " + info);
            new_end = new Flow("end", "ifEnd");
            break;
          case "while":
            new_begin = new Flow("loop", "while " + info);
            new_end = new Flow("end", "whileEnd");
            break;
          case "do":
            new_begin = new Flow("loop", "doBegin");
            new_end = new Flow("end", "doEnd");
            break;
          case "for":
            new_begin = new Flow("loop", "forControl");
            new_end = new Flow("end", "forEnd");
            break;
          case "switch":
            new_begin = new Flow("condition", "switch " + info);
            new_end = new Flow("end", "switchEnd");
            break;
          case "case":
            if (this.current_cursor.type.equals("case")) {
              new_begin = new Flow("case", this.current_cursor.name + ", " + "case " + info);
              new_end = new Flow("end", this.current_cursor.name + ", " + "case " + info + " end");
              this.current_begin.pop();
              this.current_end.pop();
            } else {
              new_begin = new Flow("case", "case " + info);
              new_end = new Flow("end", "case " + info + " end");
            }
            break;
          default:
            new_begin = new Flow(type, type + " " + info + "Begin");
            new_end = new Flow(type, type + " " + info + "End");
        }

        jumpTo(new_begin);
        this.current_begin.push(this.current_cursor);
        this.current_end.push(new_end);
        this.flows.put(new_end.id, new_end);
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
        log(new Throwable().getStackTrace()[0].getMethodName() + " " + this.current_end.peek().name);
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
        final Flow entry = this.current_begin.pop();
        final Flow log_data = this.current_end.pop();
        if (this.current_begin.isEmpty()) {
          this.current_cursor = entry;
          computeKill();
          computeInOut();
          computeDominator();
          computePostDominator();
        }
        log(new Throwable().getStackTrace()[0].getMethodName() + " " + log_data.name);
      }

      private void computeKill() {
        for (Flow flow : this.flows.values()) {
          for (Definition definition : flow.gen) {
            for (Definition being_killed : this.definitions.get(definition.variable)) {
              if (!being_killed.id.equals(definition.id)) {
                flow.kill.add(being_killed);
              }
            }
          }
        }
      }

      private void computeInOut() {
        boolean any_out_changed = true;
        while (any_out_changed) {
          any_out_changed = false;
          for (Flow flow : this.flows.values()) {
            if (!flow.name.equals("entry")) {
              for (Flow predecessor : flow.predecessors.values()) {
                flow.in.addAll(predecessor.out);
              }
              ArrayList<Definition> old_out = flow.out;
              flow.out.addAll(flow.gen);
              ArrayList<Definition> in_minus_kill = new ArrayList<Definition>();
              for (Definition in : flow.in) {
                if (!flow.kill.contains(in)) {
                  in_minus_kill.add(in);
                }
              }
              flow.out.addAll(in_minus_kill);
              if (!old_out.equals(flow.out))
                any_out_changed = true;
            }
          }
        }
      }

      private void computeDominator() {
        HashMap<String, ArrayStack<Flow>> dominators = new HashMap<String, ArrayStack<Flow>>();
        this.current_cursor.immediate_dominator = this.current_cursor;
        dominators.put(this.current_cursor.id, new ArrayStack<Flow>());
        dominators.get(this.current_cursor.id).push(this.current_cursor);
        for (Flow flow : this.flows.values()) {
          if (!flow.equals(this.current_cursor)) {
            dominators.put(flow.id, new ArrayStack<Flow>());
            for (Flow fl : this.flows.values()) {
              dominators.get(flow.id).add(fl);
            }
          }
        }
        ArrayStack<Flow> working_list = new ArrayStack<Flow>();
        for (Flow successor : this.current_cursor.successors.values()) {
          working_list.push(successor);
        }
        while (!working_list.isEmpty()) {
          Flow working_flow = working_list.pop();
          ArrayStack<Flow> intersect_dominators = new ArrayStack<Flow>();
          for (Flow predecessor : working_flow.predecessors.values()) {
            if (intersect_dominators.isEmpty())
              intersect_dominators.addAll(dominators.get(predecessor.id));
            else {
              ArrayStack<Flow> new_intersect_dominators = new ArrayStack<Flow>();
              for (Flow flow : dominators.get(predecessor.id)) {
                if (intersect_dominators.contains(flow))
                  new_intersect_dominators.add(flow);
              }
              intersect_dominators = new_intersect_dominators;
            }
          }
          intersect_dominators.add(working_flow);
          ArrayList<Flow> old_dominators = dominators.put(working_flow.id, intersect_dominators);
          if (!old_dominators.equals(dominators.get(working_flow.id))) {
            for (Flow successor : working_flow.successors.values()) {
              working_list.push(successor);
            }
          }
        }
        for (Flow flow : this.flows.values()) {
          Collections.sort(dominators.get(flow.id),
              (flow1, flow2) -> computeDistance(flow1, flow) <= computeDistance(flow2, flow) ? 1 : -1);
          if (dominators.get(flow.id).size() > 1)
            dominators.get(flow.id).pop();
          flow.immediate_dominator = dominators.get(flow.id).peek();
        }
      }

      private void computePostDominator() {
        HashMap<String, ArrayStack<Flow>> post_dominators = new HashMap<String, ArrayStack<Flow>>();
        /*
         * this is to bypass compiler, we know that there will be always flow "exit" in
         * this.flows
         */
        Flow current_cursor = new Flow("smt", "smt");
        for (Flow exit : this.flows.values()) {
          if (exit.name.equals("exit")) {
            current_cursor = exit;
            break;
          }
        }
        current_cursor.immediate_post_dominator = current_cursor;
        post_dominators.put(current_cursor.id, new ArrayStack<Flow>());
        post_dominators.get(current_cursor.id).push(current_cursor);
        for (Flow flow : this.flows.values()) {
          if (!flow.equals(current_cursor)) {
            post_dominators.put(flow.id, new ArrayStack<Flow>());
            for (Flow fl : this.flows.values()) {
              post_dominators.get(flow.id).push(fl);
            }
          }
        }
        ArrayStack<Flow> working_list = new ArrayStack<Flow>();
        for (Flow predecessor : current_cursor.predecessors.values()) {
          working_list.push(predecessor);
        }
        while (!working_list.isEmpty()) {
          Flow working_flow = working_list.pop();
          ArrayStack<Flow> intersect_post_dominators = new ArrayStack<Flow>();
          for (Flow successor : working_flow.successors.values()) {
            if (intersect_post_dominators.isEmpty())
              intersect_post_dominators.addAll(post_dominators.get(successor.id));
            else {
              ArrayStack<Flow> new_intersect_post_dominators = new ArrayStack<Flow>();
              for (Flow flow : post_dominators.get(successor.id)) {
                if (intersect_post_dominators.contains(flow))
                  new_intersect_post_dominators.add(flow);
              }
              intersect_post_dominators = new_intersect_post_dominators;
            }
          }
          intersect_post_dominators.add(working_flow);
          ArrayList<Flow> old_post_dominators = post_dominators.put(working_flow.id, intersect_post_dominators);
          if (!old_post_dominators.equals(post_dominators.get(working_flow.id))) {
            for (Flow predecessor : working_flow.predecessors.values()) {
              working_list.push(predecessor);
            }
          }
        }
        for (Flow flow : this.flows.values()) {
          Collections.sort(post_dominators.get(flow.id),
              (flow1, flow2) -> computeDistance(flow, flow1) <= computeDistance(flow, flow2) ? 1 : -1);
          if (flow.id.equals("30")) {
            System.out.println("Flow 30 has ");
            for (Flow post_dominator : post_dominators.get(flow.id)) {
              System.out.println(post_dominator.id);
            }
          }
          if (post_dominators.get(flow.id).size() > 1)
            post_dominators.get(flow.id).pop();
          flow.immediate_post_dominator = post_dominators.get(flow.id).peek();
        }
      }

      private int computeDistance(Flow from, Flow to) {
        HashMap<String, Flow> argument_type_adapter = new HashMap<String, Flow>();
        argument_type_adapter.put(from.id, from);
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        return __recursive_task__(argument_type_adapter, to, visited);
      }

      private int __recursive_task__(HashMap<String, Flow> successors, Flow to, HashMap<String, Boolean> visited) {
        HashMap<String, Flow> next_level_successors = new HashMap<String, Flow>();
        for (Flow successor : successors.values()) {
          if (successor.equals(to))
            return 0;
          for (Flow sub_successor : successor.successors.values()) {
            if (visited.get(sub_successor.id) == null) {
              next_level_successors.put(sub_successor.id, sub_successor);
              visited.put(sub_successor.id, true);
            }
          }
        }
        if (next_level_successors.isEmpty())
          return 9999999;
        return 1 + __recursive_task__(next_level_successors, to, visited);
      }

      private void blockFlowThenLinkTo(final Flow flow) {
        final Flow dead_flow = this.current_cursor;
        jumpTo(flow);
        this.current_cursor = dead_flow;
        this.current_cursor.alive = false;
      }

      private void jumpTo(final Flow next_flow) {

        if (this.current_cursor != null && this.current_cursor.alive) {

          if (this.current_cursor.type.equals("label")) {
            swapFlow(this.current_cursor, next_flow, (predecessor) -> {
              predecessor.transition.put(next_flow.id, this.current_cursor.name);
            });
          }

          else if ((this.current_cursor.type.equals("case") && next_flow.type.equals("case"))
              || this.current_cursor.type.equals("end")) {
            swapFlow(this.current_cursor, next_flow, null);
          }

          else {
            this.current_cursor.successors.put(next_flow.id, next_flow);
            next_flow.predecessors.put(this.current_cursor.id, this.current_cursor);
          }
        }
        this.current_cursor = next_flow;
        if (!this.flows.containsKey(next_flow.id))
          this.flows.put(next_flow.id, next_flow);
      }

      private void swapFlow(final Flow old_flow, final Flow new_flow,
          final java.util.function.Consumer<Flow> predecessors_side_effect_handler) {
        new_flow.predecessors.putAll(old_flow.predecessors);
        for (final Flow predecessor : old_flow.predecessors.values()) {
          predecessor.successors.remove(old_flow.id);
          predecessor.successors.put(new_flow.id, new_flow);
          predecessor.transition.put(new_flow.id, predecessor.transition.get(old_flow.id));
          predecessor.transition.remove(old_flow.id);
          if (predecessors_side_effect_handler != null)
            predecessors_side_effect_handler.accept(predecessor);
        }
        new_flow.successors.putAll(old_flow.successors);
        for (final Flow flow : old_flow.successors.values()) {
          flow.predecessors.remove(old_flow.id);
          flow.predecessors.put(new_flow.id, new_flow);
        }
        new_flow.transition.putAll(old_flow.transition);
        this.flows.remove(old_flow.id);
      }

      public String toString() {
        final String s = this.name + "(): " + this.return_type;
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
      if (type != null) {
        fields.push(new FieldData(type, name));
      } else if (name != null) {
        fields.peek().declareName(name);
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

  private void log(final String s) {
    if (LOG) {
      try {
        final File file = new File("./log");
        file.createNewFile();
        final FileOutputStream fos = new FileOutputStream(file, true);
        final PrintStream ps = new PrintStream(fos);
        fos.write((s + "\n").getBytes());
        new Exception("Logging").printStackTrace(ps);
        fos.close();
        ps.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
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
            super.writeln(dot_node.methods.get(i).id + " -> " + dot_node.methods.get(i).current_cursor.id);
            super.writeLabel(dot_node.methods.get(i).id, dot_node.methods.get(i).toString());
            writeLabel(dot_node.methods.get(i).current_cursor.id, dot_node.methods.get(i).current_cursor.toString(),
                dot_node.methods.get(i).current_cursor.type);
            for (final String key : dot_node.methods.get(i).flows.keySet()) {
              for (final String k : dot_node.methods.get(i).flows.get(key).successors.keySet()) {
                String transition_label = dot_node.methods.get(i).flows.get(key).transition
                    .get(dot_node.methods.get(i).flows.get(key).successors.get(k).id);
                transition_label = transition_label != null ? " [label=\"" + transition_label + "\"]" : "";
                super.writeln(dot_node.methods.get(i).flows.get(key).id + " -> "
                    + dot_node.methods.get(i).flows.get(key).successors.get(k).id + transition_label);
                writeLabel(dot_node.methods.get(i).flows.get(key).id, dot_node.methods.get(i).flows.get(key).toString(),
                    dot_node.methods.get(i).flows.get(key).type);
                writeLabel(dot_node.methods.get(i).flows.get(key).successors.get(k).id,
                    dot_node.methods.get(i).flows.get(key).successors.get(k).toString(),
                    dot_node.methods.get(i).flows.get(key).successors.get(k).type);
              }
            }
          }
          return null;
        });
      }
    }

    private void writeLabel(final String id, final String label, final String type) {
      switch (type) {
        /*
         * Styling problem, currently cannot have record-based shape with polygon-based
         * shape, so we can only have record-based for all flow type
         */
        // case "begin":
        // case "end":
        // super.begin(id + " [");
        // super.writeln("label = \"" + label + "\"");
        // super.writeln("shape = \"" + "doublecircle" + "\"");
        // super.end("]");
        // break;
        // case "condition":
        // case "case":
        // super.begin(id + " [");
        // super.writeln("label = \"" + label + "\"");
        // super.writeln("shape = \"" + "diamond" + "\"");
        // super.end("]");
        // break;
        // case "loop":
        // if (!label.equals("doBegin")) {
        // super.begin(id + " [");
        // super.writeln("label = \"" + label + "\"");
        // super.writeln("shape = \"" + "Mdiamond" + "\"");
        // super.end("]");
        // } else {
        // super.begin(id + " [");
        // super.writeln("label = \"" + label + "\"");
        // super.writeln("shape = \"" + "ellipse" + "\"");
        // super.end("]");
        // }
        // break;
        default:
          super.begin(id + " [");
          super.writeln("label = \"" + label + "\"");
          super.writeln("shape = \"" + "record" + "\"");
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