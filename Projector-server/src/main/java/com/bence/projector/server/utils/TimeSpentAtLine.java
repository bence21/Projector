package com.bence.projector.server.utils;

public class TimeSpentAtLine {
    private double timeSpent;
    private String className;
    private long lineNumber;
    private StackTraceElement stackTraceElement;

    public double getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(double timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void addTimeSpent(double time) {
        this.timeSpent += time;
    }

    @Override
    public String toString() {
        String filePath = System.getProperty("user.dir") + "\\Projector-server\\src\\main\\java\\" + className.replaceAll("\\.", "\\\\") + ".java";
        return timeSpent / 1000 + "\t" + filePath + ":" + lineNumber + "\t" + stackTraceElement.getMethodName();
    }

    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }

    public void setStackTraceElement(StackTraceElement stackTraceElement) {
        this.stackTraceElement = stackTraceElement;
    }
}
