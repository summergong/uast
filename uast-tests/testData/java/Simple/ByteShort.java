public class ByteShort {
    public static int foo() {
        byte b1 = 100;
        short s2 = 2;
        byte b11 = b1;
        short s21 = s2;
        int i3 = b11 + b1;
        int i4 = s21 + s2;
        int i5 = b11 + s21;
        byte b3 = (byte) i3;
        short s4 = (short) i4;
        return i5 + b3 + s4;
    }
}