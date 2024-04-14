package com.bence.projector.server.utils;

import java.util.ArrayList;

public class MemoryUtil {

    public static <T> ArrayList<T> getEmptyList() {
        return new ArrayList<>(); // should be just one for every T
    }
}
