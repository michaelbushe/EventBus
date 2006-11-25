package org.bushe.swing.event.annotation;

/**
 * An Annotation for subscribing to EventService Events.
 * <p>
 * This annotation simplifies much of the reptitive boilerplate used for subscribing to EventService Events.
 * <p>
 * Instead of this:
 * <pre>
 * public class MyAppController implements EventSubscriber {
 *   public MyAppController {
 *      EventBus.subscribe(AppClosingEvent.class, this);
 *   }
 *   public void onEvent(EventServiceEvent event) {
 *      AppClosingEvent appClosingEvent = (AppClosingEvent)event;
 *      //do something
 *   }
 * }
 * </pre>
 * You can do this:
 * <pre>
 * public class MyAppController {  //no interface necessary
 *   public MyAppController { //nothing to do in the constructor
 *   }
 *   @EventSubscriber{eventClass=AppClosingEvent.class}
 *   public void onAppClosingEvent(AppClosingEvent appClosingEvent) {//Use your own method name with typesafety
 *      //do something
 *   }
 * }
 * </pre>
 * <p>
 * That's pretty good, but when the constroller does more, annotations are even nicer.
 * <pre>
 * public class MyAppController implements EventSubscriber {
 *   public MyAppController {
 *      EventBus.subscribe(AppStartingEvent.class, this);
 *      EventBus.subscribe(AppClosingEvent.class, this);
 *   }
 *   public void onEvent(EventServiceEvent event) {
 *      //wicked bad pattern, but we have to this
 *      //...or create mutliple subscriber classes and hold instances of them fields, which is even more verbose...
 *      if (event instanceof AppStartingEvent) {
 *         onAppStartingEvent((AppStartingEvent)event);
 *      } else (event instanceof AppClosingEvent) {
 *         onAppStartingEvent((AppClosingEvent)event);
 *      }
 *
 *   }
 *
 *   public void onAppStartingEvent(AppStartingEvent appStartingEvent) {
 *      //do something
 *   }
 *
 *   public void onAppClosingEvent(AppClosingEvent appClosingEvent) {
 *      //do something
 *   }
 * }
 * </pre>
 * You can do this:
 * <pre>
 * public class MyAppController {
 *   public MyAppController {
 *       EventServiceAnnotationTool.enhance(this);//this line can be avoided with a compile-time tool or an Aspect
 *   }
 *   @EventSubscriber{eventClass=AppStartingEvent.class}
 *   public void onAppStartingEvent(AppStartingEvent appStartingEvent) {
 *      //do something
 *   }
 *   @EventSubscriber{eventClass=AppClosingEvent.class}
 *   public void onAppClosingEvent(AppClosingEvent appClosingEvent) {
 *      //do something
 *   }
 * }
 * </pre>
 * Brief, clear, and easy.
 */
/*
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventSubscriber {
    Class eventClass();
    boolean exact() default false;
    ReferenceStrength referenceSemantics() default ReferenceStrength.WEAK;
    String value() default "";
}
*/