package com.bence.projector.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class DebugUtil {
    private static final HashMap<String, TimeSpentAtLine> hashMap = new HashMap<>();
    private static Date previousDate;
    private static Date previousDateForGatherTime;

    @SuppressWarnings("unused")
    public static void printDate() {
        if (previousDate == null) {
            previousDate = new Date();
        } else {
            Date newDate = new Date();
            double time = newDate.getTime() - previousDate.getTime();
            StackTraceElement stackTraceElement = getStackTraceElement();
            String s = "";
            if (stackTraceElement != null) {
                s = "\t" + stackTraceElement.getClassName() + "\t" + stackTraceElement.getLineNumber();
            }
            System.out.println(time / 1000 + s);
            previousDate = newDate;
        }
    }

    private static StackTraceElement getStackTraceElement() {
        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length > 2) {
            return stackTrace[2];
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static void gatherTime() {
        Throwable throwable = new Throwable();
        if (previousDateForGatherTime == null) {
            previousDateForGatherTime = new Date();
        } else {
            Date newDate = new Date();
            double time = newDate.getTime() - previousDateForGatherTime.getTime();
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            if (stackTrace.length > 1) {
                StackTraceElement stackTraceElement = stackTrace[1];
                int currentLine = stackTraceElement.getLineNumber();
                String className = stackTraceElement.getClassName();
                String key = className + "_" + currentLine;
                TimeSpentAtLine timeSpentAtLine = hashMap.get(key);
                if (timeSpentAtLine == null) {
                    timeSpentAtLine = new TimeSpentAtLine();
                    timeSpentAtLine.setTimeSpent(time);
                    timeSpentAtLine.setLineNumber(currentLine);
                    timeSpentAtLine.setClassName(className);
                    timeSpentAtLine.setStackTraceElement(stackTraceElement);
                } else {
                    timeSpentAtLine.addTimeSpent(time);
                }
                hashMap.put(key, timeSpentAtLine);
            }
            previousDateForGatherTime = newDate;
        }
    }

    @SuppressWarnings("unused")
    public static void summaryGatheredTime() {
        Collection<TimeSpentAtLine> values = hashMap.values();
        List<TimeSpentAtLine> timeSpentAtLines = new ArrayList<>(values);
        timeSpentAtLines.sort((o1, o2) -> {
            int i = o1.getClassName().compareTo(o2.getClassName());
            if (i == 0) {
                return Long.compare(o1.getLineNumber(), o2.getLineNumber());
            }
            return i;
        });
        printCollection(timeSpentAtLines);
        timeSpentAtLines.sort(Comparator.comparingDouble(TimeSpentAtLine::getTimeSpent));
        printCollection(timeSpentAtLines);
        hashMap.clear();
        previousDateForGatherTime = null;
    }

    private static void printCollection(Collection<TimeSpentAtLine> values) {
        for (TimeSpentAtLine timeSpentAtLine : values) {
            System.out.println(timeSpentAtLine.toString());
        }
        System.out.println();
        System.out.println();
    }
}
