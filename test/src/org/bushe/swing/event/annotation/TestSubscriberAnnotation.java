package org.bushe.swing.event.annotation;

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

public class TestSubscriberAnnotation extends TestCase {

   public void setUp() {
      EventBus.getGlobalEventService();
      EventBus.clearAllSubscribers();
      AnnotatedEventSubcriber.setTimesCalled(0);
      AnnotatedEventSubcriber.setLastCall(null);
      System.gc();
   }

   public void testSimple() throws InvocationTargetException, InterruptedException {
      AnnotatedEventSubcriber.setTimesColorChanged(0);
      final AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      EventBus.publish(Color.BLUE);
      Collection subs = EventBus.getSubscribers(Color.class);
      assertEquals(0, subs.size());
      waitForEDT();
      assertEquals(0, AnnotatedEventSubcriber.getTimesColorChanged());
      SwingUtilities.invokeAndWait(new Runnable() {
         public void run() {
            AnnotationProcessor.process(subscriber);
         }
      });

//@TODO WHy does this pass in IDEA, but fail on the command line?  The rest passes, strange.
//      subs = EventBus.getSubscribers(Color.class);
//      assertEquals(1, subs.size());
//      EventBus.publish(Color.BLUE);
//      waitForEDT();
//      assertEquals(1, AnnotatedEventSubcriber.getTimesColorChanged());
   }

   public void testWeakReference() {
      AnnotatedEventSubcriber.setTimesColorChanged(0);
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      EventBus.publish(Color.BLUE);
      waitForEDT();
      assertEquals(0, AnnotatedEventSubcriber.getTimesColorChanged());
      AnnotationProcessor.process(subscriber);
      EventBus.publish(Color.BLUE);
      waitForEDT();
      assertEquals(1, AnnotatedEventSubcriber.getTimesColorChanged());
      subscriber = null;
      System.gc();
      EventBus.publish(Color.BLUE);
      waitForEDT();
      assertEquals(1, AnnotatedEventSubcriber.getTimesColorChanged());
      System.gc();
   }

   public void testEventClass() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals("doList", AnnotatedEventSubcriber.getLastCall());
      AnnotatedEventSubcriber.setLastCall(null);
      //it was subscribed to a list, though the method param is Collection, it shouldn't get called
      EventBus.publish(new HashSet());
      waitForEDT();
      assertEquals(null, AnnotatedEventSubcriber.getLastCall());
   }

   public void testExactly() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish(new JToggleButton());
      waitForEDT();
      assertEquals("doJToggleButtonExactly", AnnotatedEventSubcriber.getLastCall());
      assertEquals(1, AnnotatedEventSubcriber.getTimesCalled());
      EventBus.publish(new JButton());
      waitForEDT();
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
      waitForEDT();
      assertEquals("doStrong", StrongAnnotatedEventSubscriber.getLastCall());
      assertEquals(1, StrongAnnotatedEventSubscriber.getTimesCalled());
      System.gc();
      EventBus.publish(new File("foo"));
      waitForEDT();
      assertEquals("doStrong", StrongAnnotatedEventSubscriber.getLastCall());
      assertEquals(2, StrongAnnotatedEventSubscriber.getTimesCalled());
   }

   public void testTopic() {
      AnnotatedEventSubcriber subscriber = new AnnotatedEventSubcriber();
      AnnotationProcessor.process(subscriber);
      EventBus.publish("File.Open", new File("foo"));
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

   public void testAnotherIssue15MultipleAnnotatedSubscribers() {
      EventBus.clearAllSubscribers();
      System.gc();
      Issue15Subscriber i15s1 = new Issue15Subscriber();
      Issue15Subscriber2 i15s2 = new Issue15Subscriber2();
      EventBus.publish(new ArrayList());
      waitForEDT();
      assertEquals(1, i15s2.getTimesCalled());
      assertEquals(1, i15s1.getTimesCalled());
      //Ensure the garbage collector can't clean up the refs
      System.out.println(i15s1);
      System.out.println(i15s2);
   }

   //This one works with the DoubleAnnotatedEventSubcriber and AnotherDoubleAnnotatedEventSubcriber (and Single),
   //but fails with AnnotatedEventSubcriber and AnotherAnnotatedEventSubcriber
   public void testIssue15MultipleAnnotatedSubscribers() {
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
      waitForEDT();
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

   /**
    * Since we are using the event bus from a non-awt thread, stay alive for a sec to give time for the EDT to start and
    * post the message
    */
   private void waitForEDT() {
      try {
         Thread.sleep(1000);
      } catch (Throwable e) {
      }
   }

}
