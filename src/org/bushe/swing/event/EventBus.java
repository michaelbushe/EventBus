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
import java.lang.reflect.Type;

/**
 * The EventBus provides Swing event publication and subscription services.  It is a simple static wrapper around a
 * global instance of an {@link EventService}, specifically a {@link SwingEventService}.
 * <p/>
 * For Swing Applications the EventBus is nearly all you need, besides some of your own Event classes (if so desired).
 * <p/>
 * The EventBus is really just a convenience class that provides a static wrapper around a global instance of the {@link
 * SwingEventService}.  For details on the API and implementation, see {@link EventService}, {@link SwingEventService},
 * and its parent {@link ThreadSafeEventService}. This class exists solely for simplicity.  Calling
 * <code>EventBus.subscribeXXX/publishXXX</code> is equivalent to <code>EventServiceLocator.getSwingEventService().subscribeXXX/publishXXX</code>,
 * it is just shorter to type.  See {@link org.bushe.swing.event.EventServiceLocator} for details on how a custom
 * EventService can be installed instead of the default SwingEventService. 
 *
 * @author Michael Bushe michael@bushe.com
 * @see EventService
 * @see SwingEventService
 * @see ThreadSafeEventService See package JavaDoc for more information
 */
public class EventBus {

   private static EventService globalEventService = EventServiceLocator.getSwingEventService();

   /**
    * The EventBus uses a global static EventService.  This method is not necessary in usual usage, use the other static
    * methods instead.  It is used to expose any other functionality and for framework classes (EventBusAction)
    *
    * @return the global static EventService
    */
   public static EventService getGlobalEventService() {
      return globalEventService;
   }

   /** @see EventService#publish(Object) */
   public static void publish(Object event) {
      if (event == null) {
         throw new IllegalArgumentException("Can't publish null.");
      }
      globalEventService.publish(event);
   }

   /** @see EventService#publish(String,Object) */
   public static void publish(String topic, Object o) {
      if (topic == null) {
         throw new IllegalArgumentException("Can't publish to null topic.");
      }
      globalEventService.publish(topic, o);
   }

   /** @see EventService#publish(java.lang.reflect.Type, Object)  */
   public static void publish(Type genericType, Object o) {
      if (genericType == null) {
         throw new IllegalArgumentException("Can't publish to null type.");
      }
      globalEventService.publish(genericType, o);
   }


   /** @see EventService#subscribe(Class,EventSubscriber) */
   public static boolean subscribe(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.subscribe(eventClass, subscriber);
   }

   /** @see EventService#subscribe(java.lang.reflect.Type, EventSubscriber)  */
   public static boolean subscribe(Type genericType, EventSubscriber subscriber) {
      return globalEventService.subscribe(genericType, subscriber);
   }

   /** @see EventService#subscribeExactly(Class,EventSubscriber) */
   public static boolean subscribeExactly(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.subscribeExactly(eventClass, subscriber);
   }

   /** @see EventService#subscribe(String,EventTopicSubscriber) */
   public static boolean subscribe(String topic, EventTopicSubscriber subscriber) {
      return globalEventService.subscribe(topic, subscriber);
   }

   /** @see EventService#subscribe(Pattern,EventTopicSubscriber) */
   public static boolean subscribe(Pattern topicPattern, EventTopicSubscriber subscriber) {
      return globalEventService.subscribe(topicPattern, subscriber);
   }

   /** @see EventService#subscribeStrongly(Class,EventSubscriber) */
   public static boolean subscribeStrongly(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.subscribeStrongly(eventClass, subscriber);
   }

   /** @see EventService#subscribeExactlyStrongly(Class,EventSubscriber) */
   public static boolean subscribeExactlyStrongly(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.subscribeExactlyStrongly(eventClass, subscriber);
   }

   /** @see EventService#subscribeStrongly(String,EventTopicSubscriber) */
   public static boolean subscribeStrongly(String topic, EventTopicSubscriber subscriber) {
      return globalEventService.subscribeStrongly(topic, subscriber);
   }

   /** @see EventService#subscribeStrongly(Pattern,EventTopicSubscriber) */
   public static boolean subscribeStrongly(Pattern topicPattern, EventTopicSubscriber subscriber) {
      return globalEventService.subscribeStrongly(topicPattern, subscriber);
   }

   /** @see EventService#unsubscribe(Class,EventSubscriber) */
   public static boolean unsubscribe(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.unsubscribe(eventClass, subscriber);
   }

