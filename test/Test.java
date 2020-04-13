public class Test {
  public void method(Integer iana, String s) {
    int a = 1;
    int b = 2;
    int c = 3;
    if (iana == 1) {
      a = 2;
      int d = 5;
      while (a < 3) {
        b = 3;
        d = 6;
      }
      for (int i = 0; i < 10; i++) {
        switch (i) {
          case 1:
          case 2:
            c = 4;
            break;
          case 3:
            d = 7;
            continue;
          case 4:
            b = 8;
          default:
        }
      }
    } else {
      do {
        a = 5;
      } while (s.isEmpty());
      return;
    }
  }
}