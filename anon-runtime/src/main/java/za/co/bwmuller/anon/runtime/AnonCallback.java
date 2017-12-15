package za.co.bwmuller.anon.runtime;

import java.lang.annotation.Annotation;

import za.co.bwmuller.anon.annotations.AnonMethod;
import za.co.bwmuller.anon.annotations.AnonClass;

/**
 * Created by Bernhard Müller on 13/12/2017.
 */

public interface AnonCallback {
    void onMethod(AnonClass anonClass, AnonMethod methodTrack, String methodName, long executionDuration);

    void onTrackable(AnonClass anonClass, Annotation trackable, String methodName, long executionDuration);
}
