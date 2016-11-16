public class LocalClass {
    public static int foo() {

        class Local {};

        return new Local().hashCode();
    }
}