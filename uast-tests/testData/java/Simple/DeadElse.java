public class DeadElse {
    public static int foo() {
        boolean f = true;
        int x = 0;
        int y = 1;
        int z;
        if (f) {
            z = y;
        }
        else {
            z = x;
        }
        return z;
    }
}