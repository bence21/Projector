package com.bence.projector.server.utils;

import com.bence.projector.server.utils.interfaces.MatchesInterface;

import java.util.List;

public class ListUtil {

    public static <T> boolean listSizeDifferent(List<T> songVerses, List<T> song2Verses) {
        return songVerses.size() != song2Verses.size();
    }

    public static <T extends MatchesInterface<T>> boolean twoListMatches(List<T> list1, List<T> list2) {
        if (listSizeDifferent(list1, list2)) {
            return false;
        }
        for (int i = 0; i < list1.size(); ++i) {
            if (!list1.get(i).matches(list2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
