package com.notifier;

/**
 * Created by markhulia on 17/05/15.
 */
public class NumberGenerator {
    public static String[] getNumbers() {
        int[] listOfIntegers = new int[1001];
        String[] listOfStrings = new String[1001];

        for (int i = 0; i < 1001; i++) {
            listOfIntegers[i] = i;
            listOfStrings[i] = String.valueOf(listOfIntegers[i]);
        }
        return listOfStrings;
    }
}