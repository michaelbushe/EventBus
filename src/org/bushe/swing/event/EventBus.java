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
 * The EventBus provides Swing event publication and subscription services.
 * <p>
 * For Swing Applications the EventBus is nearly all you need, besides some of your own Event classes (if so desired).
 * <p>
 * The EventBus is really just a convenience class that provides a static wrapper around a global instance of the
 * {@link SwingEventService}.  For details on the API and implementation, see {@link EventService}, SwingEventService,
 * and its parent {@link ThreadSafeEventService}.  Calling EventBus subscribe/publish is equivalent to
 * <code>EventServiceLocator.getSwingEventService().subscribeXXX/publishXXX</code>, it is just shorter to type.
 * @see <b>package JavaDoc<b> for more information
 * @see EventService
 * @see SwingEventService
 * @see ThreadSafeEventService
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
    * @see {@link EventService#publish(EventServiceEvent)}
    */
   public static void publish(EventServiceEvent evt) {
      globalEventService.publish(evt);
   }

   /**
    * @see {@link EventService#publish(String, Object)}
    */
   public static void publish(String topic, Object o) {
      globalEventService.publish(topic, o);
   }

   /**
    * @see {@link EventService#subscribe(Class, EventHandler)}
    */
   public static boolean subscribe(Class eventClass, EventHandler handler) {
      return globalEventService.subscribe(eventClass, handler);
   }

   /**
    * @see {@link EventService#unsubscribe(Class, EventHandler)}
    */
   public static boolean unsubscribe(Class eventClass, EventHandler handler) {
      return globalEventService.unsubscribe(eventClass, handler);
   }

   /**
    * @see {@link EventService#subscribeWeakly(Class, EventHandler)}
    */
   public static boolean subscribeWeakly(Class eventClass, EventHandler handler) {
      return globalEventService.subscribeWeakly(eventClass, handler);
   }

   /**
    * @see {@link EventService#subscribe(String, EventTopicHandler)}
    */
   public static boolean subscribe(String topic, EventTopicHandler handler) {
      return globalEventService.subscribe(topic, handler);
   }

   /**
    * @see {@link EventService#unsubscribe(String, EventTopicHandler)}
    */
   public static boolean unsubscribe(String topic, EventTopicHandler handler) {
      return globalEventService.unsubscribe(topic, handler);
   }

   /**
    * @see {@link EventService#subscribeWeakly(String, EventTopicHandler)}
    */
   public static boolean subscribeWeakly(String topic, EventTopicHandler handler) {
      return globalEventService.subscribeWeakly(topic, handler);
   }

   /**
    * @see {@link EventService#subscribeVetoListener(Class, VetoEventListener)}
    */
   public static boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(eventClass, vetoListener);
   }

   /**
    * @see {@link EventService#unsubscribeVetoListener(Class, VetoEventListener)}
    */
   public static boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(eventClass, vetoListener);
   }

   /**
    * @see {@link EventService#subscribeVetoListenerWeakly(Class, VetoEventListener)}
    */
   public static boolean subscribeVetoListenerWeakly(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerWeakly(eventClass, vetoListener);
   }

   /**
    * @see {@link EventService#subscribeVetoListener(String, VetoEventListener)}
    */
   public static boolean subscribeVetoListener(String topic, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(topic, vetoListener);
   }

   /**
    * @see {@link EventService#unsubscribeVetoListener(String, VetoEventListener)}
    */
   public static boolean unsubscribeVetoListener(String topic, VetoEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(topic, vetoListener);
   }

   /**
    * @see {@link EventService#subscribeVetoListenerWeakly(String, VetoEventListener)}
    */
   public static boolean subscribeVetoListenerWeakly(String topic, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(topic, vetoListener);
   }

}
