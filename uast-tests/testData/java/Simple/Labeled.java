public class Labeled {
    public static int foo() {
        int first = 1;
        int second = 2;

        labeled: while (true) {
            second = 3;
            if (first > 0) break labeled;
        }

        return second;
    }
}