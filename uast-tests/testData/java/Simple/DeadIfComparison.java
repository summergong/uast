public class DeadIfComparison {
    public static int foo() {
        int x = 0;
        int y = 1;
        int z;
        if (x == y) {
            z = y;
        }
        else {
            z = x;
        }
        return z;
    }
}
