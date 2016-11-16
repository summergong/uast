public class Lambda {
    public static int foo() {
        int variable = 42;

        Runnable runnable = () -> {
            int variable1 = 24;
            variable1++;
        };
        runnable.run();

        return variable;
    }
}