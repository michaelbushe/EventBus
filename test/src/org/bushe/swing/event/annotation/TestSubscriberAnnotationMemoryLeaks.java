package org.bushe.swing.event.annotation;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.File;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.JToggleButton;
import javax.swing.JButton;

import junit.framework.TestCase;

import org.bushe.swing.event.*;
import org.bushe.swing.event.EventSubscriber;

public class TestSubscriberAnnotationMemoryLeaks extends TestCase {

   public void setUp() {
      EventBus.getGlobalEventService();
      EventBus.clearAllSubscribers();
      System.gc();
   }

   public void testStrongClassAnnotatedEventSubcriber() {
      StrongClassAnnotatedEventSubcriber subscriber = new StrongClassAnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      StrongClassAnnotatedEventSubcriber.setTimesCalled(0);
      assertEquals(0, StrongClassAnnotatedEventSubcriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(1, StrongClassAnnotatedEventSubcriber.getTimesCalled());
      subscriber = null;
      System.gc();
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(2, StrongClassAnnotatedEventSubcriber.getTimesCalled());
      List subscribers = EventBus.getSubscribers(List.class);
      assertEquals(1, subscribers.size());
      //I can unsubscribe without ever explicity subscribing
      EventBus.unsubscribe(List.class, (org.bushe.swing.event.EventSubscriber) subscribers.get(0));
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(2, StrongClassAnnotatedEventSubcriber.getTimesCalled());
      subscribers = EventBus.getSubscribers(List.class);
      assertEquals(0, subscribers.size());
   }

   public void testWeakClassAnnotatedEventSubcriber() {
      WeakClassAnnotatedEventSubcriber subscriber = new WeakClassAnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      WeakClassAnnotatedEventSubcriber.setTimesCalled(0);
      assertEquals(0, WeakClassAnnotatedEventSubcriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubcriber.getTimesCalled());

      subscriber = null;
      System.gc();
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubcriber.getTimesCalled());
      List subscribers = EventBus.getSubscribers(List.class);
      assertEquals(0, subscribers.size());
   }

   public void testWeakClassAnnotatedEventSubcriberUnsubscription() {
      WeakClassAnnotatedEventSubcriber subscriber = new WeakClassAnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      WeakClassAnnotatedEventSubcriber.setTimesCalled(0);
      assertEquals(0, WeakClassAnnotatedEventSubcriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubcriber.getTimesCalled());

      EventBus.unsubscribe(List.class, subscriber);

      subscriber = null;
      System.gc();
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubcriber.getTimesCalled());
      List subscribers = EventBus.getSubscribers(List.class);
      assertEquals(0, subscribers.size());
   }

   /**
    * Since we are using the event bus from a non-awt thread, stay alive for a sec
    * to give time for the EDT to start and post the message
    */
   private void waitForEDT() {
      try {
         Thread.sleep(1000);
      } catch (Throwable e){
      }
   }

}
