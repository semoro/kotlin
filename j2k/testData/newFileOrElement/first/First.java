package p;

public class Some {
    public static String test = "" + "a";

    public static void main(String[][][] args, int[] b) {
        b[0] |= test;
        Some.test = test;
    }
}