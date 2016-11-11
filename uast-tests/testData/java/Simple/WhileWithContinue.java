public class WhileWithContinue {

    public static boolean bar() {
        return true;
    }

    public static int foo() {
        int first = 1;
        int second = 2;

        while (bar()) {
            second = 3;
            if (first > 0) continue;
            second = 4;
        }

        return second;
    }
}