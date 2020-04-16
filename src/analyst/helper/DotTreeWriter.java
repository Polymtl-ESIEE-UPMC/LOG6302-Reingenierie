package analyst.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import analyst.core.zeus.ClassData;
import analyst.core.zeus.DotNodeCFG;
import analyst.core.zeus.DotNodeUML;
import analyst.core.zeus.Flow;

public class DotTreeWriter extends DotWriter {

  private String path;

  public void saveAsUML(final HashMap<String, DotNodeUML> dot_tree_uml) {
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

  public void saveAsCFG(final HashMap<String, DotNodeCFG> dot_tree_cfg) {
    for (final DotNodeCFG dot_node : dot_tree_cfg.values()) {
      saveNode(dot_node, () -> {
        for (int i = 0; i < dot_node.methods.size(); i++) {
          super.writeln(dot_node.methods.get(i).id + " -> " + dot_node.methods.get(i).getCurrentCursorFlow().id);
          super.writeLabel(dot_node.methods.get(i).id, dot_node.methods.get(i).toString());
          writeLabel(dot_node.methods.get(i).getCurrentCursorFlow().id,
              dot_node.methods.get(i).getCurrentCursorFlow().toString(),
              dot_node.methods.get(i).getCurrentCursorFlow().type);
          writeFlows(dot_node.methods.get(i).provide());
          for (String variable : dot_node.methods.get(i).slicedsProvider().keySet()) {
            (new DotSlicedWriter(this.path + dot_node.methods.get(i).name + "/", variable))
                .writeSliced(dot_node.methods.get(i).slicedsProvider().get(variable));
          }
        }
        return null;
      });
    }
  }

  protected void writeFlows(FlowProvider provide) {
    for (final Flow flow : provide.allFlow()) {
      for (final Flow successor : provide.flows(flow.successors)) {
        String transition_label = flow.transition.get(successor.id);
        transition_label = transition_label != null ? " [label=\"" + transition_label + "\"]" : "";
        super.writeln(flow.id + " -> " + successor.id + transition_label);
        writeLabel(flow.id, flow.toString(), flow.type);
        writeLabel(successor.id, successor.toString(), successor.type);
      }
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
      this.path = "./results/" + type + "/dot/" + dot_node.name + "/";
      final File uml_dot_file = new File(this.path + dot_node.name + ".dot");
      uml_dot_file.getParentFile().mkdirs();
      uml_dot_file.createNewFile(); // if file already exists will do nothing
      super.output_stream_uml_dot_file = new FileOutputStream(uml_dot_file, false);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}