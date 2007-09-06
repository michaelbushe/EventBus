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

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bushe.swing.exception.SwingException;

/**
 * A thread-safe EventService implementation.
 * <p/>
 * This implementation is <b>not Swing thread-safe</b>.  If publication occurs on a thread other than the Swing
 * EventDispatchThread, subscribers will receive the event on the calling thread, and not the EDT.  Swing components
 * should use the SwingEventService instead, which is the implementation used by the EventBus.
 * <p/>
 * On event publication, subscribers are called in the order in which they subscribed.
 * <p/>
 * Events and/or topic data can be cached, but are not by default.  To cache events or topic data, call
 * #setDefaultCacheSizePerClassOrTopic(int), #setCacheSizeForEventClass(Class, int), or #setCacheSizeForTopic(String,
 * int), #setCacheSizeForTopic(Pattern, int).  Retrieve cached values with #getLastEvent(Class),
 * #getLastTopicData(String), #getCachedEvents(Class), or #getCachedTopicData(String).  Using caching while subscribing
 * is most likely to make sense only if you subscribe and publish on the same thread (so caching is very useful for
 * Swing applications since both happen on the EDT in a single-threaded manner). In multithreaded applications, you
 * never know if your subscriber has handled an event while it was being subscribed (before the subscribe() method
 * returned) that is newer or older than the retrieved cached value (taked before or after subscribe() respectively).
 * <p/>
 * To deal with subscribers that take too long (a concern in Swing applications), the EventService can be made to issue
 * {@link SubscriberTimingEvent}s when subscribers exceed a certain time.  This does not interrupt subscriber processing
 * and is published after the subscriber finishes.  The service can log a warning for SubscriberTimingEvents, see the
 * constructor {@link ThreadSafeEventService (long, boolean)}.  The timing is checked for veto subscribers too.
 * <p/>
 * Multithreaded note: Two threads may be accessing the ThreadSafeEventService at the same time, on unsubscribing a
 * listener for topic "A" and the other publishing on topic "A".  If the unsubsubscribing thread gets the lock first,
 * then it is unsubscubscribed, end of story.  If the publisher gets the lock first, then a snapshot copy of the current
 * listeners is made during the publication, the lock is released and the subscribers are called.  Between the time the
 * lock is released and the time that the listener is called, the unsubscribing thread can unsubscribe, resulting in an
 * unsubscribed object receiving notifiction of the event.
 * <p/>
 * Exceptions are logged by default, override {@link #handleException(String,Object,String,Object,Throwable,
 *StackTraceElement[],String)} to handleException exceptions in another way.  Each call to a subscriber is wrapped in
 * a try block to ensure one listener does not interfere with another.
 * <p/>
 * Fine logging can be turned via java.util.logging using the name "org.bushe.swing.event.ThreadSafeEventService".  This
 * aids in debugging which subscription and publication issues.
 *
 * @author Michael Bushe michael@bushe.com
 * @todo perhaps publication should double-check to see if a subscriber is still subscribed
 * @todo (param) a JMS-like selector (can be done in base classes by implements like a commons filter
 * @todo (param) register a Comparator to sort subscriber's calling order - for a class or topic
 * @see EventService for a complete description of the API
 */
public class ThreadSafeEventService implements EventService {
   protected static final Logger LOG = Logger.getLogger(EventService.class.getName());

   private Map subscribersByEventType = new HashMap();
   private Map subscribersByEventClass = new HashMap();
   private Map subscribersByExactEventClass = new HashMap();
   private Map subscribersByTopic = new HashMap();
   private Map subscribersByTopicPattern = new HashMap();
   private Map vetoListenersByClass = new HashMap();
   private Map vetoListenersByExactClass = new HashMap();
   private Map vetoListenersByTopic = new HashMap();
   private Map vetoListenersByTopicPattern = new HashMap();
   private Object listenerLock = new Object();
   private Object cacheLock = new Object();
   private Long timeThresholdForEventTimingEventPublication;
   private Map cacheByEvent = new HashMap/*<Class, LinkedList>*/();
   private int defaultCacheSizePerClassOrTopic = 0;
   private Map cacheSizesForEventClass;
   private Map rawCacheSizesForEventClass;
   private boolean rawCacheSizesForEventClassChanged;
   private Map cacheByTopic = new HashMap/*<Class, LinkedList>*/();
   private Map cacheSizesForTopic;
   private Map rawCacheSizesForTopic;
   private boolean rawCacheSizesForTopicChanged;
   private Map rawCacheSizesForPattern;
   private boolean rawCacheSizesForPatternChanged;


   /** Creates a ThreadSafeEventService that does not monitor timing of handlers. */
   public ThreadSafeEventService() {
      this(null, false);
   }

   /**
    * Creates a ThreadSafeEventService while providing time monitoring options.
    *
    * @param timeThresholdForEventTimingEventPublication the longest time a subscriber should spend handling an event,
    * The service will pulish an SubscriberTimingEvent after listener processing if the time was exceeded.  If null, no
    * EventSubscriberTimingEvent will be issued.
    */
   public ThreadSafeEventService(Long timeThresholdForEventTimingEventPublication) {
      this(timeThresholdForEventTimingEventPublication, false);
   }

   /**
    * Creates a ThreadSafeEventService while providing time monitoring options.
    *
    * @param timeThresholdForEventTimingEventPublication the longest time a subscriber should spend handling an event.
    * The service will pulish an SubscriberTimingEvent after listener processing if the time was exceeded.  If null, no
    * SubscriberTimingEvent will be issued.
    * @param subscribeTimingEventsInternally add a subscriber to the SubscriberTimingEvent internally and call the
    * protected subscribeTiming() method when they occur.  This logs a warning to a java.util.logging logger by
    * default.
    *
    * @throws IllegalArgumentException if timeThresholdForEventTimingEventPublication is null and
    * subscribeTimingEventsInternally is true.
    * @todo (nonSwing-only?) start a timer call and when it calls back, report the time if exceeded.
    */
   public ThreadSafeEventService(Long timeThresholdForEventTimingEventPublication,
           boolean subscribeTimingEventsInternally) {
      if (timeThresholdForEventTimingEventPublication == null && subscribeTimingEventsInternally) {
         throw new IllegalArgumentException("null, true in constructor is not valid.  If you want to send timing messages for all events and subscribe them internally, pass 0, true");
      }
      this.timeThresholdForEventTimingEventPublication = timeThresholdForEventTimingEventPublication;
      if (subscribeTimingEventsInternally) {
         //Listen to timing events and log them
         subscribeStrongly(SubscriberTimingEvent.class, new EventSubscriber() {
            public void onEvent(Object event) {
               subscribeTiming((SubscriberTimingEvent) event);
            }
         });
      }
   }

   /** @see EventService#subscribe(Class,EventSubscriber) */
   public boolean subscribe(Class cl, EventSubscriber eh) {
      if (cl == null) {
         throw new IllegalArgumentException("Event class must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event subscriber must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by class, class:" + cl + ", subscriber:" + eh);
      }
      return subscribe(cl, subscribersByEventClass, new WeakReference(eh));
   }

