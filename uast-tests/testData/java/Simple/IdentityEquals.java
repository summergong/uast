public class IdentityEquals {
    public static boolean foo() {
        Integer i1 = 111;
        Integer i2 = 222;
        Integer i12 = i1 + i2;
        Integer i21 = i2 + i1;
        return i12 == i21;
    }

    public static boolean bar() {
        String s1 = "hello";
        String s2 = s1 + s1;
        String s3 = "hellohello";
        return s2 == s3;
    }
}