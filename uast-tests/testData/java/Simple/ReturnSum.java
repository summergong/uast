public class ReturnSum {
    public static int foo() {
        int x = 1 + 2;
        int y = 3 + 4;
        int z = x + y;
        return z + z; // 20
    }
}