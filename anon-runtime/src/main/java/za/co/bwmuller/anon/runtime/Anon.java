package za.co.bwmuller.anon.runtime;

import android.os.Handler;
import android.os.Looper;

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
    @Pointcut("within(@za.co.bwmuller.anon.annotations.AnonClass *)")
    public void withinAnnotatedClass() {
    }

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    @Around("execution(@(@za.co.bwmuller.anon.annotations.Trackable *) * *(..)) && methodInsideAnnotatedType()")
    public Object trackable(ProceedingJoinPoint joinPoint) throws Throwable {
        AnonClass anonClass = getAnnotatedClassAnon(joinPoint.getThis().getClass());
        ExecutionHelper.ExecutionResult execution = ExecutionHelper.execute(joinPoint, traceLogging && enabled && isClassEnabled(anonClass));

        if (enabled && callback != null && isClassEnabled(anonClass)) {
            Annotation trackable = null;
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            for (Annotation annotation : signature.getMethod().getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(Trackable.class)) {
                    trackable = annotation;
                    break;
                }
            }
            new Handler(Looper.getMainLooper()).postDelayed(new RuntimeRunnable<Annotation>(anonClass, trackable, execution, signature.getName()) {
                @Override
                public void run() {
                    callback.onTrackable(anonClass, annotation, name, execution.duration);
                }
            }, 10);
        }

        return execution.result;
    }

    private boolean isClassEnabled(AnonClass anonClass) {
        return anonClass != null && anonClass.enable();
    }

    @Around("execution(@za.co.bwmuller.anon.annotations.AnonMethod * *(..)) && methodInsideAnnotatedType() && @annotation(methodTrack)")
    public Object methodTrack(ProceedingJoinPoint joinPoint, AnonMethod methodTrack) throws Throwable {
        AnonClass anonClass = getAnnotatedClassAnon(joinPoint.getThis().getClass());
        ExecutionHelper.ExecutionResult execution = ExecutionHelper.execute(joinPoint, enabled && traceLogging && isClassEnabled(anonClass));
        if (enabled && callback != null && isClassEnabled(anonClass)) {
            new Handler(Looper.getMainLooper()).postDelayed(new RuntimeRunnable<AnonMethod>(anonClass, methodTrack, execution, joinPoint.getSignature().getName()) {
                @Override
                public void run() {
                    callback.onMethod(anonClass, annotation, name, execution.duration);
                }
            }, 10);
        }

        return execution.result;
    }

    private AnonClass getAnnotatedClassAnon(Class<?> clazz) {
        AnonClass anon = clazz.getAnnotation(AnonClass.class);
        if (anon == null) {
            return getAnnotatedClassAnon(clazz.getEnclosingClass());
        } else {
            return anon;
        }
    }

    private boolean isCallback(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getParameterTypes().length == 3 && signature.getMethod().getParameterTypes()[0] == AnonClass.class;
    }

    private abstract class RuntimeRunnable<T extends Annotation> implements Runnable {
        AnonClass anonClass;
        T annotation;
        ExecutionHelper.ExecutionResult execution;
        String name;

        public RuntimeRunnable(AnonClass anonClass, T annotation, ExecutionHelper.ExecutionResult execution, String name) {
            this.anonClass = anonClass;
            this.annotation = annotation;
            this.execution = execution;
            this.name = name;
        }
    }
}