   /** @see EventService#subscribe(java.lang.reflect.Type, EventSubscriber) */
   public boolean subscribe(Type type, EventSubscriber eh) {
      return subscribe(type, subscribersByEventType, new WeakReference(eh));
   }

   /** @see EventService#subscribeExactly(Class,EventSubscriber) */
   public boolean subscribeExactly(Class cl, EventSubscriber eh) {
      if (cl == null) {
         throw new IllegalArgumentException("Event class must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event subscriber must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by class, class:" + cl + ", subscriber:" + eh);
      }
      return subscribe(cl, subscribersByExactEventClass, new WeakReference(eh));
   }

   /** @see EventService#subscribe(String,EventTopicSubscriber) */
   public boolean subscribe(String topic, EventTopicSubscriber eh) {
      if (topic == null) {
         throw new IllegalArgumentException("Topic must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event topic subscriber must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by topic name, name:" + topic + ", subscriber:" + eh);
      }
      return subscribe(topic, subscribersByTopic, new WeakReference(eh));
   }

   /** @see EventService#subscribe(Pattern,EventTopicSubscriber) */
   public boolean subscribe(Pattern pat, EventTopicSubscriber eh) {
      if (pat == null) {
         throw new IllegalArgumentException("Pattern must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event subscriber must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by pattern, pattern:" + pat + ", subscriber:" + eh);
      }
      return subscribe(pat, subscribersByTopicPattern, new WeakReference(eh));
   }

