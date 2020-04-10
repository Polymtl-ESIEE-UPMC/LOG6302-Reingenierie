package analyst.helper;

import java.util.ArrayList;

public class ArrayStack<T> extends ArrayList<T> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private boolean stack_mode = false;

  public boolean push(T e) {
    return super.add(e);
  }

  public T peek() {
    return super.get(super.size() - 1);
  }

  public T pop() {
    return super.remove(super.size() - 1);
  }

  public ArrayStack<T> asStack() {
    this.stack_mode = true;
    return this;
  }

  public T get(int i) {
    if (!this.stack_mode)
      return super.get(i);
    else {
      this.stack_mode = false;
      return super.get(super.size() - 1 - i);
    }
  }

}