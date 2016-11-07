public enum DeadSwitchEntriesWithoutBreaks {
    FIRST,
    SECOND,
    THIRD;

    public static int bar() {
        DeadSwitchEntriesWithoutBreaks key = SECOND;
        int result;
        switch (key) {
            case FIRST:
                result = 3;
                break;
            case SECOND:
                result = 7;
            case THIRD:
                result = 13;
            default:
                result = 66;
                break;
        }
        return result;
    }
}