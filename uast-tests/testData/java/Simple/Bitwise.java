public class Bitwise {
    public static int foo() {
        int first  = 0x1234567;
        int second = 0x89abcde;

        return (first & second) + (first | second) + (first ^ second);
    }

    public static long bar() {
        long first  = 0x123456789abcdefL;
        long second = 0xfedcba987654321L;

        return (first & second) + (first | second) + (first ^ second);
    }
}