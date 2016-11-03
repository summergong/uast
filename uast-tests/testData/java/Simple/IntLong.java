public class IntLong {
    public static long foo() {
        int one = 1;
        int two = one + one;
        int four = two * two;
        int sixteen = four * four;
        int twoPowerEight = sixteen * sixteen;
        int twoPowerSixteen = twoPowerEight * twoPowerEight;
        int twoPowerTwentyFour = twoPowerSixteen * twoPowerEight;
        int twoPowerThirtyTwo = twoPowerSixteen * twoPowerSixteen;

        long twoPowerFourty = ((long) twoPowerSixteen) * ((long) twoPowerTwentyFour);
        long eight = 8L;
        long twoPowerFourtyThree = twoPowerFourty * eight;
        long twoPowerFourtyEight = twoPowerFourty * twoPowerEight;
        long twoPowerFiftySix = twoPowerEight * twoPowerFourty;
        return twoPowerFiftySix;
    }
}