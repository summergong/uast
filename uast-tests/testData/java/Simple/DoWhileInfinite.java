public class DoWhileInfinite {
    public static int foo() {
        int count = 0;
        int number = 42;
        do {
            if (number % 10 == 7) {
                count++;
            }
        } while (number > 0);
        return count;
    }
}