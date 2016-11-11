public class WhileWithMutableCondition {
    public static int foo() {
        int i = 0;
        while (++i < 2) {
            i--;
        }
        return i;
    }
}