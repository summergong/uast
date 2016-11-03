public class DoWhile {
    public static int foo() {
        int count = 0;
        int number = 42;
        do {
            if (number % 10 == 7) {
                count++;
            }
            number = number / 10;
        } while (number > 0);
        return count;
    }
}