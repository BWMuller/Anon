package za.co.bwmuller.anon.runtime;

import java.lang.annotation.Annotation;

import za.co.bwmuller.anon.annotations.AnonClass;

/**
 * Created by Bernhard Müller on 16/12/2017.
 */

public abstract class RuntimeRunnable<T extends Annotation> implements Runnable {
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