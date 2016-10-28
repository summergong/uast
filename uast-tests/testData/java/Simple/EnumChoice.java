public enum EnumChoice {
    FIRST,
    SECOND;

    public EnumChoice foo(boolean flag) {
        EnumChoice result;
        if (flag) {
            result = FIRST;
        }
        else {
            result = SECOND;
        }
        return result;
    }
}