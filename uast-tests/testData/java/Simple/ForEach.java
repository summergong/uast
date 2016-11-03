public class For {
    public static int sum(List<Integer> numbers) {
        int result = 0;
        for (int number: numbers) {
            result = result + number;
        }
        return result;
    }
}