package za.co.bwmuller.anon.runtime;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * Created by Bernhard Müller on 13/12/2017.
 */

class ExecutionHelper {

    public static ExecutionResult execute(ProceedingJoinPoint joinPoint, boolean logInstance) throws Throwable {
        enterMethod(joinPoint, logInstance);

        long startNanos = System.nanoTime();
        Object result = joinPoint.proceed();
        long endNanos = System.nanoTime();
        ExecutionResult execution = new ExecutionResult(result, TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos));

        exitMethod(joinPoint, logInstance, execution.result, execution.duration);

        return execution;
    }

    private static void enterMethod(JoinPoint joinPoint, boolean enabled) {
        if (!enabled) return;

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        Class<?> cls = codeSignature.getDeclaringType();
        String methodName = codeSignature.getName();
        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        StringBuilder builder = new StringBuilder("\u21E2 ");
        builder.append(methodName).append('(');
        for (int i = 0; i < parameterValues.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterNames[i]).append('=');
            builder.append(parameterValues[i] == null ? "null" : parameterValues[i].toString());
        }
        builder.append(')');

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }

        Log.v(asTag(cls), builder.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final String section = builder.toString().substring(2);
            Trace.beginSection(section);
        }
    }

    private static void exitMethod(JoinPoint joinPoint, boolean enabled, Object result, long lengthMillis) {
        if (!enabled) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        Signature signature = joinPoint.getSignature();

        Class<?> cls = signature.getDeclaringType();
        String methodName = signature.getName();
        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ")
                .append(methodName)
                .append(" [")
                .append(lengthMillis)
                .append("ms]");

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(result == null ? "null" : result.toString());
        }

        Log.v(asTag(cls), builder.toString());
    }

    private static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }


    public static class ExecutionResult {
        long duration;
        Object result;

        ExecutionResult(Object result, long duration) {
            this.result = result;
            this.duration = duration;
        }
    }
}
