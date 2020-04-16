package analyst.helper;

import java.util.Collection;
import java.util.HashMap;

import analyst.core.zeus.Flow;

public class FlowProvider {
  private final HashMap<String, Flow> flow_database;

  public FlowProvider(HashMap<String, Flow> flow_database) {
    this.flow_database = flow_database;
  }

  public Collection<Flow> allFlow() {
    return this.flow_database.values();
  }

  public Flow flow(String key) {
    return this.flow_database.get(key);
  }

  public ArraySet<Flow> flows(ArraySet<String> keys) {
    ArraySet<Flow> flows = new ArraySet<Flow>();
    for (String key : keys) {
      flows.add(this.flow_database.get(key));
    }
    return flows;
  }

}