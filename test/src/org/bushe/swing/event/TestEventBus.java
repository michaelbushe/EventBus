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
public class TestEventBus extends TestCase {

   private EventHandler eventHandler = null;
   private EventTopicHandler eventTopicHandler;
   private EventHandlerTimingEvent timing;
   private EBTestCounter testCounter = new EBTestCounter();

   public TestEventBus(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      EventBus.getGlobalEventService().clearAllSubscribers();
   }

   protected void tearDown() throws Exception {
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

      actualReturn = EventBus.subscribe(getEventClass(), handler);
      assertTrue("testSubscribe(new handler)", actualReturn);

      actualReturn = EventBus.subscribe(getEventClass(), handler);
      assertFalse("testSubscribe(duplicate handler)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      try {
         actualReturn = EventBus.subscribe((Class) null, getEventHandler());
         fail("subscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.subscribe(getEventClass(), null);
         fail("subscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

   }

   private void waitForEDT() {
      try {
         Thread.sleep(500);
      } catch (Throwable e){
      }
   }

   public void testSubscribeWeakly() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = EventBus.subscribeWeakly(getEventClass(), handler);
      assertTrue("testSubscribeWeakly(new handler)", actualReturn);

      actualReturn = EventBus.subscribeWeakly(getEventClass(), handler);
      assertFalse("testSubscribe(duplicate handler)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
      handler = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      try {
         actualReturn = EventBus.subscribeWeakly((Class) null, getEventHandler());
         fail("subscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.subscribeWeakly(getEventClass(), null);
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

      actualReturn = EventBus.subscribe(getEventClass(), handler);

      VetoEventListener vetoListener = new VetoEventListenerForTest();
      actualReturn = EventBus.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      EventBus.unsubscribeVetoListener(getEventClass(), vetoListener);
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);

   }

   public void testVetoException() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = EventBus.subscribe(getEventClass(), handler);

      VetoEventListener vetoListener = new VetoEventListenerForTest(true);
      actualReturn = EventBus.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      EventBus.unsubscribeVetoListener(getEventClass(), vetoListener);
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 2, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);

   }

   public void testVetoTopic() {
      boolean actualReturn;
      EventTopicHandler handler = createEventTopicHandler(false);

      actualReturn = EventBus.subscribe("FooTopic", handler);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = EventBus.subscribeVetoListener("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish("FooTopic", createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      EventBus.unsubscribeVetoListener("FooTopic", vetoListener);
      EventBus.publish("FooTopic", createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
   }


   public void testVetoWeak() {
      boolean actualReturn;
      EventHandler handler = createEventHandler(false);

      actualReturn = EventBus.subscribe(getEventClass(), handler);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = EventBus.subscribeVetoListenerWeakly(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      vetoListener = null;
      System.gc();
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
   }

   public void testVetoTopicWeak() {
      boolean actualReturn;
      EventTopicHandler handler = createEventTopicHandler(false);

      actualReturn = EventBus.subscribe("FooTopic", handler);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(EventServiceEvent evt) {
            return true;
         }
      };
      actualReturn = EventBus.subscribeVetoListenerWeakly("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish("FooTopic", createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
      vetoListener = null;
      System.gc();
      EventBus.publish("FooTopic", createEvent());
      waitForEDT();
      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.handleExceptionCount);
   }


   public void testUnsubscribe() {
      EventBus.subscribe(getEventClass(), getEventHandler(false));

      boolean actualReturn;

      try {
         actualReturn = EventBus.unsubscribe((Class) null, getEventHandler());
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.unsubscribe(getEventClass(), null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      actualReturn = EventBus.unsubscribe(getEventClass(), getEventHandler());
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
   }

   public void testUnsubscribeTopic() {
      EventTopicHandler eventTopicHandler = createEventTopicHandler(false);
      EventBus.subscribe("FooTopic", eventTopicHandler);

      boolean actualReturn;

      try {
         actualReturn = EventBus.unsubscribe(null, eventTopicHandler);
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.unsubscribe("FooTopic", null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish("FooTopic", "Foo");
      waitForEDT();

      //The test passes if 1 handlers completed and 0 handlers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      actualReturn = EventBus.unsubscribe("FooTopic", eventTopicHandler);
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish("FooTopic", "Foo");
      waitForEDT();

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
         EventBus.publish(null);
         waitForEDT();
         fail("publish(null) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         EventBus.publish((String) null, createEvent());
         waitForEDT();
         fail("publish(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      EventBus.publish(createEvent());
      waitForEDT();
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      EventBus.publish("Foo", "Bar");
      waitForEDT();
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);

      EventBus.subscribe(getEventClass(), createEventHandler(true));
      EventBus.subscribe(getEventClass(), createEventHandler(false));
      EventBus.subscribe(getEventClass(), createEventHandler(true));
      EventBus.subscribe(getEventClass(), createEventHandler(false));

      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      EventBus.publish(createEvent());
      waitForEDT();

      //The test passes if 2 handlers completed and 2 handlers threw exception.
      assertEquals("testPublish(completed)", 4, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 2, testCounter.handleExceptionCount);

      EventBus.subscribe(ObjectEvent.class, createEventHandler(false));
      testCounter.eventsHandledCount = 0;
      testCounter.handleExceptionCount = 0;
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      EventBus.publish(evt);
      waitForEDT();
      //Since we are using hte event bus from a non-awt thread, stay alive for a sec
      //to give time for the EDT to start and post the message
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      assertEquals("testPublish(completed)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.handleExceptionCount);
   }

}
