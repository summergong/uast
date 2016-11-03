public class While {
    public static int foo() {
        int result = 0, i = 0;
        while (i < 10) {
            result = result + i++;
        }
        return result;
    }
}