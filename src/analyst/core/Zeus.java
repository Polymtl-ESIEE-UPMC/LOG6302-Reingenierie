package analyst.core;

import java.util.HashMap;

import analyst.core.zeus.ClassData;
import analyst.core.zeus.DotNodeCFG;
import analyst.core.zeus.DotNodeUML;
import analyst.helper.ArrayStack;
import analyst.helper.DotTreeWriter;

/* Singleton Zeus */
public class Zeus {

  private final boolean FEATURE_FLAG_UML = false;
  private final boolean FEATURE_FLAG_CFG = true;

  // Le queue des structures de donnee des class
  private final ArrayStack<ClassData> class_database = new ArrayStack<ClassData>();
  private final HashMap<String, DotNodeUML> dot_tree_uml = new HashMap<String, DotNodeUML>();
  private final HashMap<String, DotNodeCFG> dot_tree_cfg = new HashMap<String, DotNodeCFG>();

  public static Zeus singleton = new Zeus();

  /* Commencer la declaration un nouveau class */
  public ClassData declareClass() {
    this.class_database.push(new ClassData());
    return this.class_database.peek();
  }

  /* Continuer la declaration de la class actuel */
  public ClassData connectClassDatabase() {
    return this.class_database.peek();
  }

  // quand on fini la declaration on sync sur le DotTree
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

  /*
   * la fonction done qui est appele dans le parser, une fois il termine tous les
   * parsings pour trigger l'ecriture au dot files
   */
  public void done() {
    if (FEATURE_FLAG_UML)
      (new DotTreeWriter()).saveAsUML(this.dot_tree_uml);
    if (FEATURE_FLAG_CFG)
      (new DotTreeWriter()).saveAsCFG(this.dot_tree_cfg);
  }

}