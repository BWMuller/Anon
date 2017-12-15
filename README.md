# Anon
Annotation powered analytics.

# Credit
This library was based of JakeWarton's Hugo library https://github.com/JakeWharton/hugo, which is a nice library to get started with when creating a gradle plugin or working with aspectj

# Usage
Include this in your app
implementation 'za.co.bwmuller.anon:anon-runtime:1.0.0-SNAPSHOT'

Add @AnonClass to any class that required trackable methods.
Add @AnonMethod to any method within such a class in order to receive a callback when the method has finished.

A callback class can be added during runtime using Anon.setCallback

# Advanced Usage
Create your own trackable annotation by adding @trackable to your annotation. This annotation is still needs to be using inside a class that is annotated to be trackable as well (@AnonClass)
```
@Trackable
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomMethodTrack {
    String value() default "grey";
}
```
