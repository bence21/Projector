package com.bence.songbook.utils;

public class Utility {

    public static int compare(int a, int b) {
        if (a < b) {
            return -1;
        } else {
            return a == b ? 0 : 1;
        }
    }
}
