public abstract class Foo {
    public abstract String getTestName(boolean upper);

    public void bar() {
        String t1 = getTestName(false);
        String t2 = getTestName(true);
    }
}
