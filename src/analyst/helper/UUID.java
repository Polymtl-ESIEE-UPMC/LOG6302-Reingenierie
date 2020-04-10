package analyst.helper;

public class UUID {

  private static long uuid = 0;

  public static String get() {
    long return_value = uuid;
    uuid++;
    return return_value + "";
  }

}