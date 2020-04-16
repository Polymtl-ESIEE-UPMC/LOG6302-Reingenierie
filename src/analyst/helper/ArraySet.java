package analyst.helper;

import java.util.ArrayList;

/* Set backed by ArrayList, can't have 2 exact same element */
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

  public boolean addAll(ArrayList<T> l) {
    ArraySet<T> old = this.clone();
    for (T e : l) {
      this.add(e);
    }
    if (!old.equals(this))
      return true;
    return false;
  }

  public T getLast() {
    if (super.isEmpty())
      return null;
    return super.get(super.size() - 1);
  }

  public T removeLast() {
    if (super.isEmpty())
      return null;
    return super.remove(super.size() - 1);
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

  public ArraySet<T> clone() {
    ArraySet<T> new_set = new ArraySet<T>();
    for (T t : this) {
      new_set.add(t);
    }
    return new_set;
  }

}