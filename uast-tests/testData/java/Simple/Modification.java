public class Modification {
    public static String foo() {
        String s1 = "Hello ";
        s1 += "world";
        int m = 10;
        int n = 5;
        m += n -= 3;
        m /= n *= 2;
        s1 += " = ";
        return s1 + m + " / " + n;
    }
}