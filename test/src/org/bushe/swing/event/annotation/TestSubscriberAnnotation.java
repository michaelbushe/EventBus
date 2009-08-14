package org.bushe.swing.event.annotation;

import org.bushe.swing.event.EDTUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.EventServiceLocatorTestCase;

import org.bushe.swing.event.annotation.runtime.Factory;
import org.bushe.swing.event.annotation.runtime.SubscriberForTesting;

public class TestSubscriberAnnotation extends TestCase {

   @Override
   public void setUp() {
      EventServiceLocatorTestCase.clearEventServiceLocator();
      EventBus.getGlobalEventService();
      EventBus.clearAllSubscribers();
      AnnotatedEventSubcriber.setTimesCalled(0);
      AnnotatedEventSubcriber.setLastCall(null);
      System.gc();
   }

   protected void tearDown() throws Exception {
      EventServiceLocatorTestCase.clearEventServiceLocator();      
   }

   public void testSimple() throws InvocationTargetException, InterruptedException {
      AnnotatedEventSubcriber.setTimesColorChanged(0);
      final AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      EventBus.publish(Color.BLUE);
      Collection subs = EventBus.getSubscribers(Color.class);
      assertEquals(0, subs.size());
      EDTUtil.waitForEDT();
      assertEquals(0, AnnotatedEventSubcriber.getTimesColorChanged());
      SwingUtilities.invokeAndWait(new Runnable() {
         public void run() {
            AnnotationProcessor.process(subscriber);
         }
      });

      subs = EventBus.getSubscribers(Color.class);
      assertEquals(1, subs.size());
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubcriber.getTimesColorChanged());
      System.out.println("avoid garbage collection:"+subscriber);
   }

   public void testWeakReference() {
      AnnotatedEventSubcriber.setTimesColorChanged(0);
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(0, AnnotatedEventSubcriber.getTimesColorChanged());
      AnnotationProcessor.process(subscriber);
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubcriber.getTimesColorChanged());
      System.out.println("avoid garbage collection:"+subscriber);
      subscriber = null;
      System.gc();
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubcriber.getTimesColorChanged());
      System.gc();
   }

   public void testEventClass() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals("doList", AnnotatedEventSubcriber.getLastCall());
      System.out.println("avoid garbage collection:"+subscriber);
      AnnotatedEventSubcriber.setLastCall(null);
      //it was subscribed to a list, though the method param is Collection, it shouldn't get called
      EventBus.publish(new HashSet());
      EDTUtil.waitForEDT();
      assertEquals(null, AnnotatedEventSubcriber.getLastCall());
   }

   public void testExactly() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish(new JToggleButton());
      EDTUtil.waitForEDT();
      System.out.println("avoid garbage collection:"+subscriber);
      assertEquals("doJToggleButtonExactly", AnnotatedEventSubcriber.getLastCall());
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
      EventBus.publish(new JButton());
      EDTUtil.waitForEDT();
      assertEquals("doJToggleButtonExactly", AnnotatedEventSubcriber.getLastCall());
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
   }

   public void testAutoCreateEventServiceClass() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventService es = EventServiceLocator.getEventService("IteratorService");
      es.publish(new ArrayList().iterator());
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
      assertEquals("autoCreateEventServiceClass", AnnotatedEventSubcriber.getLastCall());
   }

   public void testStrongRef() {
      StrongAnnotatedEventSubscriber subscriber = new StrongAnnotatedEventSubscriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish(new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals("doStrong", StrongAnnotatedEventSubscriber.getLastCall());
      assertEquals(1, StrongAnnotatedEventSubscriber.getTimesCalled());
      System.gc();
      EventBus.publish(new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals("doStrong", StrongAnnotatedEventSubscriber.getLastCall());
      assertEquals(2, StrongAnnotatedEventSubscriber.getTimesCalled());
   }

   public void testTopic() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish("File.Open", new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
      EventBus.publish("File.Fooooooo", new File("foo"));
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
   }

   public void testAutoCreateEventServiceTopic() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventService es = EventServiceLocator.getEventService("IteratorService");
      es.publish("Iterator", new ArrayList().iterator());
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
      assertEquals("autoCreateEventServiceClass", AnnotatedEventSubcriber.getLastCall());
   }

   public void testTopicPattern() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventService es = EventServiceLocator.getEventService("IceCreamService");
      es.publish("IceCream.Chocolate", "DoubleDip");
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
      assertEquals("doIceCream", AnnotatedEventSubcriber.getLastCall());
      System.out.println(subscriber);
   }

   public void testIssue15MultipleAnnotatedSubscribers() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      AnotherAnnotatedEventSubcriber anotherAubscriber = new AnotherAnnotatedEventSubcriber();
      AnnotationProcessor.process(anotherAubscriber);
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, AnotherAnnotatedEventSubcriber.getTimesCalled());
      EDTUtil.waitForEDT();
      System.out.println(subscriber);
      System.out.println(anotherAubscriber);
   }

   public void testAnotherIssue15MultipleAnnotatedSubscribers() {
      EventBus.clearAllSubscribers();
      System.gc();
      Issue15Subscriber i15s1 = new Issue15Subscriber();
      Issue15Subscriber2 i15s2 = new Issue15Subscriber2();
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, i15s2.getTimesCalled());
      assertEquals(1, i15s1.getTimesCalled());
      //Ensure the garbage collector can't clean up the refs
      System.out.println(i15s1);
      System.out.println(i15s2);
   }

   //This one works with the DoubleAnnotatedEventSubcriber and AnotherDoubleAnnotatedEventSubcriber (and Single),
   //but fails with AnnotatedEventSubcriber and AnotherAnnotatedEventSubcriber
   public void testYetAnotherIssue15MultipleAnnotatedSubscribers() {
      EventBus.clearAllSubscribers();
      System.gc();
      DoubleAnnotatedEventSubcriber subscriber = new DoubleAnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      DoubleAnnotatedEventSubcriber secondSubscriber = new DoubleAnnotatedEventSubcriber();
      AnnotationProcessor.process(secondSubscriber);
      AnotherDoubleAnnotatedEventSubcriber anotherSubscriber = new AnotherDoubleAnnotatedEventSubcriber();
      AnnotationProcessor.process(anotherSubscriber);
      AnotherDoubleAnnotatedEventSubcriber secondAnotherSubscriber = new AnotherDoubleAnnotatedEventSubcriber();
      AnnotationProcessor.process(secondAnotherSubscriber);
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(2, AnotherDoubleAnnotatedEventSubcriber.getTimesCalled());
      assertEquals(2, DoubleAnnotatedEventSubcriber.getTimesCalled());
      //Ensure the garbage collector can't clean up the refs
      System.out.println("finished with:"+subscriber);
      System.out.println("finished with:"+secondSubscriber);
      System.out.println("finished with:"+anotherSubscriber);
   }

//Would like to test this, but an exception isn't thrown, since you want all the subscribers to be called
//even if calling any one throws an exception
//   public void testTopicWrongType() {
//      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
//      AnnotationProcessor.process(subscriber);
//      EventService es = EventServiceLocator.getEventService("IteratorService");
//      try {
//         es.publish("Iterator", "foo");
//         fail("Should get an IllegalArgumentException");
//      } catch (Exception ex) {
//      }
//   }      

   public void testRuntimeTopicSubscriber() {
	   SubscriberForTesting runtimeTopicSubcriber = Factory.newRuntimeTopicSubcriber("foo");
	   EventBus.publish("foo", new ArrayList<String>());
	   EDTUtil.waitForEDT();
	   assertEquals(1, runtimeTopicSubcriber.getTimesCalled());
   }

   public void testRuntimeTopicPatternSubscriber() {
	   SubscriberForTesting runtimeTopicSubcriber = Factory.newRuntimeTopicPatternSubscriber("hope.*");
	   EventBus.publish("hope_and_change", new ArrayList<String>());
	   EDTUtil.waitForEDT();
	   assertEquals(1, runtimeTopicSubcriber.getTimesCalled());
   }
}
