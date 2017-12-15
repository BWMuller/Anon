package za.co.bwmuller.anon.runtime;

/**
 * Created by Bernhard Müller on 13/12/2017.
 */

public class AnonState {
    static volatile boolean enabled = true;
    static volatile boolean traceLogging = false;
    static volatile AnonCallback callback = null;

    public static void setTraceLogging(boolean traceLogging) {
        AnonState.traceLogging = traceLogging;
    }

    public static void setCallback(AnonCallback callback) {
        AnonState.callback = callback;
    }

    public static void setEnabled(boolean enabled) {
        AnonState.enabled = enabled;
    }

}
