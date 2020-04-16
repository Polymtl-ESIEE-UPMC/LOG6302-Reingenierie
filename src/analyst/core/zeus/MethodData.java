package analyst.core.zeus;

import java.util.Collections;
import java.util.HashMap;

import analyst.helper.ArraySet;
import analyst.helper.ArrayStack;
import analyst.helper.FlowProvider;
import analyst.helper.UUID;

public class MethodData {

  private int definition_id = 0;

  private String genDefinitionID() {
    int temp = this.definition_id;
    this.definition_id++;
    return "d" + temp;
  }

  public final String id = UUID.get();
  public final String return_type;
  public final String name;

  private String current_cursor;
  private final ArrayStack<String> saved_cursors = new ArrayStack<String>();
  private final ArrayStack<String> current_begin = new ArrayStack<String>(); /*
                                                                              * stack des points d'entree des structure
                                                                              * de control
                                                                              */
  private final ArrayStack<String> current_end = new ArrayStack<String>(); /*
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
  private final HashMap<String, Flow> flow_database = new HashMap<String, Flow>();
  private final HashMap<String, ArraySet<Definition>> definitions = new HashMap<String, ArraySet<Definition>>();
  private final HashMap<String, HashMap<String, Flow>> sliceds = new HashMap<String, HashMap<String, Flow>>();

  public MethodData(final String return_type, final String name) {
    this.return_type = return_type;
    this.name = name;
    begin("entry", "");
  }

  public void genVar(final String variable) {
    Definition new_definition = new Definition(genDefinitionID(), variable, this.current_cursor);
    flow_database.get(this.current_cursor).gens.add(new_definition);
    if (!definitions.containsKey(new_definition.variable))
      definitions.put(new_definition.variable, new ArraySet<Definition>());
    definitions.get(new_definition.variable).add(new_definition);
  }

  public void genExpression(final String expression) {
    flow_database.get(this.current_cursor).gens.getLast().assignExpression(expression);
  }

  public void use(final String variable) {
    if (this.definitions.containsKey(variable))
      flow_database.get(this.current_cursor).uses.add(variable);
  }

  public MethodData addFlow(final String type, final String name) {
    final Flow next_flow = new Flow(type, name);
    jumpTo(next_flow);
    return this;
  }

  public MethodData saveCursor() {
    this.saved_cursors.push(this.current_cursor);
    return this;
  }

  public MethodData loadCursor() {
    this.current_cursor = this.saved_cursors.pop();
    return this;
  }

  /* add flow begin of control structure */
  public MethodData begin(final String type) {
    return begin(type, "");
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
        new_begin = new Flow("condition", "if");
        new_begin.info = info;
        new_end = new Flow("end", "ifEnd");
        break;
      case "while":
        new_begin = new Flow("loop", "while");
        new_begin.info = info;
        new_end = new Flow("break", "whileEnd");
        break;
      case "do":
        new_begin = new Flow("loop", "doBegin");
        new_end = new Flow("break", "doEnd");
        break;
      case "for":
        new_begin = new Flow("loop", "forControl");
        new_end = new Flow("break", "forEnd");
        break;
      case "switch":
        new_begin = new Flow("switch", "switch");
        new_begin.info = info;
        new_end = new Flow("break", "switchEnd");
        break;
      case "case":
        if (flow_database.get(this.current_cursor).type.equals("case")) {
          new_begin = new Flow("case", flow_database.get(this.current_cursor).name + ", " + "case " + info);
          new_end = new Flow("end", flow_database.get(this.current_cursor).name + ", " + "case " + info + " end");
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
    this.current_end.push(new_end.id);
    this.flow_database.put(new_end.id, new_end);
    return this;
  }

  /* mark flow to be modified later */
  public MethodData markIncomplete() {
    flow_database.get(this.current_cursor).incomplete = true;
    return this;
  }

  public MethodData modify(final String info) {
    if (flow_database.get(this.current_cursor).incomplete) {
      flow_database.get(this.current_cursor).incomplete = false;
      flow_database.get(this.current_cursor).info = info;
    } else {
      try {
        System.out.println("new value " + info);
        System.out.println(
            "modify " + flow_database.get(this.current_cursor).name + flow_database.get(this.current_cursor).info);
        Thread.sleep(5000);
        throw new Exception("Try to modify completed flow");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return this;
  }

  public MethodData loop() {
    for (int i = 0; i < this.current_begin.size(); i++) {
      if (this.flow_database.get(this.current_begin.asStack().get(i)).type.equals("loop")) {
        blockFlowThenLinkTo(this.flow_database.get(this.current_begin.asStack().get(i)));
        break;
      }
    }
    return this;
  }

  public MethodData end() {
    jumpTo(this.flow_database.get(this.current_end.peek()));
    return this;
  }

  public MethodData end(final String type) {
    switch (type) {
      case "return":
        blockFlowThenLinkTo(this.flow_database.get(this.current_end.get(0)));
        break;
      case "break":
        for (int i = 0; i < this.current_end.size(); i++) {
          if (this.flow_database.get(this.current_end.asStack().get(i)).type.equals("break")) {
            blockFlowThenLinkTo(this.flow_database.get(this.current_end.asStack().get(i)));
            break;
          }
        }
        break;
      default:
    }
    return this;
  }

  /* every flow after this flow is dead code */
  private void blockFlowThenLinkTo(final Flow flow) {
    final String dead_flow = this.current_cursor;
    jumpTo(flow);
    this.current_cursor = dead_flow;
    if (this.flow_database.get(this.current_cursor) != null)
      this.flow_database.get(this.current_cursor).alive = false;
  }

  private void jumpTo(final Flow next_flow) {

    if (!this.flow_database.containsKey(next_flow.id))
      this.flow_database.put(next_flow.id, next_flow);

    if (this.current_cursor != null && this.flow_database.get(this.current_cursor) != null
        && this.flow_database.get(this.current_cursor).alive) {

      if (this.flow_database.get(this.current_cursor).type.equals("label")) {
        fusionFlow(this.current_cursor, next_flow.id, (predecessor) -> {
          this.flow_database.get(predecessor).transition.put(next_flow.id,
              this.flow_database.get(this.current_cursor).name);
        });
      }

      else if ((this.flow_database.get(this.current_cursor).type.equals("case") && next_flow.type.equals("case"))
          || this.flow_database.get(this.current_cursor).type.equals("end")
          || this.flow_database.get(this.current_cursor).type.equals("break")) {
        fusionFlow(this.current_cursor, next_flow.id, null);
      }

      else {
        this.flow_database.get(this.current_cursor).successors.add(next_flow.id);
        this.flow_database.get(next_flow.id).predecessors.add(this.current_cursor);
      }
    }
    this.current_cursor = next_flow.id;
  }

  private void fusionFlow(final String old_flow, final String new_flow,
      final java.util.function.Consumer<String> predecessors_side_effect_handler) {
    this.flow_database.get(new_flow).predecessors.addAll(this.flow_database.get(old_flow).predecessors);
    this.flow_database.get(new_flow).predecessors.remove(old_flow);
    for (final String predecessor : this.flow_database.get(old_flow).predecessors) {
      this.flow_database.get(predecessor).successors.remove(old_flow);
      this.flow_database.get(predecessor).successors.add(new_flow);
      this.flow_database.get(predecessor).transition.put(new_flow,
          this.flow_database.get(predecessor).transition.get(old_flow));
      this.flow_database.get(predecessor).transition.remove(old_flow);
      if (predecessors_side_effect_handler != null)
        predecessors_side_effect_handler.accept(predecessor);
    }
    this.flow_database.get(new_flow).successors.addAll(this.flow_database.get(old_flow).successors);
    this.flow_database.get(new_flow).successors.remove(new_flow);
    for (final String successor : this.flow_database.get(old_flow).successors) {
      this.flow_database.get(successor).predecessors.remove(old_flow);
      this.flow_database.get(successor).predecessors.add(new_flow);
    }
    this.flow_database.get(new_flow).transition.putAll(this.flow_database.get(old_flow).transition);
    this.flow_database.remove(old_flow);
  }

  /*
   * on a fini de parsing le structure de controle, on enleve les points d'entree
   * et sortie
   */
  public void exit() {
    final String entry = this.current_begin.pop();
    this.current_end.pop();
    /* si on a fini le method, alors */
    if (this.current_begin.isEmpty()) {
      this.current_cursor = entry;
      computeKill();
      computeInOut();
      computeDominator();
      computePostDominator();
      computeControlDependant();
      computeDataDependant();
      slice();
    }
  }

  private void computeKill() {
    for (Flow flow : this.flow_database.values()) {
      for (Definition definition : flow.gens) {
        for (Definition being_killed : this.definitions.get(definition.variable)) {
          if (!being_killed.id.equals(definition.id)) {
            flow.kills.add(being_killed);
          }
        }
      }
    }
  }

  private void computeInOut() {
    boolean any_out_changed = true;
    while (any_out_changed) {
      any_out_changed = false;
      for (Flow flow : this.flow_database.values()) {
        if (!flow.name.equals("entry")) {
          flow.ins.clear();
          for (String predecessor : flow.predecessors) {
            flow.ins.addAll(this.flow_database.get(predecessor).outs);
          }
          ArraySet<Definition> old_out = flow.outs.clone();
          flow.outs.clear();
          flow.outs.addAll(flow.gens);
          for (Definition in : flow.ins) {
            if (!flow.kills.contains(in)) {
              flow.outs.add(in);
            }
          }
          if (!old_out.equals(flow.outs))
            any_out_changed = true;
        }
      }
    }
  }

  private void computeDominator() {
    HashMap<String, ArraySet<String>> dominators = new HashMap<String, ArraySet<String>>();
    this.flow_database.get(this.current_cursor).immediate_dominator = this.current_cursor;
    dominators.put(this.current_cursor, new ArraySet<String>());
    dominators.get(this.current_cursor).add(this.current_cursor);
    for (String flow : this.flow_database.keySet()) {
      if (!flow.equals(this.current_cursor)) {
        dominators.put(flow, new ArraySet<String>());
        for (String fl : this.flow_database.keySet()) {
          dominators.get(flow).add(fl);
        }
      }
    }
    ArrayStack<String> working_list = new ArrayStack<String>();
    for (String successor : this.flow_database.get(this.current_cursor).successors) {
      working_list.push(successor);
    }
    while (!working_list.isEmpty()) {
      String working_flow = working_list.pop();
      ArraySet<String> intersect_dominators = new ArraySet<String>();
      for (String predecessor : this.flow_database.get(working_flow).predecessors) {
        if (intersect_dominators.isEmpty())
          intersect_dominators.addAll(dominators.get(predecessor));
        else {
          intersect_dominators.retainAll(dominators.get(predecessor));
        }
      }
      intersect_dominators.add(working_flow);
      ArraySet<String> old_dominators = dominators.put(working_flow, intersect_dominators);
      if (!old_dominators.equals(dominators.get(working_flow))) {
        working_list.addAll(this.flow_database.get(working_flow).successors);
      }
    }
    for (String flow : this.flow_database.keySet()) {
      Collections.sort(dominators.get(flow),
          (flow1, flow2) -> computeDistance(flow1, flow) <= computeDistance(flow2, flow) ? 1
              : (computeDistance(flow1, flow) == computeDistance(flow2, flow) ? 0 : -1));
      if (dominators.get(flow).size() > 1)
        dominators.get(flow).removeLast();
      if (this.flow_database.get(flow).name.equals("exit"))
        this.flow_database.get(flow).immediate_dominator = "";
      else
        this.flow_database.get(flow).immediate_dominator = dominators.get(flow).getLast();
    }
  }

  private void computePostDominator() {
    HashMap<String, ArraySet<String>> post_dominators = new HashMap<String, ArraySet<String>>();
    /*
     * this is to bypass compiler, we know that there will be always flow "exit" in
     * this.flows
     */
    String current_cursor = "";
    for (Flow exit : this.flow_database.values()) {
      if (exit.name.equals("exit")) {
        current_cursor = exit.id;
        break;
      }
    }
    this.flow_database.get(current_cursor).immediate_post_dominator = current_cursor;
    post_dominators.put(current_cursor, new ArraySet<String>());
    post_dominators.get(current_cursor).add(current_cursor);
    for (String flow : this.flow_database.keySet()) {
      if (!flow.equals(current_cursor)) {
        post_dominators.put(flow, new ArraySet<String>());
        for (String fl : this.flow_database.keySet()) {
          post_dominators.get(flow).add(fl);
        }
      }
    }
    ArrayStack<String> working_list = new ArrayStack<String>();
    for (String predecessor : this.flow_database.get(current_cursor).predecessors) {
      working_list.push(predecessor);
    }
    while (!working_list.isEmpty()) {
      String working_flow = working_list.pop();
      ArraySet<String> intersect_post_dominators = new ArraySet<String>();
      for (String successor : this.flow_database.get(working_flow).successors) {
        if (intersect_post_dominators.isEmpty())
          intersect_post_dominators.addAll(post_dominators.get(successor));
        else {
          intersect_post_dominators.retainAll(post_dominators.get(successor));
        }
      }
      intersect_post_dominators.add(working_flow);
      ArraySet<String> old_post_dominators = post_dominators.put(working_flow, intersect_post_dominators);
      if (!old_post_dominators.equals(post_dominators.get(working_flow))) {
        working_list.addAll(this.flow_database.get(working_flow).predecessors);
      }
    }
    for (String flow : this.flow_database.keySet()) {
      Collections.sort(post_dominators.get(flow),
          (flow1, flow2) -> computeDistance(flow, flow1) <= computeDistance(flow, flow2) ? 1
              : (computeDistance(flow, flow1) == computeDistance(flow, flow2) ? 0 : -1));
      if (post_dominators.get(flow).size() > 1)
        post_dominators.get(flow).removeLast();
      if (this.flow_database.get(flow).name.equals("entry"))
        this.flow_database.get(flow).immediate_post_dominator = "";
      else
        this.flow_database.get(flow).immediate_post_dominator = post_dominators.get(flow).getLast();
      ;
    }
  }

  private int computeDistance(String from, String to) {
    return computeDistance(from, to, new HashMap<String, Boolean>());
  }

  private int computeDistance(String from, String to, HashMap<String, Boolean> visited) {
    if (visited.get(from) == null) {
      visited.put(from, true);
      if (from.equals(to))
        return 0;
      for (String successor : this.flow_database.get(from).successors) {
        int distance = computeDistance(successor, to, visited);
        if (distance < 9999999) {
          return 1 + distance;
        }
      }
    }
    return 9999999;
  }

  private void computeControlDependant() {
    for (String flow : this.flow_database.keySet()) {
      for (String fl : this.flow_database.keySet()) {
        if (!fl.equals(flow) && thereIsPathFromXtoYthatDoesntContainImmediatePostDominatorOfX(flow, fl)) {
          this.flow_database.get(flow).control_dependants.add(fl);
        }
      }
    }
    for (String flow : this.flow_database.keySet()) {
      ArraySet<String> duplicated_control_dependant = this.flow_database.get(flow).control_dependants.clone();
      for (String control_dependant : duplicated_control_dependant) {
        this.flow_database.get(flow).control_dependants
            .removeAll(this.flow_database.get(control_dependant).control_dependants);
      }
      for (String dependant : this.flow_database.get(flow).control_dependants) {
        this.flow_database.get(dependant).controllers.add(flow);
      }
    }
  }

  private boolean thereIsPathFromXtoYthatDoesntContainImmediatePostDominatorOfX(String x, String y) {
    return __recursive_task_thereIsPathFromXtoYthatDoesntContainImmediatePostDominatorOfX__(x, y, x,
        new HashMap<String, Boolean>());
  }

  private boolean __recursive_task_thereIsPathFromXtoYthatDoesntContainImmediatePostDominatorOfX__(String x, String y,
      String original_x, HashMap<String, Boolean> visited) {
    if (!this.flow_database.get(original_x).immediate_post_dominator.equals(x) && visited.get(x) == null) {
      visited.put(x, true);
      if (x.equals(y))
        return true;
      for (String successor : this.flow_database.get(x).successors) {
        if (__recursive_task_thereIsPathFromXtoYthatDoesntContainImmediatePostDominatorOfX__(successor, y, original_x,
            visited)) {
          return true;
        }
      }
    }
    return false;
  }

  private void computeDataDependant() {
    for (Flow flow : this.flow_database.values()) {
      for (String use : flow.uses) {
        for (Definition def : flow.ins) {
          if (def.variable.equals(use)) {
            flow.data_controllers.add(def.flow);
            this.flow_database.get(def.flow).data_dependants.add(flow.id);
          }
        }
      }
    }
  }

  private void slice() {
    for (String variable : this.definitions.keySet()) {
      HashMap<String, Flow> sliced_database = new HashMap<String, Flow>();
      ArraySet<String> sliced_flows = new ArraySet<String>();
      for (Flow flow : this.flow_database.values()) {
        sliced_database.put(flow.id, flow.clone());
        if (flow.name.equals("exit"))
          sliced_flows.add(sliced_database.get(flow.id).id);
      }
      for (Definition def : this.definitions.get(variable)) {
        sliced_flows.addAll(sliced_database.get(def.flow).data_dependants);
      }
      boolean changed = true;
      while (changed) {
        changed = false;
        ArraySet<String> old_sliced_flows = sliced_flows.clone();
        for (String flow : old_sliced_flows) {
          sliced_flows.addAll(sliced_database.get(flow).data_controllers);
          sliced_flows.addAll(sliced_database.get(flow).controllers);
        }
        if (!old_sliced_flows.equals(sliced_flows))
          changed = true;
      }
      retainSlicedFlows(this.current_cursor, sliced_database, sliced_flows, new HashMap<String, Boolean>());
      this.sliceds.put(variable, sliced_database);
    }
  }

  private void retainSlicedFlows(String x, HashMap<String, Flow> sliced_database, ArraySet<String> sliced_flows,
      HashMap<String, Boolean> visited) {
    if (visited.get(x) == null) {
      visited.put(x, true);
      ArraySet<String> successors = sliced_database.get(x).successors.clone();
      if (!sliced_flows.contains(x)) {
        if (successors.isEmpty()) {
          for (final String predecessor : sliced_database.get(x).predecessors) {
            sliced_database.get(predecessor).successors.remove(x);
            sliced_database.get(predecessor).transition.remove(x);
          }
          sliced_database.remove(x);
        } else {
          String last_successor = null;
          for (String successor : successors) {
            if (last_successor != null) {
              fusionFlowInDatabase(last_successor, successor, sliced_database);
            }
            last_successor = successor;
          }
          fusionFlowInDatabase(x, last_successor, sliced_database);
          successors.clear();
          successors.add(last_successor);
        }
      }
      for (String successor : successors) {
        retainSlicedFlows(successor, sliced_database, sliced_flows, visited);
      }
    }
  }

  private void fusionFlowInDatabase(final String old_flow, final String new_flow, HashMap<String, Flow> database) {
    database.get(new_flow).predecessors.addAll(database.get(old_flow).predecessors);
    database.get(new_flow).predecessors.remove(old_flow);
    for (final String predecessor : database.get(old_flow).predecessors) {
      database.get(predecessor).successors.remove(old_flow);
      database.get(predecessor).successors.add(new_flow);
      database.get(predecessor).transition.put(new_flow, database.get(predecessor).transition.get(old_flow));
      database.get(predecessor).transition.remove(old_flow);
    }
    database.get(new_flow).successors.addAll(database.get(old_flow).successors);
    database.get(new_flow).successors.remove(new_flow);
    for (final String successor : database.get(old_flow).successors) {
      database.get(successor).predecessors.remove(old_flow);
      database.get(successor).predecessors.add(new_flow);
    }
    database.get(new_flow).transition.putAll(database.get(old_flow).transition);
    database.remove(old_flow);
  }

  public Flow getCurrentCursorFlow() {
    return this.flow_database.get(this.current_cursor);
  }

  public FlowProvider provide() {
    return new FlowProvider(this.flow_database);
  }

  public HashMap<String, FlowProvider> slicedsProvider() {
    HashMap<String, FlowProvider> sliceds_provider = new HashMap<String, FlowProvider>();
    for (String variable : this.sliceds.keySet()) {
      sliceds_provider.put(variable, new FlowProvider(this.sliceds.get(variable)));
    }
    return sliceds_provider;
  }

  public String toString() {
    final String s = this.name + "(): " + this.return_type;
    return s;
  }
}