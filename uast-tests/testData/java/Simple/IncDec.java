public class IncDec {
    public static int foo() {
        int i1 = 1;    // 1
        int i2 = ++i1; // 2, 2
        int i3 = i2++; // 2, 3
        int i4 = --i3; // 1, 1
        int i5 = i4--; // 1, 0
        return i4 + i5;// 1
    }
}