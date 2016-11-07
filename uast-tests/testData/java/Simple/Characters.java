public class Characters {
    public static char foo() {
        char a = 'a';
        char c = (char) (a + 2);
        char f = (char) (c + 3);
        char d = (char) (f - 2);
        return d;
    }
}