public class CascadeIf {
    public static int foo(boolean f, boolean g, boolean h) {
        int x = 0;
        int y = 1;
        int v = 2;
        int w = 3;
        int z;
        if (f) {
            z = y;
        }
        else if (g) {
            z = x;
        }
        else if (h) {
            z = v;
        }
        else {
            z = w;
        }
        return z;
    }
}