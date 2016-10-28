public class Dependents {
    public static int foo() {
        int x = 42;
        int y = x;
        int z = y;
        int w = z;
        return w;
    }
}