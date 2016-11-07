public class Anonymous {

    public static void bar() {
        int variable1 = 24;
        variable1++;

    }

    public static int foo() {
        int variable = 42;

        Runnable runnable = User::bar;
        runnable.run();

        return variable;
    }
}