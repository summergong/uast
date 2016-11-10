public class FloatDouble {
    public static double foo() {
        float f1 = 1.0F;
        double d2 = 2.0;
        double d3 = f1 + d2;
        float f2 = f1 + f1;
        float f3 = (float) d3;
        double d1 = (double) f1;
        return d1 + f1 + f2 + f3;
    }
}