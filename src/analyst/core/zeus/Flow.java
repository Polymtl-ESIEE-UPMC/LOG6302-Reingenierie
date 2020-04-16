package analyst.core.zeus;

import java.util.HashMap;

import analyst.helper.ArraySet;
import analyst.helper.UUID;

public class Flow {
  public final String id;
  public final String type;
  public final String name;
  public String info = "";
  public boolean alive = true;
  public boolean incomplete = false;
  public final ArraySet<String> predecessors;
  public final ArraySet<String> successors;
  public final HashMap<String, String> transition;
  public final ArraySet<Definition> gens;
  public final ArraySet<Definition> kills;
  public final ArraySet<Definition> ins;
  public final ArraySet<Definition> outs;
  public final ArraySet<String> uses;
  public String immediate_dominator = "";
  public String immediate_post_dominator = "";
  public final ArraySet<String> control_dependants;
  public final ArraySet<String> controllers;
  public final ArraySet<String> data_dependants;
  public final ArraySet<String> data_controllers;

  public Flow(final String type, final String name) {
    this.id = UUID.get();
    this.type = type;
    this.name = name;
    this.predecessors = new ArraySet<String>();
    this.successors = new ArraySet<String>();
    this.transition = new HashMap<String, String>();
    this.gens = new ArraySet<Definition>();
    this.kills = new ArraySet<Definition>();
    this.ins = new ArraySet<Definition>();
    this.outs = new ArraySet<Definition>();
    this.uses = new ArraySet<String>();
    this.control_dependants = new ArraySet<String>();
    this.controllers = new ArraySet<String>();
    this.data_dependants = new ArraySet<String>();
    this.data_controllers = new ArraySet<String>();
  }

  private Flow(Flow flow) {
    this.id = flow.id;
    this.type = flow.type;
    this.name = flow.name;
    this.info = flow.info;
    this.alive = flow.alive;
    this.incomplete = flow.incomplete;
    this.predecessors = flow.predecessors.clone();
    this.successors = flow.successors.clone();
    this.transition = new HashMap<String, String>();
    this.transition.putAll(flow.transition);
    this.gens = flow.gens.clone();
    this.kills = flow.kills.clone();
    this.ins = flow.ins.clone();
    this.outs = flow.outs.clone();
    this.uses = flow.uses.clone();
    this.immediate_dominator = flow.immediate_dominator;
    this.immediate_post_dominator = flow.immediate_post_dominator;
    this.control_dependants = flow.control_dependants.clone();
    this.controllers = flow.controllers.clone();
    this.data_dependants = flow.data_dependants.clone();
    this.data_controllers = flow.data_controllers.clone();
  }

  public Flow clone() {
    return new Flow(this);
  }

  public String toString() {
    String s = "{flow_" + this.id + ": " + this.name + " " + this.info + "|";
    if (this.gens.isEmpty()) {
      s += "GEN = [ ]\\l";
    } else {
      for (Definition def : this.gens) {
        s += "GEN " + def.id + ": " + def.variable + " = " + def.expression + "\\l";
      }
    }
    s = s.replace("<", "\\<").replace(">", "\\>");
    s += "|";
    s += "KILL = [ ";
    for (Definition def : this.kills) {
      s += def.id + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "IN = [ ";
    for (Definition def : this.ins) {
      s += def.id + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "OUT = [ ";
    for (Definition def : this.outs) {
      s += def.id + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "USE = [ ";
    for (String use : this.uses) {
      s += use + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "Immediate dominator: " + (!this.immediate_dominator.isEmpty() ? "flow " + this.immediate_dominator : "")
        + "\\l";
    s += "|";
    s += "Immediate post-dominator: "
        + (!this.immediate_post_dominator.isEmpty() ? "flow " + this.immediate_post_dominator : "") + "\\l";
    s += "|";
    s += "Control Dependant = [ ";
    for (String flow : this.control_dependants) {
      s += "flow_" + flow + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "Controller = [ ";
    for (String flow : this.controllers) {
      s += "flow_" + flow + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "Data Dependant = [ ";
    for (String flow : this.data_dependants) {
      s += "flow_" + flow + " ";
    }
    s += " ]\\l";
    s += "|";
    s += "Data Controller = [ ";
    for (String flow : this.data_controllers) {
      s += "flow_" + flow + " ";
    }
    s += " ]\\l}";
    return s;
  }
}