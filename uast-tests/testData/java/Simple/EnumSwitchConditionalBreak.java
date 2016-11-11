public enum EnumSwitchConditionalBreak {
    FIRST,
    SECOND,
    THIRD;

    public static int foo(EnumSwitchConditionalBreak key, int result) {
        int newResult;
        int counter = 0;
        switch (key) {
            case FIRST:
                if (result > 0) {
                    newResult = 42;
                    counter++;
                    break;
                }
                counter++;
            default:
                newResult = 42;
                counter++;
                break;
        }
        return newResult + counter;
    }
}