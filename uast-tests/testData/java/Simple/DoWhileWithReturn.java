public class DoWhileWithReturn {
    public static int foo() {
        int count = 0;
        int number = 1;
        do {
            if (number > 0) return count;
            count++;
            number--;
        } while (number >= 0);
        return count;
    }
}