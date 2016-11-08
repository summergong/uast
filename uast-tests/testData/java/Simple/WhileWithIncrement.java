public class WhileWithIncrement {
    public static int foo() {
        int i = 0;
        while (true) {
            i++;
            if (i % 42 == 0) break;
        }
        return i;
    }
}