public class Test {
  public void method(Integer iana, String s) {
    if (iana == 1) {
      int a = 2;
      while (a < 3) {
        int c = 4;
      }
      for (int i = 0; i < 10; i++) {
        switch (i) {
          case 1:
          case 2:
            int e = 1;
            break;
          case 3:
            int f = 2;
            continue;
          case 4:
            int g = 3;
          default:
        }
      }
      int b = 3;
    } else {
      do {
        int d = 5;
      } while (s.isEmpty());
      return;
    }
  }
}