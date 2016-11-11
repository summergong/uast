public class LabeledOuter {
    public static int foo() {

        int second = 2;
        labeled: for (int first = 1; first < 4; first++) {
            while (second < 10) {
                second = 3;
                if (first > 0) break labeled;
            }
            second = 4;
        }

        return second;
    }
}