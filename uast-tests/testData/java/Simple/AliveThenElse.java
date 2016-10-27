public class AliveThenElse {
    public static int foo(boolean f) {
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