package org.bushe.swing.event.annotation;

import java.util.List;
import java.util.Collection;

/** Test class for class-based subscriptions */
public class AnotherDoubleAnnotatedEventSubcriber {

   static int timesCalled = 0;

   public static int getTimesCalled() {
      return timesCalled;
   }

   public static void setTimesCalled(int times) {
      timesCalled = times;
   }

   @EventSubscriber(eventClass = List.class)
   public void doList(Collection collection) {
      timesCalled++;
   }
}
