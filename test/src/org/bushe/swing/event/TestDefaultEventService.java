/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bushe.swing.event;

import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestDefaultEventService extends TestCase {

   private ThreadSafeEventService eventService = null;
   private EventSubscriber eventSubscriber = null;
   private EventTopicSubscriber eventTopicSubscriber;
   private SubscriberTimingEvent timing;
   private EBTestCounter testCounter = new EBTestCounter();

   public TestDefaultEventService(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      eventService = new ThreadSafeEventService(null, false);
   }

   protected void tearDown() throws Exception {
      eventService = null;
   }

   private EventServiceEvent createEvent() {
      return new EventServiceEvent() {
         public Object getSource() {
            return "";
         }
      };
   }

   private Class getEventClass() {
      return createEvent().getClass();
   }

   private EventSubscriber createEventSubscriber(boolean throwException) {
      return new SubscriberForTest(testCounter, throwException);
   }

   private EventTopicSubscriber createEventTopicSubscriber(boolean throwException) {
      return new TopicSubscriberForTest(testCounter, throwException);
   }

   private EventSubscriber createEventSubscriber(Long waitTime) {
      return new SubscriberForTest(testCounter, waitTime);
   }

   private EventSubscriber getEventSubscriber() {
      return getEventSubscriber(true);
   }

   private EventSubscriber getEventSubscriber(boolean throwException) {
      if (eventSubscriber == null) {
         eventSubscriber = createEventSubscriber(throwException);
      }
      return eventSubscriber;
   }

   private EventTopicSubscriber getEventTopicSubscriber() {
      if (eventTopicSubscriber == null) {
         eventTopicSubscriber = createEventTopicSubscriber(false);
      }
      return eventTopicSubscriber;
   }

   public void testSubscribe() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertTrue("testSubscribe(new subscriber)", actualReturn);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = eventService.subscribe((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribe(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }

   }

   public void testSubscribeOrder() {
      boolean actualReturn;
      SubscriberForTest subscriber1 = (SubscriberForTest) createEventSubscriber(new Long(100));
      SubscriberForTest subscriber2 = (SubscriberForTest) createEventSubscriber(new Long(100));
      SubscriberForTest subscriber3 = (SubscriberForTest) createEventSubscriber(new Long(100));

      actualReturn = eventService.subscribe(getEventClass(), subscriber1);
      actualReturn = eventService.subscribe(getEventClass(), subscriber2);
      actualReturn = eventService.subscribe(getEventClass(), subscriber3);

      eventService.publish(createEvent());

      assertTrue(subscriber1.callTime.before(subscriber2.callTime));
      assertTrue(subscriber2.callTime.before(subscriber3.callTime));

      actualReturn = eventService.subscribe(getEventClass(), subscriber1);
      eventService.publish(createEvent());

      assertTrue(subscriber2.callTime.before(subscriber3.callTime));
      assertTrue(subscriber3.callTime.before(subscriber1.callTime));

      List subscribers = eventService.getSubscribers(getEventClass());
      assertEquals(3, subscribers.size());
      for (int i = 0; i < subscribers.size(); i++) {
         EventSubscriber subscriber = (EventSubscriber) subscribers.get(i);
         eventService.unsubscribe(getEventClass(),subscriber);
      }
      eventService.subscribe(getEventClass(), (EventSubscriber) subscribers.get(1));
      eventService.subscribe(getEventClass(), (EventSubscriber) subscribers.get(0));
      eventService.subscribe(getEventClass(), (EventSubscriber) subscribers.get(2));
      eventService.publish(createEvent());
      assertTrue(subscriber3.callTime.before(subscriber2.callTime));
      assertTrue(subscriber2.callTime.before(subscriber1.callTime));
   }

   public void testSubscribeWeakly() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertTrue("testSubscribeWeakly(new subscriber)", actualReturn);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
      subscriber = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = eventService.subscribeStrongly((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribeStrongly(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }
   }

   public void testSubscribeStrongly() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribeStrongly(getEventClass(), subscriber);
      assertTrue("testSubscribeWeakly(new subscriber)", actualReturn);

      actualReturn = eventService.subscribeStrongly(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
      subscriber = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = eventService.subscribeStrongly((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribeStrongly(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }
   }


   public void testIllegalArgs() {
      try {
         EventBus.subscribeVetoListenerStrongly((Class) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListenerStrongly((String) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListenerStrongly("foo", null);
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListenerStrongly(getEventClass(), null);
         fail();
      } catch (Throwable t) {
      }


      try {
         EventBus.unsubscribeVetoListener((Class) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.unsubscribeVetoListener((String) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.unsubscribeVetoListener("foo", null);
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.unsubscribeVetoListener(getEventClass(), null);
         fail();
      } catch (Throwable t) {
      }

   }

   public void testVeto() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListenerForTest();
      actualReturn = eventService.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      eventService.unsubscribeVetoListener(getEventClass(), vetoListener);
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);

   }

   public void testVetoException() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListenerForTest(true);
      actualReturn = eventService.subscribeVetoListenerStrongly(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      eventService.unsubscribeVetoListener(getEventClass(), vetoListener);
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 2, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);

   }

   public void testVetoTopic() {
      boolean actualReturn;
      EventTopicSubscriber subscriber = createEventTopicSubscriber(false);

      actualReturn = eventService.subscribe("FooTopic", subscriber);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListenerStrongly("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      eventService.unsubscribeVetoListener("FooTopic", vetoListener);
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }


   public void testVetoWeak() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      vetoListener = null;
      System.gc();
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testVetoTopicWeak() {
      boolean actualReturn;
      EventTopicSubscriber subscriber = createEventTopicSubscriber(false);

      actualReturn = eventService.subscribe("FooTopic", subscriber);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListener("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      vetoListener = null;
      System.gc();
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }


   public void testUnsubscribe() {
      eventService.subscribe(getEventClass(), getEventSubscriber(false));

      boolean actualReturn;

      try {
         actualReturn = eventService.unsubscribe((Class) null, getEventSubscriber());
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.unsubscribe(getEventClass(), null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      actualReturn = eventService.unsubscribe(getEventClass(), getEventSubscriber());
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testUnsubscribeTopic() {
      EventTopicSubscriber eventTopicSubscriber = createEventTopicSubscriber(false);
      eventService.subscribe("FooTopic", eventTopicSubscriber);

      boolean actualReturn;

      try {
         actualReturn = eventService.unsubscribe((String) null, eventTopicSubscriber);
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.unsubscribe("FooTopic", null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", "Foo");

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      actualReturn = eventService.unsubscribe("FooTopic", eventTopicSubscriber);
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", "Foo");

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   /**
    * Test that the publish method works and that execptions thrown in event subscribers don't halt publishing. In the
    * test 2 subscribers are good and 2 subscribers throw exceptions.
    */
   public void testPublish() {
      try {
         eventService.publish(null);
         fail("publish(null) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         eventService.publish((String) null, createEvent());
         fail("publish(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      eventService.publish(createEvent());
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      eventService.publish("Foo", "Bar");
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      eventService.subscribe(getEventClass(), createEventSubscriber(true));
      eventService.subscribe(getEventClass(), createEventSubscriber(false));
      eventService.subscribe(getEventClass(), createEventSubscriber(true));
      eventService.subscribe(getEventClass(), createEventSubscriber(false));

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 2 subscribers completed and 2 subscribers threw exception.
      assertEquals("testPublish(completed)", 4, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 2, testCounter.subscribeExceptionCount);

      EventBus.subscribe(ObjectEvent.class, createEventSubscriber(false));
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      EventBus.publish(evt);
      //Since we are using hte event bus from a non-awt thread, stay alive for a sec
      //to give time for the EDT to start and post the message
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      assertEquals("testPublish(completed)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testTimeHandling() {
      eventService.subscribe(getEventClass(), createEventSubscriber(new Long(200L)));
      final Boolean[] wasCalled = new Boolean[1];
      eventService.subscribe(SubscriberTimingEvent.class, new EventSubscriber() {
         public void onEvent(EventServiceEvent evt) {
            wasCalled[0] = Boolean.TRUE;
         }
      });
      eventService.publish(createEvent());
      assertTrue(wasCalled[0] == null);
      eventService = new ThreadSafeEventService(new Long(100), true);
      eventService.subscribe(getEventClass(), createEventSubscriber(new Long(200L)));
      final Boolean[] wasCalled2 = new Boolean[1];
      eventService.subscribe(SubscriberTimingEvent.class, new EventSubscriber() {
         public void onEvent(EventServiceEvent evt) {
            wasCalled2[0] = Boolean.TRUE;
            timing = (SubscriberTimingEvent) evt;
         }
      });
      eventService.publish(createEvent());
      assertTrue(wasCalled2[0] == Boolean.TRUE);
      assertNotNull(timing.getSource());
      assertNotNull(timing.getEnd());
      assertNotNull(timing.getEvent());
      assertNotNull(timing.getSubscriber());
      assertNotNull(timing.getStart());
      assertNotNull(timing.getTimeLimitMilliseconds());
      assertFalse(timing.isEventHandlingExceeded());
      assertFalse(timing.isVetoExceeded());
      assertNull(timing.getVetoEventListener());
   }

   public void testEventLocator() {
      EventService es = EventServiceLocator.getSwingEventService();
      assertTrue(es instanceof SwingEventService);
      es = new ThreadSafeEventService(null, false);
      EventServiceLocator.setEventService("foo", es);
      EventService es2 = EventServiceLocator.getEventService("foo");
      assertTrue(es2 == es);
      EventServiceLocator.setEventService("foo", null);
      es2 = EventServiceLocator.getEventService("foo");
      assertNull(es2);
      assertEquals(EventServiceLocator.getSwingEventService(), EventBus.getGlobalEventService());
   }

   /**
    * Test for ISSUE #1:
    * If a class implements both subscriber interfaces I've seen a topci 'event' be
    * published from a publish methog with the correct (topic) signature, yet be
    * subscribed at the wrong subscriber method (the one with the signature for real event
    * classes, not topics
    */
   public void testSimultaneousTopicAndClass() {
      DoubleSubscriber doubleSubscriber = new DoubleSubscriber();
      eventService.subscribe(org.bushe.swing.event.ObjectEvent.class, doubleSubscriber);
      eventService.subscribe("org.bushe.swing.event.ObjectEvent.class", doubleSubscriber);
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      eventService.publish(evt);
      assertEquals(1, doubleSubscriber.timesEventCalled);
      assertEquals(0, doubleSubscriber.timesTopicCalled);
      assertEquals(evt, doubleSubscriber.lastEvent);
      assertEquals(null, doubleSubscriber.lastEventString);
      eventService.publish("org.bushe.swing.event.ObjectEvent.class", "Bar");
      assertEquals(1, doubleSubscriber.timesEventCalled);
      assertEquals(1, doubleSubscriber.timesTopicCalled);
      assertEquals(evt, doubleSubscriber.lastEvent);
      assertEquals("org.bushe.swing.event.ObjectEvent.class", doubleSubscriber.lastEventString);
   }

   public void testRegex() {
      DoubleSubscriber doubleSubscriber = new DoubleSubscriber();
      Pattern pat = Pattern.compile("Foo[1-5]");
      eventService.subscribe(pat, doubleSubscriber);
      List subscribers = eventService.getSubscribersToPattern(pat);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribersByPattern("Foo1");
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribers("Foo1");
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());

      eventService.publish("Foo1", "Bar");
      assertEquals(0, doubleSubscriber.timesEventCalled);
      assertEquals(1, doubleSubscriber.timesTopicCalled);
      assertEquals(null, doubleSubscriber.lastEvent);
      assertEquals("Foo1", doubleSubscriber.lastEventString);
      eventService.publish("Foo2", "Bar");
      assertEquals(0, doubleSubscriber.timesEventCalled);
      assertEquals(2, doubleSubscriber.timesTopicCalled);
      assertEquals(null, doubleSubscriber.lastEvent);
      assertEquals("Foo2", doubleSubscriber.lastEventString);
   }

   public void testTypeSubscription() {
      DoubleSubscriber subscriber = new DoubleSubscriber();

      eventService.subscribe(TopLevelEvent.class, subscriber);
      List subscribers = eventService.getSubscribersToType(TopLevelEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribersToType(DerivedEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribers(DerivedEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribers(TopLevelEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());

      DerivedEvent derivedEvent = new DerivedEvent(this);
      eventService.publish(derivedEvent);
      assertEquals(1, subscriber.timesEventCalled);
      assertEquals(0, subscriber.timesTopicCalled);
      assertEquals(derivedEvent, subscriber.lastEvent);
      assertEquals(null, subscriber.lastEventString);
      TopLevelEvent topLevelEvent = new TopLevelEvent(this);
      eventService.publish(topLevelEvent);
      assertEquals(2, subscriber.timesEventCalled);
      assertEquals(0, subscriber.timesTopicCalled);
      assertEquals(topLevelEvent, subscriber.lastEvent);
      assertEquals(null, subscriber.lastEventString);
   }

   class DoubleSubscriber implements EventTopicSubscriber, EventSubscriber {
      public int timesTopicCalled = 0;
      public int timesEventCalled = 0;
      public String lastEventString;
      public EventServiceEvent lastEvent;

      public void onEvent(String topic, Object data) {
         timesTopicCalled++;
         lastEventString = topic;
      }

      public void onEvent(EventServiceEvent evt) {
         timesEventCalled++;
         lastEvent = evt;
      }
   }

   class TopLevelEvent extends AbstractEventServiceEvent {
      public TopLevelEvent(Object source) {
         super(source);
      }
   }
   class DerivedEvent extends TopLevelEvent {
      public DerivedEvent(Object source) {
         super(source);
      }
   }
}
