public class Test {
  public void method(Integer iana, String s) {
    int a = 1;
    int b = 2;
    int c = 3;
    if (iana == 1) {
      a = 2;
      int d = 5;
      while (a < 3) {
        d = b + iana;
      }
      for (int i = 0; i < 10; i++) {
        switch (i) {
          case 1:
          case 2:
            c = a + b;
            System.out.print(b);
            break;
          case 3:
            d = c + a;
            continue;
          case 4:
            b = a + d;
          default:
        }
        if (i > 7) {
          System.out.print(c);
          break;
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