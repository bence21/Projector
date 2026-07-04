package projector.utils;

import projector.api.RemoteFetchFailureKind;
import projector.api.RemoteFetchSupport;

import java.util.ResourceBundle;

public final class ConnectionErrorMessages {

    private ConnectionErrorMessages() {
    }

    public static String getMessage(ResourceBundle bundle, RemoteFetchFailureKind kind) {
        String lead;
        if (kind == RemoteFetchFailureKind.NO_INTERNET) {
            lead = bundle.getString("No internet connection");
        } else if (kind == RemoteFetchFailureKind.UNKNOWN) {
            lead = bundle.getString("Something went wrong");
        } else {
            lead = bundle.getString("Could not reach the server");
        }
        return lead + "! " + bundle.getString("Try again later") + "!";
    }

    public static String getMessageForAmbiguousFailure(ResourceBundle bundle) {
        return getMessage(bundle, RemoteFetchSupport.classifyConnectivityFailure());
    }
}
