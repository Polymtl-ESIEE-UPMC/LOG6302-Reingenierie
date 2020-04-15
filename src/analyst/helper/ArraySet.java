package analyst.helper;

import java.util.ArrayList;

public class ArraySet<T> extends ArrayList<T> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public boolean add(T e) {
    if (super.contains(e))
      return false;
    return super.add(e);
  }

  @SuppressWarnings("unchecked")
  public boolean addAll(ArrayList<T> l) {
    ArraySet<T> old = (ArraySet<T>) super.clone();
    for (T e : l) {
      this.add(e);
    }
    if (!old.equals(this))
      return true;
    return false;
  }

  public boolean equals(ArraySet<T> s) {
    if (super.size() != s.size())
      return false;
    for (T e : s) {
      if (!super.contains(e))
        return false;
    }
    return true;
  }

}