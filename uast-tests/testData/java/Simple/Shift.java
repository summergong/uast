public class Shift {
    public static int foo() {
        int one = 1;
        int two = one << 1;
        int sixteen = two << 3;
        int minInt = 0x80000000;
        int quarter = minInt >> 2;
        int unsignedQuarter = minInt >>> 2;

        return sixteen + quarter + unsignedQuarter;
    }

    public static long bar() {
        long one = 1L;
        long two = one << 1;
        long large = two << 61;
        long minLong = 0x8000000000000000L;
        long eighth = minLong >> 3;
        long unsignedEighth = minLong >>> 3;

        return large + eighth + unsignedEighth;
    }
}