   /** @see EventService#subscribeStrongly(Class,EventSubscriber) */
   public boolean subscribeStrongly(Class cl, EventSubscriber eh) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing weakly by class, class:" + cl + ", subscriber:" + eh);
      }
      if (eh == null) {
         throw new IllegalArgumentException("Subscriber cannot be null.");
      }
      return subscribe(cl, subscribersByEventClass, eh);
   }

   /** @see EventService#subscribeExactlyStrongly(Class,EventSubscriber) */
   public boolean subscribeExactlyStrongly(Class cl, EventSubscriber eh) {
      if (cl == null) {
         throw new IllegalArgumentException("Event class must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event subscriber must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by class, class:" + cl + ", subscriber:" + eh);
      }
      return subscribe(cl, subscribersByExactEventClass, eh);
   }

   /** @see EventService#subscribeStrongly(String,EventTopicSubscriber) */
   public boolean subscribeStrongly(String name, EventTopicSubscriber eh) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing weakly by topic name, name:" + name + ", subscriber:" + eh);
      }
      if (eh == null) {
         throw new IllegalArgumentException("Subscriber cannot be null.");
      }
      return subscribe(name, subscribersByTopic, eh);
   }

   /** @see EventService#subscribeStrongly(Pattern,EventTopicSubscriber) */
   public boolean subscribeStrongly(Pattern pat, EventTopicSubscriber eh) {
      if (pat == null) {
         throw new IllegalArgumentException("Pattern must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event subscriber must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by pattern, pattern:" + pat + ", subscriber:" + eh);
      }
      return subscribe(pat, subscribersByTopicPattern, eh);
   }


   /** @see org.bushe.swing.event.EventService#clearAllSubscribers() */
   public void clearAllSubscribers() {
      synchronized (listenerLock) {
         this.subscribersByEventType.clear();
         this.subscribersByEventClass.clear();
         this.subscribersByTopic.clear();
         this.vetoListenersByClass.clear();
         this.vetoListenersByTopic.clear();
         this.subscribersByExactEventClass.clear();
         this.subscribersByTopicPattern.clear();
         this.vetoListenersByExactClass.clear();
         this.vetoListenersByTopicPattern.clear();
      }
   }

   /**
    * All subscribe methods call this method.  Extending classes only have to override this method to subscribe all
    * subscriber subscriptions.
    * <p/>
    * Overriding this method is only for the adventurous.  This basically gives you just enough rope to hang yourself.
    *
    * @param o the topic String or event Class to subscribe to
    * @param subscriberMap the internal map of subscribers to use (by topic or class)
    * @param eh the EventSubscriber or EventTopicSubscriber to subscribe, or a WeakReference to either
    *
    * @return boolean if the subscriber is subscribed (was not subscribed).
    *
    * @throws IllegalArgumentException if eh or o is null
    */
   protected boolean subscribe(final Object o, final Map subscriberMap, final Object eh) {
      if (o == null) {
         throw new IllegalArgumentException("Can't subscribe to null.");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Can't subscribe null subscriber to " + o);
      }
      boolean alreadyExists = false;
      synchronized (listenerLock) {
         List subscribers = (List) subscriberMap.get(o);
         if (subscribers == null) {
            if (LOG.isLoggable(Level.FINE)) {
               LOG.fine("Creating new subscriber map for :" + o);
            }
            subscribers = new ArrayList();
            subscriberMap.put(o, subscribers);
         } else {
            //Two weak references to the same object don't compare equal, also need to make sure a weak ref and a hard
            //ref aren't both subscribed
            Object compareEH = eh;
            if (eh instanceof WeakReference) {
               compareEH = ((WeakReference) eh).get();
               if (compareEH == null) {
                  return false;//already garbage collected?  Weird.
               }
            }
            for (Iterator iterator = subscribers.iterator(); iterator.hasNext();) {
               Object existingSubscriber = iterator.next();
               if (existingSubscriber instanceof WeakReference) {
                  existingSubscriber = ((WeakReference) existingSubscriber).get();
                  if (existingSubscriber == null) {
                     iterator.remove();//was garbage collected
                  }
               }
               if (compareEH.equals(existingSubscriber)) {
                  iterator.remove();//will add to the end of the calling list
                  alreadyExists = true;
               }
            }
         }
         subscribers.add(eh);
         return !alreadyExists;
      }
   }

   /** @see EventService#subscribeVetoListener(Class,VetoEventListener) */
   public boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (eventClass == null) {
         throw new IllegalArgumentException("eventClass cannot be null.");
      }
      return subscribeVetoListener(eventClass, vetoListenersByClass, new WeakReference(vetoListener));
   }

   /** @see EventService#subscribeVetoListenerExactly(Class,VetoEventListener) */
   public boolean subscribeVetoListenerExactly(Class eventClass, VetoEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (eventClass == null) {
         throw new IllegalArgumentException("eventClass cannot be null.");
      }
      return subscribeVetoListener(eventClass, vetoListenersByExactClass, new WeakReference(vetoListener));
   }

   /** @see EventService#subscribeVetoListener(String,VetoTopicEventListener) */
   public boolean subscribeVetoListener(String topic, VetoTopicEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (topic == null) {
         throw new IllegalArgumentException("topic cannot be null.");
      }
      return subscribeVetoListener(topic, vetoListenersByTopic, new WeakReference(vetoListener));
   }

   /** @see EventService#subscribeVetoListener(Pattern,VetoTopicEventListener) */
   public boolean subscribeVetoListener(Pattern topicPattern, VetoTopicEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (topicPattern == null) {
         throw new IllegalArgumentException("topicPattern cannot be null.");
      }
      return subscribeVetoListener(topicPattern, vetoListenersByTopicPattern, new WeakReference(vetoListener));
   }

   /** @see EventService#subscribeVetoListenerStrongly(Class,VetoEventListener) */
   public boolean subscribeVetoListenerStrongly(Class eventClass, VetoEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (eventClass == null) {
         throw new IllegalArgumentException("eventClass cannot be null.");
      }
      return subscribeVetoListener(eventClass, vetoListenersByClass, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerExactlyStrongly(Class,VetoEventListener) */
   public boolean subscribeVetoListenerExactlyStrongly(Class eventClass, VetoEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (eventClass == null) {
         throw new IllegalArgumentException("eventClass cannot be null.");
      }
      return subscribeVetoListener(eventClass, vetoListenersByExactClass, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerStrongly(String,VetoTopicEventListener) */
   public boolean subscribeVetoListenerStrongly(String topic, VetoTopicEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (topic == null) {
         throw new IllegalArgumentException("topic cannot be null.");
      }
      return subscribeVetoListener(topic, vetoListenersByTopic, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerStrongly(Pattern,VetoTopicEventListener) */
   public boolean subscribeVetoListenerStrongly(Pattern topicPattern, VetoTopicEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      if (topicPattern == null) {
         throw new IllegalArgumentException("topicPattern cannot be null.");
      }
      return subscribeVetoListener(topicPattern, vetoListenersByTopicPattern, vetoListener);
   }

   /**
    * All veto subscriptions methods call this method.  Extending classes only have to override this method to subscribe
    * all veto subscriptions.
    *
    * @param o the topic, Pattern, or event class to subsribe to
    * @param vetoListenerMap the internal map of veto listeners to use (by topic of class)
    * @param vl the veto listener to subscribe, may be a VetoEventListener or a WeakReference to one
    *
    * @return boolean if the veto listener is subscribed (was not subscribed).
    *
    * @throws IllegalArgumentException if vl or o is null
    */
   protected boolean subscribeVetoListener(final Object o, final Map vetoListenerMap, final Object vl) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("subscribeVetoListenerStrongly(" + o + "," + vl + ")");
      }
      if (vl == null) {
         throw new IllegalArgumentException("Can't subscribe null veto listener to " + o);
      }
      if (o == null) {
         throw new IllegalArgumentException("Can't subscribe veto listener to null.");
      }
      synchronized (listenerLock) {
         List vetoListeners = (List) vetoListenerMap.get(o);
         if (vetoListeners == null) {
            vetoListeners = new ArrayList();
            vetoListenerMap.put(o, vetoListeners);
         }
         return vetoListeners.add(vl);
      }
   }

   /** @see EventService#unsubscribe(Class,EventSubscriber) */
   public boolean unsubscribe(Class cl, EventSubscriber eh) {
      return unsubscribe(cl, subscribersByEventClass, eh);
   }

   /** @see EventService#unsubscribeExactly(Class,EventSubscriber) */
   public boolean unsubscribeExactly(Class cl, EventSubscriber eh) {
      return unsubscribe(cl, subscribersByExactEventClass, eh);
   }

   /** @see EventService#unsubscribe(String,EventTopicSubscriber) */
   public boolean unsubscribe(String name, EventTopicSubscriber eh) {
      return unsubscribe(name, subscribersByTopic, eh);
   }

   /** @see EventService#unsubscribe(String,EventTopicSubscriber) */
   public boolean unsubscribe(Pattern topicPattern, EventTopicSubscriber eh) {
      return unsubscribe(topicPattern, subscribersByTopicPattern, eh);
   }

   /**
    * All event subscriber unsubscriptions call this method.  Extending classes only have to override this method to
    * subscribe all subscriber unsubscriptions.
    *
    * @param o the topic or event class to unsubsribe from
    * @param subscriberMap the map of subscribers to use (by topic of class)
    * @param eh the subscriber to unsubscribe, either an EventSubscriber or an EventTopicSubscriber, or a WeakReference
    * to either
    *
    * @return boolean if the subscriber is unsubscribed (was subscribed).
    */
   protected boolean unsubscribe(Object o, Map subscriberMap, Object eh) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("unsubscribe(" + o + "," + eh + ")");
      }
      if (o == null) {
         throw new IllegalArgumentException("Can't unsubscribe to null.");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Can't unsubscribe null subscriber to " + o);
      }
      synchronized (listenerLock) {
         return removeFromSetResolveWeakReferences(subscriberMap, o, eh);

      }
   }

   /** @see EventService#unsubscribe(Class,Object) */
   public boolean unsubscribe(Class eventClass, Object subcribedByProxy) {
      EventSubscriber subscriber = (EventSubscriber) getProxySubscriber(eventClass, subcribedByProxy);
      if (subscriber == null) {
         return false;
      } else {
         return unsubscribe(eventClass, subscriber);
      }
   }

   /** @see EventService#unsubscribeExactly(Class,Object) */
   public boolean unsubscribeExactly(Class eventClass, Object subcribedByProxy) {
      Object subscriber = getProxySubscriber(eventClass, subcribedByProxy);
      if (subscriber == null) {
         return false;
      } else {
         return unsubscribeExactly(eventClass, subscriber);
      }
   }

   /** @see EventService#unsubscribe(String,Object) */
   public boolean unsubscribe(String topic, Object subcribedByProxy) {
      EventTopicSubscriber subscriber = (EventTopicSubscriber) getProxySubscriber(topic, subcribedByProxy);
      if (subscriber == null) {
         return false;
      } else {
         return unsubscribe(topic, subscriber);
      }
   }

   /** @see EventService#unsubscribe(java.util.regex.Pattern,Object) */
   public boolean unsubscribe(Pattern pattern, Object subcribedByProxy) {
      EventTopicSubscriber subscriber = (EventTopicSubscriber) getProxySubscriber(pattern, subcribedByProxy);
      if (subscriber == null) {
         return false;
      } else {
         return unsubscribe(pattern, subscriber);
      }
   }

   private ProxySubscriber getProxySubscriber(Class eventClass, Object subcribedByProxy) {
      List subscribers = getSubscribers(eventClass);
      return getProxySubscriber(subscribers, subcribedByProxy);
   }

   private ProxySubscriber getProxySubscriber(String topic, Object subcribedByProxy) {
      List subscribers = getSubscribers(topic);
      return getProxySubscriber(subscribers, subcribedByProxy);
   }

   private ProxySubscriber getProxySubscriber(Pattern pattern, Object subcribedByProxy) {
      List subscribers = getSubscribersToPattern(pattern);
      return getProxySubscriber(subscribers, subcribedByProxy);
   }

   private ProxySubscriber getProxySubscriber(List subscribers, Object subcribedByProxy) {
      for (Iterator iter = subscribers.iterator(); iter.hasNext();) {
         Object subscriber = iter.next();
         if (subscriber instanceof WeakReference) {
            WeakReference wr = (WeakReference) subscriber;
            subscriber = wr.get();
         }
         if (subscriber instanceof ProxySubscriber) {
            ProxySubscriber proxy = (ProxySubscriber) subscriber;
            subscriber = proxy.getProxiedSubscriber();
            if (subscriber == subcribedByProxy) {
               return proxy;
            }
         }
      }
      return null;
   }

   /** @see EventService#unsubscribeVetoListener(Class,VetoEventListener) */
   public boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return unsubscribeVetoListener(eventClass, vetoListenersByClass, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListenerExactly(Class,VetoEventListener) */
   public boolean unsubscribeVetoListenerExactly(Class eventClass, VetoEventListener vetoListener) {
      return unsubscribeVetoListener(eventClass, vetoListenersByExactClass, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListener(String,VetoTopicEventListener) */
   public boolean unsubscribeVetoListener(String topic, VetoTopicEventListener vetoListener) {
      return unsubscribeVetoListener(topic, vetoListenersByTopic, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListener(Pattern,VetoTopicEventListener) */
   public boolean unsubscribeVetoListener(Pattern topicPattern, VetoTopicEventListener vetoListener) {
      return unsubscribeVetoListener(topicPattern, vetoListenersByTopicPattern, vetoListener);
   }

   /**
    * All veto unsubscriptions methods call this method.  Extending classes only have to override this method to
    * subscribe all veto unsubscriptions.
    *
    * @param o the topic or event class to unsubsribe from
    * @param vetoListenerMap the map of veto listeners to use (by topic or class)
    * @param vl the veto listener to unsubscribe, or a WeakReference to one
    *
    * @return boolean if the veto listener is unsubscribed (was subscribed).
    */
   protected boolean unsubscribeVetoListener(Object o, Map vetoListenerMap, Object vl) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("unsubscribeVetoListener(" + o + "," + vl + ")");
      }
      if (o == null) {
         throw new IllegalArgumentException("Can't unsubscribe veto listener to null.");
      }
      if (vl == null) {
         throw new IllegalArgumentException("Can't unsubscribe null veto listener to " + o);
      }
      synchronized (listenerLock) {
         return removeFromSetResolveWeakReferences(vetoListenerMap, o, vl);
      }
   }

   /** @see EventService#publish(Object) */
   public void publish(Object event) {
      if (event == null) {
         throw new IllegalArgumentException("Cannot publish null event.");
      }
      publish(event, null, null, getSubscribers(event.getClass()), getVetoSubscribers(event.getClass()), null);
   }

   /** @see EventService#publish(java.lang.reflect.Type, Object)  */
   public void publish(Type genericType, Object event) {
      if (genericType == null) {
         throw new IllegalArgumentException("genericType must not be null.");
      }
      if (event == null) {
         throw new IllegalArgumentException("Cannot publish null event.");
      }
      publish(event, null, null, getSubscribers(genericType), null/*getVetoSubscribers(genericType)*/, null);
   }

   /** @see EventService#publish(String,Object) */
   public void publish(String topicName, Object eventObj) {
      publish(null, topicName, eventObj, getSubscribers(topicName), getVetoSubscribers(topicName), null);
   }

   /**
    * All publish methods call this method.  Extending classes only have to override this method to handle all
    * publishing cases.
    *
    * @param event the event to publish, null if publishing on a topic
    * @param topic if publishing on a topic, the topic to publish on, else null
    * @param eventObj if publishing on a topic, the eventObj to publish, else null
    * @param subscribers the subscribers to publish to - must be a snapshot copy
    * @param vetoSubscribers the veto subscribers to publish to - must be a snapshot copy.
    *
    * @throws IllegalArgumentException if eh or o is null
    */
   protected void publish(final Object event, final String topic, final Object eventObj,
           final List subscribers, final List vetoSubscribers, StackTraceElement[] callingStack) {

      if (event == null && topic == null) {
         throw new IllegalArgumentException("Can't publish to null topic/event.");
      }

      //topic or event
      if (LOG.isLoggable(Level.FINE)) {
         if (event != null) {
            LOG.fine("Publishing event: class=" + event.getClass() + ", event=" + event);
         } else if (topic != null) {
            LOG.fine("Publishing event: topic=" + topic + ", eventObj=" + eventObj);
         }
      }

      //Check all veto subscribers, if any veto, then don't publish or cache
      if (vetoSubscribers != null && !vetoSubscribers.isEmpty()) {
         for (Iterator vlIter = vetoSubscribers.iterator(); vlIter.hasNext();) {
            Object vetoer = vlIter.next();
            VetoEventListener vl = null;
            VetoTopicEventListener vtl = null;
            if (event == null) {
               vtl = (VetoTopicEventListener) vetoer;
            } else {
               vl = (VetoEventListener) vetoer;
            }
            long start = System.currentTimeMillis();
            try {
               boolean shouldVeto = false;
               if (event == null) {
                  shouldVeto = vtl.shouldVeto(topic, eventObj);
               } else {
                  shouldVeto = vl.shouldVeto(event);
               }
               if (shouldVeto) {
                  handleVeto(vl, event, vtl, topic, eventObj);
                  checkTimeLimit(start, event, null, vl);
                  if (LOG.isLoggable(Level.FINE)) {
                     LOG.fine("Publication vetoed. Event:" + event + ", Topic:" + topic + ", veto subscriber:" + vl);
                  }
                  return;
               }
            } catch (Throwable ex) {
               checkTimeLimit(start, event, null, vl);
               subscribeVetoException(event, topic, eventObj, ex, callingStack, vl);
            }
         }
      }

      addEventToCache(event, topic, eventObj);

      if (subscribers == null || subscribers.isEmpty()) {
         if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("No subscribers for event or topic. Event:" + event + ", Topic:" + topic);
         }
         return;
      }

      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Publishing to subscribers :" + subscribers);
      }

      for (int i = 0; i < subscribers.size(); i++) {
         Object eh = subscribers.get(i);
         if (event != null) {
            EventSubscriber eventSubscriber = (EventSubscriber) eh;
            long start = System.currentTimeMillis();
            try {
               eventSubscriber.onEvent(event);
               checkTimeLimit(start, event, eventSubscriber, null);
            } catch (Throwable e) {
               checkTimeLimit(start, event, eventSubscriber, null);
               handleException(event, e, callingStack, eventSubscriber);
            }
         } else {
            EventTopicSubscriber eventTopicSubscriber = (EventTopicSubscriber) eh;
            try {
               eventTopicSubscriber.onEvent(topic, eventObj);
            } catch (Throwable e) {
               onEventException(topic, eventObj, e, callingStack, eventTopicSubscriber);
            }
         }
      }
   }

   /**
    * Adds an event to the event cache, if appropriate.  This method is called just before publication to listeners,
    * after the event passes any veto listeners.
    * <p/>
    * Using protected visibility to open the caching to other implementations.
    *
    * @param event the event about to be published, null if topic is non-null
    * @param topic the topic about to be published to, null if the event is non-null
    * @param eventObj the eventObj about to be published on a topic, null if the event is non-null
    */
   protected void addEventToCache(Object event, String topic, Object eventObj) {
      //Taking the listener lock here, since a listener that is now subscribing will want
      //this event since they are not in this subscriber list.
      synchronized (listenerLock) {
         if (event != null) {
            int cacheSizeForEventClass = getCacheSizeForEventClass(event.getClass());
            List eventClassCache = (List) cacheByEvent.get(event.getClass());
            if (cacheSizeForEventClass <= 0) {
               if (eventClassCache != null) {
                  //the cache threshold was lowered to 0
                  cacheByEvent.remove(event.getClass());
               }
            } else {
               if (eventClassCache == null) {
                  eventClassCache = new LinkedList();
                  cacheByEvent.put(event.getClass(), eventClassCache);
               }
               eventClassCache.add(0, event);
               while (eventClassCache.size() > cacheSizeForEventClass) {
                  eventClassCache.remove(eventClassCache.size() - 1);
               }
            }
         } else {
            //topic
            int cacheSizeForTopic = getCacheSizeForTopic(topic);
            List topicCache = (List) cacheByTopic.get(topic);
            if (cacheSizeForTopic <= 0) {
               if (topicCache != null) {
                  //the cache threshold was lowered to 0
                  topicCache.remove(topic);
               }
            } else {
               if (topicCache == null) {
                  topicCache = new LinkedList();
                  cacheByTopic.put(topic, topicCache);
               }
               topicCache.add(0, eventObj);
               while (topicCache.size() > cacheSizeForTopic) {
                  topicCache.remove(topicCache.size() - 1);
               }
            }
         }
      }
   }

   /** @see EventService#getSubscribers(Class) */
   public List getSubscribers(Class eventClass) {
      synchronized (listenerLock) {
         List hierarchyMatches = getSubscribersToClass(eventClass);
         List exactMatches = getSubscribersToExactClass(eventClass);
         List result = new ArrayList();
         if (exactMatches != null) {
            result.addAll(exactMatches);
         }
         if (hierarchyMatches != null) {
            result.addAll(hierarchyMatches);
         }
         return result;
      }
   }

   /** @see EventService#getSubscribersToClass(Class) */
   public List getSubscribersToClass(Class eventClass) {
      synchronized (listenerLock) {
         Map classMap = subscribersByEventClass;
         return getEventOrVetoSubscribersToClass(classMap, eventClass);
      }
   }

   /** @see EventService#getSubscribersToExactClass(Class) */
   public List getSubscribersToExactClass(Class eventClass) {
      synchronized (listenerLock) {
         return getSubscribers(eventClass, subscribersByExactEventClass);
      }
   }

   /** @see EventService#getSubscribers(Class) */
   public List getSubscribers(Type eventType) {
      synchronized (listenerLock) {
         return getEventOrVetoSubscribersToType(subscribersByEventType, eventType);
      }
   }

   /** @see EventService#getSubscribers(String) */
   public List getSubscribers(String topic) {
      synchronized (listenerLock) {
         List exactMatches = getSubscribersToTopic(topic);
         List patternMatches = getSubscribersByPattern(topic);
         List result = new ArrayList();
         if (exactMatches != null) {
            result.addAll(exactMatches);
         }
         if (patternMatches != null) {
            result.addAll(patternMatches);
         }
         return result;
      }
   }

   /** @see EventService#getSubscribers(Class) */
   public List getSubscribersToTopic(String topic) {
      synchronized (listenerLock) {
         return getSubscribers(topic, subscribersByTopic);
      }
   }

   public List getSubscribersByPattern(String topic) {
      List result = new ArrayList();
      Set keys = subscribersByTopicPattern.keySet();
      for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
         Pattern patternKey = (Pattern) iterator.next();
         if (patternKey.matcher(topic).matches()) {
            if (LOG.isLoggable(Level.FINE)) {
               LOG.fine("Pattern " + patternKey + " matched topic name " + topic);
            }
            Collection subscribers = (Collection) subscribersByTopicPattern.get(patternKey);
            result.addAll(createCopyOfContentsRemoveWeakRefs(subscribers));
         }
      }
      return result;
   }


   protected List getSubscribersToPattern(Pattern topicPattern) {
      synchronized (listenerLock) {
         return getSubscribers(topicPattern, subscribersByTopicPattern);
      }
   }

   /** @see EventService#getVetoSubscribers(Class) */
   public List getVetoSubscribers(Class eventClass) {
      synchronized (listenerLock) {
         List exactMatches = getVetoSubscribersToClass(eventClass);
         List hierarchyMatches = getVetoSubscribersToExactClass(eventClass);
         List result = new ArrayList();
         if (exactMatches != null) {
            result.addAll(exactMatches);
         }
         if (hierarchyMatches != null) {
            result.addAll(hierarchyMatches);
         }
         return result;
      }
   }

   /** @see EventService#getVetoSubscribersToClass(Class) */
   public List getVetoSubscribersToClass(Class eventClass) {
      synchronized (listenerLock) {
         Map classMap = vetoListenersByClass;
         return getEventOrVetoSubscribersToClass(classMap, eventClass);
      }
   }

   public List getVetoSubscribersToExactClass(Class eventClass) {
      synchronized (listenerLock) {
         return getSubscribers(eventClass, vetoListenersByExactClass);
      }
   }

   public List getVetoSubscribers(String topic) {
      synchronized (listenerLock) {
         return getSubscribers(topic, vetoListenersByTopic);
      }
   }

   public List getVetoSubscribers(Pattern topicPattern) {
      synchronized (listenerLock) {
         return getSubscribers(topicPattern, vetoListenersByTopicPattern);
      }
   }

   private List getSubscribers(Object classOrTopic, Map subscriberMap) {
      synchronized (listenerLock) {
         List subscribers = (List) subscriberMap.get(classOrTopic);
         //Make a defensive copy of subscribers and veto listeners so listeners
         //can change the listener list while the listeners are being called
         //Resolve WeakReferences and unsubscribe if necessary.
         return createCopyOfContentsRemoveWeakRefs(subscribers);
      }
   }

   private List getEventOrVetoSubscribersToClass(Map classMap, Class eventClass) {
      List result = new ArrayList();
      Set keys = classMap.keySet();
      for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
         Class cl = (Class) iterator.next();
         if (cl.isAssignableFrom(eventClass)) {
            if (LOG.isLoggable(Level.FINE)) {
               LOG.fine("Hierachical match " + cl + " matched event of class " + eventClass);
            }
            Collection subscribers = (Collection) classMap.get(cl);
            result.addAll(createCopyOfContentsRemoveWeakRefs(subscribers));
         }
      }
      return result;
   }

   private List getEventOrVetoSubscribersToType(Map typeMap, Type eventType) {
      List result = new ArrayList();
      Set mapKeySet = typeMap.keySet();
      for (Object mapKey : mapKeySet) {
         Type subscriberType = (Type) mapKey;
         if (eventType instanceof ParameterizedType && subscriberType instanceof ParameterizedType) {
            ParameterizedType subscriberPT = (ParameterizedType) subscriberType;
            ParameterizedType eventPT = (ParameterizedType) eventType;
            if (eventPT.getRawType().equals(subscriberPT.getRawType())) {
               Type[] mapTypeArgs = subscriberPT.getActualTypeArguments();
               Type[] eventTypeArgs = eventPT.getActualTypeArguments();
               if (mapTypeArgs == null || eventTypeArgs == null || mapTypeArgs.length != eventTypeArgs.length) {
                  continue;
               }
               boolean parameterArgsMatch = true;
               for (int argCount = 0; argCount < mapTypeArgs.length; argCount++) {
                  Type eventTypeArg = eventTypeArgs[argCount];
                  if (!(eventTypeArg instanceof Class)) {
                     throw new IllegalArgumentException("Only simple Class parameterized types can be published, not wildcards, etc.  Published attempt made for:"+eventTypeArg);                     
                  }
                  Type subscriberTypeArg = mapTypeArgs[argCount];
                  if (subscriberTypeArg instanceof WildcardType) {
                     WildcardType wildcardSubscriberTypeArg = (WildcardType) subscriberTypeArg;
                     Type[] upperBound = wildcardSubscriberTypeArg.getUpperBounds();
                     Type[] lowerBound = wildcardSubscriberTypeArg.getLowerBounds();
                     if (upperBound != null && upperBound.length > 0) {
                        if (upperBound[0] instanceof Class) {
                           Class upper = (Class) upperBound[0];
                           if (eventTypeArg instanceof Class) {
                              if (!upper.isAssignableFrom((Class) eventTypeArg)) {
                                 parameterArgsMatch = false;
                                 break;
                              }
                           } else {
                              parameterArgsMatch = false;
                              break;
                           }
                        } else {
                           throw new IllegalArgumentException("Only Class and Interface types are supported as types of wildcard subscriptions.  Type:"+upperBound[0]);
                        }
                     }
                     if (lowerBound != null && lowerBound.length > 0) {
                        if (lowerBound[0] instanceof Class) {
                           Class lower = (Class) lowerBound[0];
                           if (eventTypeArg instanceof Class) {
                              if (!((Class)eventTypeArg).isAssignableFrom(lower)) {
                                 parameterArgsMatch = false;
                                 break;
                              }
                           } else {
                              parameterArgsMatch = false;
                              break;
                           }
                        } else {
                           throw new IllegalArgumentException("Only Class and Interface types are supported as types of wildcard subscriptions.  Type:"+upperBound[0]);
                        }
                     }
                  } else if (!subscriberTypeArg.equals(eventTypeArg)) {
                     parameterArgsMatch = false;
                     break;
                  }
               }
               if (parameterArgsMatch) {
                  if (LOG.isLoggable(Level.FINE)) {
                     LOG.fine("Exact parameterized subscriberType match for event subscriberType " + eventType);
                  }
                  Collection subscribers = (Collection) typeMap.get(subscriberType);
                  if (subscribers != null) {
                     result.addAll(createCopyOfContentsRemoveWeakRefs(subscribers));
                  }
               }
            }
         }
      }
      return result;
//            Type o = p.getOwnerType();
//            if (o != null) {
//
//            }
//            p.getActualTypeArguments();
//         }
         /*
	} else if (type instanceof TypeVariable<?>) {
	    TypeVariable<?> v = (TypeVariable<?>)type;
	    out.print(v.getName());
	} else if (type instanceof GenericArrayType) {
	    GenericArrayType a = (GenericArrayType)type;
	    printType(a.getGenericComponentType());
	    out.print("[]");
	} else if (type instanceof WildcardType) {
	    WildcardType w = (WildcardType)type;
	    Type[] upper = w.getUpperBounds();
	    Type[] lower = w.getLowerBounds();
	    if (upper.length==1 && lower.length==0) {
		out.print("? extends ");
		printType(upper[0]);
	    } else if (upper.length==0 && lower.length==1) {
		out.print("? super ");
		printType(lower[0]);
	    } else assert false;
	}         
          */
   }

   private void checkTimeLimit(long start, Object event, EventSubscriber subscriber, VetoEventListener l) {
      if (timeThresholdForEventTimingEventPublication == null) {
         return;
      }
      long end = System.currentTimeMillis();
      if (end - start > timeThresholdForEventTimingEventPublication.longValue()) {
         publish(new SubscriberTimingEvent(this, new Long(start), new Long(end), timeThresholdForEventTimingEventPublication, event, subscriber, l));
      }
   }

   protected void subscribeTiming(SubscriberTimingEvent event) {
      LOG.log(Level.WARNING, event + "");
   }

   /**
    * Handle vetos of an event or topic, by default logs finely.
    *
    * @param vl the veto listener for an event
    * @param event the event, can be null if topic is not
    * @param vtl the veto listener for a topic
    * @param topic can be null if event is not
    * @param eventObj the object published with the topic
    */
   protected void handleVeto(VetoEventListener vl, Object event,
           VetoTopicEventListener vtl, String topic, Object eventObj) {
      //@todo register object that want to know about the veto and notify them of the veto
      if (LOG.isLoggable(Level.FINE)) {
         if (event != null) {
            LOG.fine("Vetoing event: class=" + event.getClass() + ", event=" + event + ", vetoer:" + vl);
         } else {
            LOG.fine("Vetoing event: topic=" + topic + ", eventObj=" + eventObj + ", vetoer:" + vtl);
         }
      }
   }

   /**
    * Given a Map (of Lists of subscribers or veto listeners), removes the toRemove element from the List in the map for
    * the given key.  If WeakReferences are encountered,
    *
    * @param map map of lists
    * @param key key for a List in the map
    * @param toRemove the object to remove form the list with the key of the map
    *
    * @return true if toRemove was unsibscribed
    */
   private boolean removeFromSetResolveWeakReferences(Map map, Object key, Object toRemove) {
      List subscribers = (List) map.get(key);
      if (subscribers == null) {
         return false;
      }
      if (subscribers.remove(toRemove)) {
         return true;
      }

      //search for a WeakReference and ProxySubscribers
      for (Iterator iter = subscribers.iterator(); iter.hasNext();) {
         Object item = iter.next();
         if (item instanceof ProxySubscriber) {
            ProxySubscriber proxy = (ProxySubscriber) item;
            item = proxy.getProxiedSubscriber();
            if (item == toRemove) {
               iter.remove();
               proxy.proxyUnsubscribed();
               return true;
            }
         }
         if (item instanceof WeakReference) {
            WeakReference wr = (WeakReference) item;
            Object realRef = wr.get();
            if (realRef == null) {
               //clean up a garbage collected reference
               iter.remove();
            } else if (realRef == toRemove) {
               iter.remove();
               return true;
            } else if (realRef instanceof ProxySubscriber) {
               ProxySubscriber proxy = (ProxySubscriber) realRef;
               item = proxy.getProxiedSubscriber();
               if (item == toRemove) {
                  iter.remove();
                  proxy.proxyUnsubscribed();
                  return true;
               }
            }
         }
      }
      return false;
   }

   /**
    * Given a set (or subscribers or veto listeners), makes a copy of the set, resolving WeakReferences to hard
    * references, and removing garbage collected referenences from the original set.
    *
    * @param subscribersOrVetoListeners
    *
    * @return a copy of the set
    */
   private List createCopyOfContentsRemoveWeakRefs(Collection subscribersOrVetoListeners) {
      if (subscribersOrVetoListeners == null) {
         return null;
      }
      List copyOfSubscribersOrVetolisteners = new ArrayList(subscribersOrVetoListeners.size());
      for (Iterator iter = subscribersOrVetoListeners.iterator(); iter.hasNext();) {
         Object elem = iter.next();
         if (elem instanceof WeakReference) {
            Object hardRef = ((WeakReference) elem).get();
            if (hardRef == null) {
               //Was reclaimed, unsubscribe
               iter.remove();
            } else {
               copyOfSubscribersOrVetolisteners.add(hardRef);
            }
         } else {
            copyOfSubscribersOrVetolisteners.add(elem);
         }
      }
      return copyOfSubscribersOrVetolisteners;
   }

   /**
    * Sets the default cache size for each kind of event, default is 0 (no caching).
    * <p/>
    * If this value is set to a positive number, then when an event is published, the EventService caches the event or
    * topic payload data for later retrieval.  This allows subscribers to find out what has most recently happened
    * before they subscribed.  The cached event(s) are returned from #getLastEvent(Class), #getLastTopicData(String),
    * #getCachedEvents(Class), or #getCachedTopicData(String)
    * <p/>
    * The default can be overridden on a by-event-class or by-topic basis.
    *
    * @param defaultCacheSizePerClassOrTopic
    */
   public void setDefaultCacheSizePerClassOrTopic(int defaultCacheSizePerClassOrTopic) {
      synchronized (cacheLock) {
         this.defaultCacheSizePerClassOrTopic = defaultCacheSizePerClassOrTopic;
      }
   }

   /** @return the default number of event payloads kept per event class or topic */
   public int getDefaultCacheSizePerClassOrTopic() {
      synchronized (cacheLock) {
         return defaultCacheSizePerClassOrTopic;
      }
   }

   /**
    * Set the number of events cached for a particular class of event.  By default, no events are cached.
    * <p/>
    * This overrides any setting for the DefaultCacheSizePerClassOrTopic.
    * <p/>
    * Class hierarchy semantics are respected.  That is, if there are three events, A, X and Y, and X and Y are both
    * derived from A, then setting the cache size for A applies the cache size for all three.  Setting the cache size
    * for X applies to X and leaves the settings for A and Y in tact.  Intefaces can be passed to this method, but they
    * only take effect if the cache size of a class or it's superclasses has been set. Just like Class.getInterfaces(),
    * if multiple cache sizes are set, the interface names declared earliest in the implements clause of the eventClass
    * takes effect.
    * <p/>
    * The cache for an event is not adjusted until the next event of that class is published.
    *
    * @param eventClass the class of event
    * @param cacheSize the number of published events to cache for this event
    */
   public void setCacheSizeForEventClass(Class eventClass, int cacheSize) {
      synchronized (cacheLock) {
         if (rawCacheSizesForEventClass == null) {
            rawCacheSizesForEventClass = new HashMap();
         }
         rawCacheSizesForEventClass.put(eventClass, new Integer(cacheSize));
         rawCacheSizesForEventClassChanged = true;
      }
   }

   /**
    * Returns the number of events cached for a particular class of event.  By default, no events are cached.
    * <p/>
    * This result is computed for a particular class from the values passed to #setCacheSizeForEventClass(Class, int),
    * and respects the class hierarchy.
    *
    * @param eventClass the class of event
    *
    * @return the maximum size of the event cache for the given event class
    *
    * @see #setCacheSizeForEventClass(Class,int)
    */
   public int getCacheSizeForEventClass(Class eventClass) {
      if (eventClass == null) {
         throw new IllegalArgumentException("eventClass must not be null.");
      }
      synchronized (cacheLock) {
         if (rawCacheSizesForEventClass == null || rawCacheSizesForEventClass.size() == 0) {
            return getDefaultCacheSizePerClassOrTopic();
         }
         if (cacheSizesForEventClass == null) {
            cacheSizesForEventClass = new HashMap();
         }
         if (rawCacheSizesForEventClassChanged) {
            cacheSizesForEventClass.clear();
            cacheSizesForEventClass.putAll(rawCacheSizesForEventClass);
            rawCacheSizesForEventClassChanged = false;
         }

         //Has this been computed yet or set directly?
         Integer size = (Integer) cacheSizesForEventClass.get(eventClass);
         if (size != null) {
            return size.intValue();
         } else {
            //must be computed
            Class parent = eventClass.getSuperclass();
            while (parent != null) {
               Integer parentSize = (Integer) cacheSizesForEventClass.get(parent);
               if (parentSize != null) {
                  cacheSizesForEventClass.put(eventClass, parentSize);
                  return parentSize.intValue();
               }
               parent = parent.getSuperclass();
            }
            //try interfaces
            Class[] interfaces = eventClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
               Class anInterface = interfaces[i];
               Integer interfaceSize = (Integer) cacheSizesForEventClass.get(anInterface);
               if (interfaceSize != null) {
                  cacheSizesForEventClass.put(eventClass, interfaceSize);
                  return interfaceSize.intValue();
               }
            }
         }
         return getDefaultCacheSizePerClassOrTopic();
      }
   }

   /**
    * Set the number of published data objects cached for a particular event topic.  By default, no caching is done.
    * <p/>
    * This overrides any setting for the DefaultCacheSizePerClassOrTopic.
    * <p/>
    * Settings for exact topic names take precedence over pattern matching.
    * <p/>
    * The cache for a topic is not adjusted until the next publication on that topic.
    *
    * @param topicName the topic name
    * @param cacheSize the number of published data Objects to cache for this topci
    */
   public void setCacheSizeForTopic(String topicName, int cacheSize) {
      synchronized (cacheLock) {
         if (rawCacheSizesForTopic == null) {
            rawCacheSizesForTopic = new HashMap();
         }
         rawCacheSizesForTopic.put(topicName, new Integer(cacheSize));
         rawCacheSizesForTopicChanged = true;
      }
   }

   /**
    * Set the number of published data objects cached for topics matching a pattern.  By default, caching is done.
    * <p/>
    * This overrides any setting for the DefaultCacheSizePerClassOrTopic.
    * <p/>
    * Settings for exact topic names take precedence over pattern matching.  If a topic matches the cache settings for
    * more than one pattern, the cache size chosen is an undetermined one from one of the matched pattern settings.
    * <p/>
    * The cache for a topic is not adjusted until the next publication on that topic.
    *
    * @param pattern the pattern matching topic names
    * @param cacheSize the number of data Objects to cache for this topci
    */
   public void setCacheSizeForTopic(Pattern pattern, int cacheSize) {
      synchronized (cacheLock) {
         if (rawCacheSizesForPattern == null) {
            rawCacheSizesForPattern = new HashMap();
         }
         rawCacheSizesForPattern.put(pattern, new Integer(cacheSize));
         rawCacheSizesForPatternChanged = true;
      }
   }

   /**
    * Returns the number of cached data objects published on a particular topic.  By default, no caching is performed.
    * <p/>
    * This result is computed for a particular topic from the values passed to #setCacheSizeForTopic(String, int) and
    * #setCacheSizeForTopic(Pattern, int).
    *
    * @param topic the topic name
    *
    * @return the maximum size of the data Object cache for the given topic
    *
    * @see #setCacheSizeForTopic(String,int)
    * @see #setCacheSizeForTopic(java.util.regex.Pattern,int)
    */
   public int getCacheSizeForTopic(String topic) {
      if (topic == null) {
         throw new IllegalArgumentException("topic must not be null.");
      }
      synchronized (cacheLock) {
         if ((rawCacheSizesForTopic == null || (rawCacheSizesForTopic != null && rawCacheSizesForTopic.size() == 0)) &&
                 (rawCacheSizesForPattern == null || (rawCacheSizesForPattern != null && rawCacheSizesForPattern.size() == 0))) {
            return getDefaultCacheSizePerClassOrTopic();
         }
         if (cacheSizesForTopic == null) {
            cacheSizesForTopic = new HashMap();
         }
         if (rawCacheSizesForTopicChanged || rawCacheSizesForPatternChanged) {
            cacheSizesForTopic.clear();
            cacheSizesForTopic.putAll(rawCacheSizesForTopic);
            rawCacheSizesForTopicChanged = false;
            rawCacheSizesForPatternChanged = false;
         }

         //Is this an exact match or has it been matched to a pattern yet?
         Integer size = (Integer) cacheSizesForTopic.get(topic);
         if (size != null) {
            return size.intValue();
         } else {
            //try mattching patterns
            if (rawCacheSizesForPattern != null) {
               Set patterns = rawCacheSizesForPattern.keySet();
               for (Iterator iterator = patterns.iterator(); iterator.hasNext();) {
                  Pattern pattern = (Pattern) iterator.next();
                  if (pattern.matcher(topic).matches()) {
                     size = (Integer) rawCacheSizesForPattern.get(pattern);
                     cacheSizesForTopic.put(topic, size);
                     return size.intValue();
                  }
               }
            }
         }
         return getDefaultCacheSizePerClassOrTopic();
      }
   }

   /**
    * @param eventClass an index into the cache, cannot be an inteface
    *
    * @return the last event published for this event class, or null if caching is turned off (the default)
    */
   public Object getLastEvent(Class eventClass) {
      if (eventClass.isInterface()) {
         throw new IllegalArgumentException("Interfaces are not accepted in get last event, use a specific event class.");
      }
      synchronized (cacheLock) {
         List eventCache = (List) cacheByEvent.get(eventClass);
         if (eventCache == null || eventCache.size() == 0) {
            return null;
         }
         return eventCache.get(0);
      }
   }

   /**
    * @param eventClass an index into the cache, cannot be an inteface
    *
    * @return the last events published for this event class, or null if caching is turned off (the default)
    */
   public List getCachedEvents(Class eventClass) {
      if (eventClass.isInterface()) {
         throw new IllegalArgumentException("Interfaces are not accepted in get last event, use a specific event class.");
      }
      synchronized (cacheLock) {
         List eventCache = (List) cacheByEvent.get(eventClass);
         if (eventCache == null || eventCache.size() == 0) {
            return null;
         }
         return eventCache;
      }
   }

   /**
    * @param topic an index into the cache
    *
    * @return the last data Object published on this topic, or null if caching is turned off (the default)
    */
   public Object getLastTopicData(String topic) {
      synchronized (cacheLock) {
         List topicCache = (List) cacheByTopic.get(topic);
         if (topicCache == null || topicCache.size() == 0) {
            return null;
         }
         return topicCache.get(0);
      }
   }

   /**
    * @param topic an index into the cache
    *
    * @return the last data Objects published on this topic, or null if caching is turned off (the default)
    */
   public List getCachedTopicData(String topic) {
      synchronized (cacheLock) {
         List topicCache = (List) cacheByTopic.get(topic);
         if (topicCache == null || topicCache.size() == 0) {
            return null;
         }
         return topicCache;
      }
   }

   /**
    * Clears the event cache for a specific event class or interface and it's any of it's subclasses or implementing
    * classes.
    *
    * @param eventClassToClear the event class to clear the cache for
    */
   public void clearCache(Class eventClassToClear) {
      synchronized (cacheLock) {
         Set classes = cacheByEvent.keySet();
         for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
            Class cachedClass = (Class) iterator.next();
            if (eventClassToClear.isAssignableFrom(cachedClass)) {
               iterator.remove();
            }
         }
      }
   }

   /**
    * Clears the topic data cache for a specific topic name.
    *
    * @param topic the topic name to clear the cache for
    */
   public void clearCache(String topic) {
      synchronized (cacheLock) {
         cacheByTopic.remove(topic);
      }
   }

   /**
    * Clears the topic data cache for all topics that match a particular pattern.
    *
    * @param pattern the pattern to match topic caches to
    */
   public void clearCache(Pattern pattern) {
      synchronized (cacheLock) {
         Set classes = cacheByTopic.keySet();
         for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
            String cachedTopic = (String) iterator.next();
            if (pattern.matcher(cachedTopic).matches()) {
               iterator.remove();
            }
         }
      }
   }

   /** Clear all event caches for all topics and event. */
   public void clearCache() {
      synchronized (cacheLock) {
         cacheByEvent.clear();
         cacheByTopic.clear();
      }
   }

   /** Called during veto exceptions, calls handleException */
   protected void subscribeVetoException(final Object event, final String topic, final Object eventObj,
           Throwable e, StackTraceElement[] callingStack, VetoEventListener vetoer) {
      String str = "EventService veto event listener r:" + vetoer;
      if (vetoer != null) {
         str = str + ".  Vetoer class:" + vetoer.getClass();
      }
      handleException("vetoing", event, topic, eventObj, e, callingStack, str);
   }

   /** Called during event handling exceptions, calls handleException */
   protected void onEventException(final String topic, final Object eventObj, Throwable e,
           StackTraceElement[] callingStack, EventTopicSubscriber eventTopicSubscriber) {
      String str = "EventService topic subscriber:" + eventTopicSubscriber;
      if (eventTopicSubscriber != null) {
         str = str + ".  Subscriber class:" + eventTopicSubscriber.getClass();
      }
      handleException("handling event", null, topic, eventObj, e, callingStack, str);
   }

   /** Called during event handling exceptions, calls handleException */
   protected void handleException(final Object event, Throwable e,
           StackTraceElement[] callingStack, EventSubscriber eventSubscriber) {
      String str = "EventService subscriber:" + eventSubscriber;
      if (eventSubscriber != null) {
         str = str + ".  Subscriber class:" + eventSubscriber.getClass();
      }
      handleException("handling event topic", event, null, null, e, callingStack, str);
   }

   /** All exception handling goes through this method.  Logs a warning by default. */
   protected void handleException(final String action, final Object event, final String topic,
           final Object eventObj, Throwable e, StackTraceElement[] callingStack, String sourceString) {
      String eventClassString = (event == null ? "none" : event.getClass().getName());
      String eventString = event + "";
      String contextMsg = "Exception " + action + " event class=" + eventClassString
              + ", event=" + eventString + ", topic=" + topic + ", eventObj=" + eventObj;
      SwingException clientEx = new SwingException(contextMsg, e, callingStack);
      String msg = "Exception thrown by;" + sourceString;
      LOG.log(Level.WARNING, msg, clientEx);
   }

}
