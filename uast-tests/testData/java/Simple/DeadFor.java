public class DeadFor {
    public static int foo() {
        int result = 0;
        for (int i = 9; i < 5; i++) {
            result = result + i;
        }
        return result;
    }
}