   /** @see EventService#unsubscribeExactly(Class,EventSubscriber) */
   public static boolean unsubscribeExactly(Class eventClass, EventSubscriber subscriber) {
      return globalEventService.unsubscribeExactly(eventClass, subscriber);
   }

   /** @see EventService#unsubscribe(String,EventTopicSubscriber) */
   public static boolean unsubscribe(String topic, EventTopicSubscriber subscriber) {
      return globalEventService.unsubscribe(topic, subscriber);
   }

   /** @see EventService#unsubscribe(Pattern,EventTopicSubscriber) */
   public static boolean unsubscribe(Pattern topicPattern, EventTopicSubscriber subscriber) {
      return globalEventService.unsubscribe(topicPattern, subscriber);
   }

   /**
    * For usage with annotatations.
    *
    * @see EventService#unsubscribe(Class,Object)
    */
   public static boolean unsubscribe(Class eventClass, Object object) {
      return globalEventService.unsubscribe(eventClass, object);
   }

   /**
    * For usage with annotatations.
    *
    * @see EventService#unsubscribeExactly(Class,Object)
    */
   public static boolean unsubscribeExactly(Class eventClass, Object subscriber) {
      return globalEventService.unsubscribeExactly(eventClass, subscriber);
   }

   /**
    * For usage with annotatations.
    *
    * @see EventService#unsubscribe(String,Object)
    */
   public static boolean unsubscribe(String topic, Object subscriber) {
      return globalEventService.unsubscribe(topic, subscriber);
   }

   /**
    * For usage with annotatations.
    *
    * @see EventService#unsubscribe(Pattern,Object)
    */
   public static boolean unsubscribe(Pattern topicPattern, Object subscriber) {
      return globalEventService.unsubscribe(topicPattern, subscriber);
   }

