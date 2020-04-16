package analyst.core.zeus;

public class FieldData {

  public final String type;
  public String name;

  public FieldData(final String type, final String name) {
    this.type = type;
    this.name = name;
  }

  public void declareName(final String name) {
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