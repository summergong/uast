public class Logicals {
    public static boolean foo() {
        int one = 1;
        int two = 2;
        int three = 3;
        int four = 4;
        boolean b1 = two > one && four > three;
        boolean b2 = one > two && four > three;
        boolean b3 = b1 || b2;
        boolean b4 = two > one || three > four;
        return b1 && !b2 && b3 && b4;
    }
}