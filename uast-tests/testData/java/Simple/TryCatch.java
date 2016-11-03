public class TryCatch {
    public static int foo(String str) {
        int sum = 0;
        for (String part: str.split(" ")) {
            int b = 0;
            try {
                sum = sum + Integer.parseInt(part);
                b = 1;
            }
            catch (NumberFormatException ex) {
                b = 1;
            }
            int c = b;
        }
        return sum;
    }
}