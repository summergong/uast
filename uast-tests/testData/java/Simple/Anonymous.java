public class Anonymous {
    public static int foo() {
        int variable = 42;

        Runnable runnable = new Runnable() {

            public void run() {
                int variable = 24;
                variable++;
            }
        };
        runnable.run();

        return variable;
    }
}