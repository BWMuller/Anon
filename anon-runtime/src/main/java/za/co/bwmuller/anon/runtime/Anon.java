package za.co.bwmuller.anon.runtime;

import android.text.TextUtils;
import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

import za.co.bwmuller.anon.annotations.AnonClass;
import za.co.bwmuller.anon.annotations.AnonMethod;
import za.co.bwmuller.anon.annotations.Trackable;


/**
 * Created by Bernhard Müller on 13/12/2017.
 */

@Aspect
public class Anon extends AnonState {
    private static final String TAG = "ANON";

    @Pointcut("within(@za.co.bwmuller.anon.annotations.AnonClass *)")
    public void withinAnnotatedClass() {
    }

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    @Around("execution(@(@za.co.bwmuller.anon.annotations.Trackable *) * *(..)) && methodInsideAnnotatedType()")
    public Object trackable(ProceedingJoinPoint joinPoint) throws Throwable {
        AnonClass anonClass = ExecutionHelper.getAnnotatedClassAnon(joinPoint.getThis().getClass());
        ExecutionHelper.ExecutionResult execution = ExecutionHelper.execute(joinPoint, traceLogging && enabled && ExecutionHelper.isClassEnabled(anonClass));

        try {
            if (enabled && callback != null && ExecutionHelper.isClassEnabled(anonClass)) {
                Annotation trackable = null;
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                for (Annotation annotation : signature.getMethod().getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(Trackable.class)) {
                        trackable = annotation;
                        break;
                    }
                }
                ExecutionHelper.tryPostDelayed(new RuntimeRunnable<Annotation>(anonClass, trackable, execution, signature.getName()) {
                    @Override
                    public void run() {
                        try {
                            callback.onTrackable(anonClass, annotation, name, execution.duration);
                        } catch (Throwable ex) {
                            Log.e(TAG, "Error executing onTrackable", ex);
                        }
                    }
                });
            }
        } catch (Throwable ex) {
            Log.e(TAG, "Error triggering onTrackable", ex);
        }

        return execution.result;
    }

    @Around("execution(@za.co.bwmuller.anon.annotations.AnonMethod * *(..)) && methodInsideAnnotatedType() && @annotation(methodTrack)")
    public Object methodTrack(ProceedingJoinPoint joinPoint, AnonMethod methodTrack) throws Throwable {
        AnonClass anonClass = ExecutionHelper.getAnnotatedClassAnon(joinPoint.getThis().getClass());
        ExecutionHelper.ExecutionResult execution = ExecutionHelper.execute(joinPoint, enabled && traceLogging && ExecutionHelper.isClassEnabled(anonClass));
        try {
            if (enabled && callback != null && ExecutionHelper.isClassEnabled(anonClass)) {
                ExecutionHelper.tryPostDelayed(new RuntimeRunnable<AnonMethod>(anonClass, methodTrack, execution, joinPoint.getSignature().getName()) {
                    @Override
                    public void run() {
                        try {
                            callback.onMethod(anonClass, annotation, name, execution.duration);
                        } catch (Throwable ex) {
                            Log.e(TAG, "Error executing onMethod", ex);
                        }
                    }
                });
            }
        } catch (Throwable ex) {
            Log.e(TAG, "Error triggering onTrackable", ex);
        }
        return execution.result;
    }
}
