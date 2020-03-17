package javaparser;

public class CustomString {
  private String str;

  public CustomString(final String str) {
    this.str = str;
  }

  public static CustomString makeCustomString(final String str) {
    return new CustomString(str);
  }

  public CustomString indent(int n) {
    if (n < 0)
      throw new Error("Expect positive value in indent function of class CustomString, instead having " + n);
    for (int i = 0; i < n; i++) {
      this.str = "  " + this.str;
    }
    return this;
  }

  public CustomString indent() {
    this.indent(1);
    return this;
  }

  public String finish() {
    return this.str;
  }
}