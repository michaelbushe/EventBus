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

/**
 * The EventBus provides Swing event publication and subscription services.  It is a simple static wrapper around
 * a global instance of an {@link EventService}, specifically a {@link SwingEventService}.
 * <p>
 * For Swing Applications the EventBus is nearly all you need, besides some of your own Event classes (if so desired).
 * <p>
 * The EventBus is really just a convenience class that provides a static wrapper around a global instance of the
 * {@link SwingEventService}.  For details on the API and implementation, see {@link EventService},
 * {@link SwingEventService}, and its parent {@link ThreadSafeEventService}.
 * This class exists solely for simplicity.  Calling <code>EventBus.subscribeXXX/publishXXX</code> is equivalent to
 * <code>EventServiceLocator.getSwingEventService().subscribeXXX/publishXXX</code>, it is just shorter to type.
 * @see EventService
 * @see SwingEventService
 * @see ThreadSafeEventService
 * See package JavaDoc for more information
 * @author Michael Bushe michael@bushe.com
 */
public class EventBus {

   private static EventService globalEventService = EventServiceLocator.getSwingEventService();

   /**
    * The EventBus uses a global static EventService.  This method is not necessary in usual usage, use the other
    * static methods instead.  It is used to expose any other functionality and for framework classes (EventBusAction)
    *
    * @return the global static EventService
    */
   public static EventService getGlobalEventService() {
      return globalEventService;
   }

   /**
    * @see EventService#publish(EventServiceEvent)
    */
   public static void publish(EventServiceEvent evt) {
      if (evt == null) {
         throw new IllegalArgumentException("Can't publish null.");
      }
      globalEventService.publish(evt);
   }

   /**
    * @see EventService#publish(String, Object)
    */
   public static void publish(String topic, Object o) {
      if (topic == null) {
         throw new IllegalArgumentException("Can't publish to null topic.");
      }
      globalEventService.publish(topic, o);
   }

   /**
    * @see EventService#subscribe(Class, EventSubscriber)
    */
   public static boolean subscribe(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.subscribe(eventClass, subscriber);
   }

   /**
    * @see EventService#subscribeStrongly(Class, EventSubscriber)
    */
   public static boolean subscribeStrongly(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.subscribeStrongly(eventClass, subscriber);
   }

   /**
    * @see EventService#unsubscribe(Class, EventSubscriber)
    */
   public static boolean unsubscribe(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.unsubscribe(eventClass, subscriber);
   }

   /**
    * @see EventService#subscribeVetoListener(Class, VetoEventListener)
    */
   public static boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(eventClass, vetoListener);
   }

   /**
    * @see EventService#subscribeVetoListenerStrongly(Class, VetoEventListener)
    */
   public static boolean subscribeVetoListenerStrongly(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerStrongly(eventClass, vetoListener);
   }

   /**
    * @see EventService#unsubscribeVetoListener(Class, VetoEventListener)
    */
   public static boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(eventClass, vetoListener);
   }

   /**
    * @see EventService#subscribe(String, EventTopicSubscriber)
    */
   public static boolean subscribe(String topic, EventTopicSubscriber subscriber) {
      return globalEventService.subscribe(topic, subscriber);
   }

   /**
    * @see EventService#subscribeStrongly(String, EventTopicSubscriber)
    */
   public static boolean subscribeStrongly(String topic, EventTopicSubscriber subscriber) {
      return globalEventService.subscribeStrongly(topic, subscriber);
   }

   /**
    * @see EventService#unsubscribe(String, EventTopicSubscriber)
    */
   public static boolean unsubscribe(String topic, EventTopicSubscriber subscriber) {
      return globalEventService.unsubscribe(topic, subscriber);
   }

   /**
    * @see EventService#subscribeVetoListenerStrongly(String, VetoEventListener)
    */
   public static boolean subscribeVetoListener(String topic, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(topic, vetoListener);
   }

   /**
    * @see EventService#subscribeVetoListener(String, VetoEventListener)
    */
   public static boolean subscribeVetoListenerStrongly(String topic, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerStrongly(topic, vetoListener);
   }

   /**
    * @see EventService#unsubscribeVetoListener(String, VetoEventListener)
    */
   public static boolean unsubscribeVetoListener(String topic, VetoEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(topic, vetoListener);
   }

}
