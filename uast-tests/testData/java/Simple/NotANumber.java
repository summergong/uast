public class NotANumber {
    public static boolean foo() {
        double x = 0.0 / 0.0;
        boolean b1 = x < x;
        boolean b2 = x > x;
        boolean b3 = x <= x;
        boolean b4 = x >= x;
        boolean b5 = x == x;
        boolean b6 = x != x;
        return b1 || b2 || b3 || b4 || b5 || !b6;
    }

    public static boolean bar() {
        float x = 0.0f / 0.0f;
        boolean b1 = x <= x;
        boolean b2 = x >= x;
        boolean b3 = x < x;
        boolean b4 = x > x;
        boolean b5 = x == x;
        boolean b6 = x != x;
        return b1 || b2 || b3 || b4 || b5 || !b6;
    }
}