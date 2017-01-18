public enum Style {
    SHEET("foo") {
        @Override
        public String getExitAnimation() {
            return "bar";
        }
    };

    Style(String s) {
    }

    public String getExitAnimation() {
        return null;
    }
}

