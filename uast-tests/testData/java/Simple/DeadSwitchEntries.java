public enum DeadSwitchEntries {
    FIRST,
    SECOND,
    THIRD;

    public static int bar() {
        DeadSwitchEntries key = THIRD;
        int result;
        switch (key) {
            case FIRST:
                result = 3;
                break;
            case SECOND:
                result = 7;
                break;
            case THIRD:
                result = 13;
                break;
            default:
                result = 66;
                break;
        }
        return result;
    }
}