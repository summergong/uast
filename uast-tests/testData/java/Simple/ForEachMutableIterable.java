import java.util.*;

public class For {

    public static List<Integer> getList(int size) {
        List<Integer> result = new LinkedList<Integer>();
        int a = 0;
        for (int i = a++; i < size; i++) {
            result.add(i);
        }
        result.add(a);
        return result;
    }

    public static int sum(List<Integer> numbers) {
        int result = 0;
        int size = 3;
        for (int number: getList(++size)) {
            result = result + number;
        }
        return result + size;
    }
}