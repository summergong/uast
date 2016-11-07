public class ImmutableField {
    final int immutable;

    public ImmutableField() {
        immutable = 1;
        bar(immutable);
        bar(immutable);
    }

    public static int bar(int arg) {
        return arg;
    }
}