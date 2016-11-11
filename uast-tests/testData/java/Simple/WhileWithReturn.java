public class WhileWithReturn {
    public static int foo() {
        int first = 1;
        int second = 2;

        while (first == 1) {
            second = 3;
            if (first > 0) return second;
        }

        return second;
    }
}