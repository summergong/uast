import java.util.LinkedList;

public class External {
    public static boolean foo() {
        return new LinkedList<Integer>() == new LinkedList<Integer>();
    }

    public static boolean bar() {
        List<Integer> list = new LinkedList<Integer>();
        return list == list;
    }
}