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

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestDefaultEventService extends TestCase {

   private ThreadSafeEventService eventService = null;
   private EventHandler eventHandler = null;
   private EventTopicHandler eventTopicHandler;
   private EventHandlerTimingEvent timing;
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

   private EventHandler createEventHandler(boolean throwException) {
      return new HandlerForTest(testCounter, throwException);
   }

   private EventTopicHandler createEventTopicHandler(boolean throwException) {
      return new TopicHandlerForTest(testCounter, throwException);
   }

   private EventHandler createEventHandler(Long waitTime) {
      return new HandlerForTest(testCounter, waitTime);
   }

   private EventHandler getEventHandler() {
      return getEventHandler(true);
   }

   private EventHandler getEventHandler(boolean throwException) {
      if (eventHandler == null) {
         eventHandler = createEventHandler(throwException);
      }
      return eventHandler;
   }

   private EventTopicHandler getEventTopicHandler() {
      if (eventTopicHandler == null) {
         eventTopicHandler = createEventTopicHandler(false);
      }
      return eventTopicHandler;
   }

   public void testSubscribe() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = eventService.subscribe(getEventClass(), handler);
      assertTrue("testSubscribe(new handler)", actualReturn);

      actualReturn = eventService.subscribe(getEventClass(), handler);
      assertFalse("testSubscribe(duplicate handler)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      try {
         actualReturn = eventService.subscribe((Class) null, getEventHandler());
         fail("subscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribe(getEventClass(), null);
         fail("subscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

   }

   public void testSubscribeWeakly() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = eventService.subscribeWeakly(getEventClass(), handler);
      assertTrue("testSubscribeWeakly(new handler)", actualReturn);

      actualReturn = eventService.subscribeWeakly(getEventClass(), handler);
      assertFalse("testSubscribe(duplicate handler)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
      handler = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      try {
         actualReturn = eventService.subscribeWeakly((Class) null, getEventHandler());
         fail("subscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribeWeakly(getEventClass(), null);
         fail("subscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }
   }

   public void testIllegalArgs() {
      try {
         EventBus.subscribeVetoListener((Class) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListener((String) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListener("foo", null);
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListener(getEventClass(), null);
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
      EventHandler handler = createEventHandler(false);

      actualReturn = eventService.subscribe(getEventClass(), handler);

      VetoEventListener vetoListener = new VetoEventListenerForTest();
      actualReturn = eventService.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      eventService.unsubscribeVetoListener(getEventClass(), vetoListener);
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);

   }

   public void testVetoException() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = eventService.subscribe(getEventClass(), handler);

      VetoEventListener vetoListener = new VetoEventListenerForTest(true);
      actualReturn = eventService.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      eventService.unsubscribeVetoListener(getEventClass(), vetoListener);
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 2, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);

   }

   public void testVetoTopic() {
      boolean actualReturn;
      EventTopicHandler handler = createEventTopicHandler(false);

      actualReturn = eventService.subscribe("FooTopic", handler);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListener("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      eventService.unsubscribeVetoListener("FooTopic", vetoListener);
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
   }


   public void testVetoWeak() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = eventService.subscribe(getEventClass(), handler);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListenerWeakly(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      vetoListener = null;
      System.gc();
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
   }

   public void testVetoTopicWeak() {
      boolean actualReturn;
      EventTopicHandler handler = createEventTopicHandler(false);

      actualReturn = eventService.subscribe("FooTopic", handler);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListenerWeakly("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      vetoListener = null;
      System.gc();
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
   }


   public void testUnsubscribe() {
      eventService.subscribe(getEventClass(), getEventHandler(false));

      boolean actualReturn;

      try {
         actualReturn = eventService.unsubscribe((Class) null, getEventHandler());
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.unsubscribe(getEventClass(), null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      actualReturn = eventService.unsubscribe(getEventClass(), getEventHandler());
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
   }

   public void testUnsubscribeTopic() {
      EventTopicHandler eventTopicHandler = createEventTopicHandler(false);
      eventService.subscribe("FooTopic", eventTopicHandler);

      boolean actualReturn;

      try {
         actualReturn = eventService.unsubscribe(null, eventTopicHandler);
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.unsubscribe("FooTopic", null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish("FooTopic", "Foo");

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      actualReturn = eventService.unsubscribe("FooTopic", eventTopicHandler);
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish("FooTopic", "Foo");

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
   }

   /**
    * Test that the publish method works and that execptions thrown in event handlers don't halt publishing. In the
    * test 2 handlers are good and 2 handlers throw exceptions.
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
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      eventService.publish("Foo", "Bar");
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      eventService.subscribe(getEventClass(), createEventHandler(true));
      eventService.subscribe(getEventClass(), createEventHandler(false));
      eventService.subscribe(getEventClass(), createEventHandler(true));
      eventService.subscribe(getEventClass(), createEventHandler(false));

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 2 handlers completed and 2 handlers threw exception.
      assertEquals("testPublish(completed)", 4, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 2, testCounter.handleExceptionCount);

      EventBus.subscribe(ObjectEvent.class, createEventHandler(false));
      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
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
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
   }

   public void testTimeHandling() {
      eventService.subscribe(getEventClass(), createEventHandler(new Long(200L)));
      final Boolean[] wasCalled = new Boolean[1];
      eventService.subscribe(EventHandlerTimingEvent.class, new EventHandler() {
         public void handleEvent(EventServiceEvent evt) {
            wasCalled[0] = Boolean.TRUE;
         }
      });
      eventService.publish(createEvent());
      assertTrue(wasCalled[0] == null);
      eventService = new ThreadSafeEventService(new Long(100), true);
      eventService.subscribe(getEventClass(), createEventHandler(new Long(200L)));
      final Boolean[] wasCalled2 = new Boolean[1];
      eventService.subscribe(EventHandlerTimingEvent.class, new EventHandler() {
         public void handleEvent(EventServiceEvent evt) {
            wasCalled2[0] = Boolean.TRUE;
            timing = (EventHandlerTimingEvent) evt;
         }
      });
      eventService.publish(createEvent());
      assertTrue(wasCalled2[0] == Boolean.TRUE);
      assertNotNull(timing.getSource());
      assertNotNull(timing.getEnd());
      assertNotNull(timing.getEvent());
      assertNotNull(timing.getHandler());
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
    * If a class implements both handler interfaces I've seen a topci 'event' be
    * published from a publish methog with the correct (topic) signature, yet be
    * handled at the wrong handler method (the one with the signature for real event
    * classes, not topics
    */
   public void testSimultaneousTopicAndClass() {
      DoubleHandler doubleHandler = new DoubleHandler();
      eventService.subscribe(org.bushe.swing.event.ObjectEvent.class, doubleHandler);
      eventService.subscribe("org.bushe.swing.event.ObjectEvent.class", doubleHandler);
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      eventService.publish(evt);
      assertEquals(1, doubleHandler.timesEventCalled);
      assertEquals(0, doubleHandler.timesTopicCalled);
      assertEquals(evt, doubleHandler.lastEvent);
      assertEquals(null, doubleHandler.lastEventString);
      eventService.publish("org.bushe.swing.event.ObjectEvent.class", "Bar");
      assertEquals(1, doubleHandler.timesEventCalled);
      assertEquals(1, doubleHandler.timesTopicCalled);
      assertEquals(evt, doubleHandler.lastEvent);
      assertEquals("org.bushe.swing.event.ObjectEvent.class", doubleHandler.lastEventString);
   }

   class DoubleHandler implements EventTopicHandler, EventHandler {
      public int timesTopicCalled = 0;
      public int timesEventCalled = 0;
      public String lastEventString;
      public EventServiceEvent lastEvent;

      public void handleEvent(String topic, Object data) {
         timesTopicCalled++;
         lastEventString = topic;
      }

      public void handleEvent(EventServiceEvent evt) {
         timesEventCalled++;
         lastEvent = evt;
      }
   }

}
