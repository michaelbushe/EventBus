package org.bushe.swing.event.annotation;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.awt.Color;
import javax.swing.JToggleButton;
import javax.swing.JComponent;

import org.bushe.swing.event.ThreadSafeEventService;

/**
 * Test class for class-based subscriptions
 */
public class StrongClassAnnotatedEventSubcriber {
   static int timesColorChanged = 0;
   static String lastCall = null;
   static int timesCalled = 0;

   public static int getTimesCalled() {
      return timesCalled;
   }

   public static void setTimesCalled(int times) {
      timesCalled = times;
   }

   @EventSubscriber(eventClass=List.class, referenceStrength = ReferenceStrength.STRONG)
   public void doList(Collection collection) {
      timesCalled++;
   }
}
