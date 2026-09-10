package projector.utils;

public final class DevelopmentMode {

    private DevelopmentMode() {
    }

    public static boolean isActive() {
        return AppProperties.getInstance().isDevelopmentMode();
    }
}
