public class MutableField {
    static int mutable = 0;

    public static int foo() {
        mutable = 1;
        bar();
        return mutable;
    }

    public static void bar() {
        mutable = 2;
    }
}