   /** @see EventService#subscribeVetoListener(Class,VetoEventListener) */
   public static boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(eventClass, vetoListener);
   }

   /** @see EventService#subscribeVetoListener(Class,VetoEventListener) */
   public static boolean subscribeVetoListenerExactly(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerExactly(eventClass, vetoListener);
   }


   /** @see EventService#subscribeVetoListener(String,VetoTopicEventListener) */
   public static boolean subscribeVetoListener(String topic, VetoTopicEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(topic, vetoListener);
   }

   /** @see EventService#subscribeVetoListener(Pattern,VetoTopicEventListener) */
   public static boolean subscribeVetoListener(Pattern topicPattern, VetoTopicEventListener vetoListener) {
      return globalEventService.subscribeVetoListener(topicPattern, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerStrongly(Class,VetoEventListener) */
   public static boolean subscribeVetoListenerStrongly(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerStrongly(eventClass, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerExactlyStrongly(Class,VetoEventListener) */
   public static boolean subscribeVetoListenerExactlyStrongly(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerExactlyStrongly(eventClass, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerStrongly(String,VetoTopicEventListener) */
   public static boolean subscribeVetoListenerStrongly(String topic, VetoTopicEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerStrongly(topic, vetoListener);
   }

   /** @see EventService#subscribeVetoListener(String,VetoTopicEventListener) */
   public static boolean subscribeVetoListenerStrongly(Pattern topicPattern, VetoTopicEventListener vetoListener) {
      return globalEventService.subscribeVetoListenerStrongly(topicPattern, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListener(Class,VetoEventListener) */
   public static boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(eventClass, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListenerExactly(Class,VetoEventListener) */
   public static boolean unsubscribeVetoListenerExactly(Class eventClass, VetoEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListenerExactly(eventClass, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListener(String,VetoTopicEventListener) */
   public static boolean unsubscribeVetoListener(String topic, VetoTopicEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(topic, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListener(Pattern,VetoTopicEventListener) */
   public static boolean unsubscribeVetoListener(Pattern topicPattern, VetoTopicEventListener vetoListener) {
      return globalEventService.unsubscribeVetoListener(topicPattern, vetoListener);
   }

   /** @see EventService#getSubscribers(Class) */
   public static List getSubscribers(Class eventClass) {
      return globalEventService.getSubscribers(eventClass);
   }

   /** @see EventService#getSubscribersToClass(Class) */
   public static List getSubscribersToClass(Class eventClass) {
      return globalEventService.getSubscribersToClass(eventClass);
   }

   /** @see EventService#getSubscribersToExactClass(Class) */
   public static List getSubscribersToExactClass(Class eventClass) {
      return globalEventService.getSubscribersToExactClass(eventClass);
   }

   /** @see EventService#getSubscribers(String) */
   public static List getSubscribers(String topic) {
      return globalEventService.getSubscribers(topic);
   }

   /** @see EventService#getSubscribersToTopic(String) */
   public static List getSubscribersToTopic(String topic) {
      return globalEventService.getSubscribersToTopic(topic);
   }

   /** @see EventService#getSubscribers(Class) */
   public static List getSubscribersByPattern(String topic) {
      return globalEventService.getSubscribersByPattern(topic);
   }

   /** @see EventService#getSubscribers(Class) */
   public static List getVetoSubscribers(Class eventClass) {
      return globalEventService.getVetoSubscribers(eventClass);
   }

   /** @see EventService#getVetoSubscribersToExactClass(Class) */
   public static List getVetoSubscribersToExactClass(Class eventClass) {
      return globalEventService.getVetoSubscribersToExactClass(eventClass);
   }

   /** @see EventService#getVetoSubscribers(Class) */
   public static List getVetoSubscribers(String topic) {
      return globalEventService.getVetoSubscribers(topic);
   }

   /** @see EventService#getVetoSubscribersToClass(Class) */
   public static List getVetoSubscribersToClass(Class eventClass) {
      return globalEventService.getVetoSubscribersToClass(eventClass);
   }

   /** @see EventService#getVetoSubscribers(Pattern) */
   public static List getVetoSubscribers(Pattern pattern) {
      return globalEventService.getVetoSubscribers(pattern);
   }

   /** @see EventService#clearAllSubscribers() */
   public static void clearAllSubscribers() {
      globalEventService.clearAllSubscribers();
   }

   /** @see EventService#setDefaultCacheSizePerClassOrTopic(int) */
   public static void setDefaultCacheSizePerClassOrTopic(int defaultCacheSizePerClassOrTopic) {
      globalEventService.setDefaultCacheSizePerClassOrTopic(defaultCacheSizePerClassOrTopic);
   }

   /** @see org.bushe.swing.event.EventService#getDefaultCacheSizePerClassOrTopic() */
   public static int getDefaultCacheSizePerClassOrTopic() {
      return globalEventService.getDefaultCacheSizePerClassOrTopic();
   }

   /** @see EventService#setCacheSizeForEventClass(Class,int) */
   public static void setCacheSizeForEventClass(Class eventClass, int cacheSize) {
      globalEventService.setCacheSizeForEventClass(eventClass, cacheSize);
   }

   /** @see EventService#getCacheSizeForEventClass(Class) */
   public static int getCacheSizeForEventClass(Class eventClass) {
      return globalEventService.getCacheSizeForEventClass(eventClass);
   }

   /** @see EventService#setCacheSizeForTopic(String,int) */
   public static void setCacheSizeForTopic(String topicName, int cacheSize) {
      globalEventService.setCacheSizeForTopic(topicName, cacheSize);
   }

   /** @see EventService#setCacheSizeForTopic(java.util.regex.Pattern,int) */
   public static void setCacheSizeForTopic(Pattern pattern, int cacheSize) {
      globalEventService.setCacheSizeForTopic(pattern, cacheSize);
   }

   /** @see EventService#getCacheSizeForTopic(String) */
   public static int getCacheSizeForTopic(String topic) {
      return globalEventService.getCacheSizeForTopic(topic);
   }

   /** @see EventService#getLastEvent(Class) */
   public static Object getLastEvent(Class eventClass) {
      return globalEventService.getLastEvent(eventClass);
   }

   /** @see EventService#getCachedEvents(Class) */
   public static List getCachedEvents(Class eventClass) {
      return globalEventService.getCachedEvents(eventClass);
   }

   /** @see EventService#getLastTopicData(String) */
   public static Object getLastTopicData(String topic) {
      return globalEventService.getLastTopicData(topic);
   }

   /** @see EventService#getCachedTopicData(String) */
   public static List getCachedTopicData(String topic) {
      return globalEventService.getCachedTopicData(topic);
   }

   /** @see EventService#clearCache(Class) */
   public static void clearCache(Class eventClass) {
      globalEventService.clearCache(eventClass);
   }

   /** @see EventService#clearCache(String) */
   public static void clearCache(String topic) {
      globalEventService.clearCache(topic);
   }

   /** @see EventService#clearCache(java.util.regex.Pattern) */
   public static void clearCache(Pattern pattern) {
      globalEventService.clearCache(pattern);
   }

   /** @see org.bushe.swing.event.EventService#clearCache() */
   public static void clearCache() {
      globalEventService.clearCache();
   }

}
