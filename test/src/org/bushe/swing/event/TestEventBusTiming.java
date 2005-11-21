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
public class TestEventBusTiming extends TestCase {

   private EventHandler eventHandler = null;
   private EventTopicHandler eventTopicHandler;
   private EventHandlerTimingEvent timing;
   private EBTestCounter testCounter = new EBTestCounter();

   public TestEventBusTiming(String name) {
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

   public void testNothing() {
      
   }

   public void broker_timeHandling() {
      EventBus.subscribe(getEventClass(), createEventHandler(new Long(200L)));
      final Boolean[] wasCalled = new Boolean[1];
      EventBus.subscribe(EventHandlerTimingEvent.class, new EventHandler() {
         public void handleEvent(EventServiceEvent evt) {
            wasCalled[0] = Boolean.TRUE;
         }
      });
      EventBus.publish(createEvent());
      try {
         Thread.sleep(500);
      } catch (Throwable e){
      }
      assertTrue(wasCalled[0] == null);
      EventBus.subscribe(getEventClass(), createEventHandler(new Long(200L)));
      final Boolean[] wasCalled2 = new Boolean[1];
      EventBus.subscribe(EventHandlerTimingEvent.class, new EventHandler() {
         public void handleEvent(EventServiceEvent evt) {
            wasCalled2[0] = Boolean.TRUE;
            timing = (EventHandlerTimingEvent) evt;
         }
      });
      EventBus.publish(createEvent());
      try {
         Thread.sleep(3000);
      } catch (Throwable e){
      }
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

}
