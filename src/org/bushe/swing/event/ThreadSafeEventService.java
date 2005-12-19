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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bushe.swing.exception.SwingException;

/**
 * A thread-safe EventService implementation that can be used anywhere, even on a server. This implementation is <b>not
 * Swing thread-safe</b>, meaning if called on a thread other than the Swing EventDispatchThread, subscribers will
 * receive the event on the calling thread, and not the EDT.  Swing components generally prefer  updates on the EDT (but
 * one could imagine a multi-thread safe TableModel that ensured TableChangeEvents are on the EDT. Swing components
 * should use the SwingEventService instead, which is the default implementation returned from the EventBus.
 * <p/>
 * On event publication, handlers are called in the order in which they subscribed by default.  To force the
 * order of publication for a handler, you can subscribe with a weighting Number.
 * <p>
 * To debug problems with your usage of the event service you may find it helpful to turn on fine logging for the logger
 * named "org.bushe.swing.event.ThreadSafeEventService" (java.util.logging).
 * <p/>
 * If you are concerned that your subscribers take too long (a concern in Swing applications), then you can force the
 * service to issue {@link EventHandlerTimingEvent}s when subscribers exceed a certain time.  This does not interrupt
 * subscriber processing and is published after the subscriber finishes.  You can have the service log a warning for
 * EventHandlerTimingEvents, see the constructor {@link ThreadSafeEventService (long, boolean)}.  The timing is checked
 * for veto subscribers too.
 * <p/>
 * Multithreaded note: Two threads may be accessing the ThreadSafeEventService at the same time, on unsubscribing a
 * listener for topic "A" and the other publishing on topic "A".  If the unsubsubscribing thread gets the lock first,
 * then it is unsubscubscribed, end of story.  If the publisher gets the lock first, then a snapshot copy of the current
 * listeners is made during the publication, the lock is released and the subscribers are called.  Between the time the
 * lock is released and the time that the listener is called, the unsubscribing thread can unsubscribe, resulting in an
 * unsubscribed object receiving notifiction of the event.
 * <p>
 * Exceptions are logged by default, override {@link #handleException(String, EventServiceEvent, String, Object, Throwable, StackTraceElement[], String)}
 * to handle exceptions in another way.  Each call to a subscriber is wrapped in a try block to ensure one listener
 * does not interfere with another.
 * @author Michael Bushe michael@bushe.com
 * @todo perhaps publication should double-check to see if a subscriber is still subscribed
 * @see EventService for a complete description of the API
 */
public class ThreadSafeEventService implements EventService {
   protected static final Logger LOG = Logger.getLogger(EventService.class.getName());

   private Map handlersByEventClass = new HashMap();
   private Map handlersByTopic = new HashMap();
   private Map vetoListenersByClass = new HashMap();
   private Map vetoListenersByTopic = new HashMap();
   private Object listenerLock = new Object();
   private Long timeThresholdForEventTimingEventPublication;

   /**
    * Creates a ThreadSafeEventService while providing time monitoring options.
    *
    * @param timeThresholdForEventTimingEventPublication the longest time a handler should spend handling an event,
    * The service will pulish an EventHandlerTimingEvent after listener processing if the time was exceeded.  If null,
    * no EventHandlerTimingEvent will be issued.
    */
   public ThreadSafeEventService(Long timeThresholdForEventTimingEventPublication) {
      this(timeThresholdForEventTimingEventPublication, false);
   }

   /**
    * Creates a ThreadSafeEventService while providing time monitoring options.
    *
    * @param timeThresholdForEventTimingEventPublication the longest time a handler should spend handling an event.
    * The service will pulish an EventHandlerTimingEvent after listener processing if the time was exceeded.  If null,
    * no EventHandlerTimingEvent will be issued.
    * @param handleTimingEventsInternally add a subscriber to the EventHandlerTimingEvent internally and call the
    * protected handleTiming() method when they occur.  This logs a warning to a java.util.logging logger by default.
    * @throws IllegalArgumentException if timeThresholdForEventTimingEventPublication is null and handleTimingEventsInternally
    * is true.
    * @todo (non Swing-only?) start a timer call and when it calls back, report the time if exceeded.
    */
   public ThreadSafeEventService(Long timeThresholdForEventTimingEventPublication, boolean handleTimingEventsInternally) {
      if (timeThresholdForEventTimingEventPublication == null && handleTimingEventsInternally) {
         throw new IllegalArgumentException("null, true in constructor is not valid.  If you want to send timing messages for all events and handle them internally, pass 0, true");
      }
      this.timeThresholdForEventTimingEventPublication = timeThresholdForEventTimingEventPublication;
      if (handleTimingEventsInternally) {
         //Listen to timing events and log them
         subscribeStrongly(EventHandlerTimingEvent.class, new EventHandler() {
            public void handleEvent(EventServiceEvent evt) {
               handleTiming((EventHandlerTimingEvent) evt);
            }
         });
      }
   }

   /** @see EventService#subscribe(Class, EventHandler) */
   public boolean subscribe(Class cl, EventHandler eh) {
      if (cl == null) {
         throw new IllegalArgumentException("Event class must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event handler must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by class, class:" + cl + ", handler:" + eh);
      }
      return subscribe(cl, handlersByEventClass, new WeakReference(eh));
   }

   /** @see EventService#subscribe(String, EventTopicHandler) */
   public boolean subscribe(String topic, EventTopicHandler eh) {
      if (topic == null) {
         throw new IllegalArgumentException("Topic must not be null");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Event topic handler must not be null");
      }
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing by topic name, name:" + topic + ", handler:" + eh);
      }
      return subscribe(topic, handlersByTopic, new WeakReference(eh));
   }

   /** @see EventService#subscribeStrongly(Class, EventHandler) */
   public boolean subscribeStrongly(Class cl, EventHandler eh) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing weakly by class, class:" + cl + ", handler:" + eh);
      }
      if (eh == null) {
         throw new IllegalArgumentException("Handler cannot be null.");
      }
      return subscribe(cl, handlersByEventClass, eh);
   }

   /** @see EventService#subscribeStrongly(String, EventTopicHandler) */
   public boolean subscribeStrongly(String name, EventTopicHandler eh) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Subscribing weakly by topic name, name:" + name + ", handler:" + eh);
      }
      if (eh == null) {
         throw new IllegalArgumentException("Handler cannot be null.");
      }
      return subscribe(name, handlersByTopic, eh);
   }

   /**
    * All handler subscription methods call this method.  Extending classes only have to override this method to handle
    * all handler subscriptions.
    *
    * @param o the topic String or EventServiceEvent Class to subscribe to
    * @param handlerMap the internal map of handlers to use (by topic or class)
    * @param eh the EventHandler or EventTopicHandler to subscribe, or a WeakReference to either
    *
    * @return boolean if the handler is subscribed (was not subscribed).
    *
    * @throws IllegalArgumentException if eh or o is null
    * @todo (param) a JMS-like selector (can be done in base classes by implements like a commons filter
    * @todo (param) register a Comparator to sort subscriber's calling order - for a class or topic
    * @todo is it worth the overhead to check that o (if a Class) implements EventServiceEvent ?
    */
   protected boolean subscribe(final Object o, final Map handlerMap, final Object eh) {
      if (o == null) {
         throw new IllegalArgumentException("Can't subscribe to null.");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Can't subscribe null handler to " + o);
      }
      boolean alreadyExists = false;
      synchronized (listenerLock) {
         List handlers = (List) handlerMap.get(o);
         if (handlers == null) {
            if (LOG.isLoggable(Level.FINE)) {
               LOG.fine("Creating new handler map for :" + o);
            }
            handlers = new ArrayList();
            handlerMap.put(o, handlers);
         } else {
            //Two weak references to the same object don't compare equal, also need to make sure a weak ref and a hard
            //ref aren't both subscribed
            Object compareEH = eh;
            if (eh instanceof WeakReference) {
               compareEH = ((WeakReference)eh).get();
               if (compareEH == null) {
                  return false;//already garbage collected?  Weird.
               }
            }
            for (Iterator iterator = handlers.iterator(); iterator.hasNext();) {
               Object existingHandler = iterator.next();
               if (existingHandler instanceof WeakReference) {
                  existingHandler = ((WeakReference)existingHandler).get();
                  if (existingHandler == null) {
                     iterator.remove();//was garbage collected
                  }
               }
               if (compareEH.equals(existingHandler)) {
                  iterator.remove();//will add to the end of the calling list
                  alreadyExists = true;
               }
            }
         }
         handlers.add(eh);
         return !alreadyExists;
      }
   }

   /** @see EventService#subscribeVetoListener(Class, VetoEventListener) */
   public boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return subscribeVetoListener(eventClass, vetoListenersByClass, vetoListener);
   }

   /** @see EventService#subscribeVetoListener(String, VetoEventListener) */
   public boolean subscribeVetoListener(String topic, VetoEventListener vetoListener) {
      return subscribeVetoListener(topic, vetoListenersByTopic, vetoListener);
   }

   /** @see EventService#subscribeVetoListenerStrongly(Class, VetoEventListener) */
   public boolean subscribeVetoListenerStrongly(Class eventClass, VetoEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      return subscribeVetoListener(eventClass, vetoListenersByClass, new WeakReference(vetoListener));
   }

   /** @see EventService#subscribeVetoListenerStrongly(String, VetoEventListener) */
   public boolean subscribeVetoListenerStrongly(String topic, VetoEventListener vetoListener) {
      if (vetoListener == null) {
         throw new IllegalArgumentException("VetoListener cannot be null.");
      }
      return subscribeVetoListener(topic, vetoListenersByTopic, new WeakReference(vetoListener));
   }

   /** @see org.bushe.swing.event.EventService#clearAllSubscribers()  */
   public void clearAllSubscribers() {
      synchronized(listenerLock) {
         this.handlersByEventClass.clear();
         this.handlersByTopic.clear();
         this.vetoListenersByClass.clear();
         this.vetoListenersByTopic.clear();
      }
   }

   /**
    * All veto subscriptions methods call this method.  Extending classes only have to override this method to handle
    * all veto subscriptions.
    *
    * @param o the topic or EventServiceEvent class to subsribe to
    * @param vetoListenerMap the internal map of veto listeners to use (by topic of class)
    * @param vl the veto listener to subscribe, may be a VetoEventListener or a WeakReference to one
    *
    * @return boolean if the veto listener is subscribed (was not subscribed).
    *
    * @throws IllegalArgumentException if vl or o is null
    */
   protected boolean subscribeVetoListener(final Object o, final Map vetoListenerMap, final Object vl) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("subscribeVetoListener(" + o + "," + vl + ")");
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

   /** @see EventService#unsubscribe(Class, EventHandler) */
   public boolean unsubscribe(Class cl, EventHandler eh) {
      return unsubscribe(cl, handlersByEventClass, eh);
   }

   /** @see EventService#unsubscribe(String, EventTopicHandler) */
   public boolean unsubscribe(String name, EventTopicHandler eh) {
      return unsubscribe(name, handlersByTopic, eh);
   }

   /**
    * All event handler unsubscriptions call this method.  Extending classes only have to override this method to
    * handle all handler unsubscriptions.
    *
    * @param o the topic or event class to unsubsribe from
    * @param handlerMap the map of handlers to use (by topic of class)
    * @param eh the handler to unsubscribe, either an EventHandler or an EventTopicHandler, or a WeakReference to
    * either
    *
    * @return boolean if the handler is unsubscribed (was subscribed).
    */
   protected boolean unsubscribe(Object o, Map handlerMap, Object eh) {
      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("unsubscribe(" + o + "," + eh + ")");
      }
      if (o == null) {
         throw new IllegalArgumentException("Can't unsubscribe to null.");
      }
      if (eh == null) {
         throw new IllegalArgumentException("Can't unsubscribe null handler to " + o);
      }
      synchronized (listenerLock) {
         return removeFromSetResolveWeakReferences(handlerMap, o, eh);

      }
   }

   /** @see EventService#unsubscribeVetoListener(String, VetoEventListener) */
   public boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener) {
      return unsubscribeVetoListener(eventClass, vetoListenersByClass, vetoListener);
   }

   /** @see EventService#unsubscribeVetoListener(String, VetoEventListener) */
   public boolean unsubscribeVetoListener(String topic, VetoEventListener vetoListener) {
      return unsubscribeVetoListener(topic, vetoListenersByTopic, vetoListener);
   }

   /**
    * All veto unsubscriptions methods call this method.  Extending classes only have to override this method to handle
    * all veto unsubscriptions.
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

   /** @see EventService#publish(EventServiceEvent) */
   public void publish(EventServiceEvent evt) {
      if (evt == null) {
         throw new IllegalArgumentException("Cannot publish null event.");
      }
      publish(evt, null, null, getSubscribers(evt.getClass()), getVetoSubscribers(evt.getClass()), null);
   }

   /** @see EventService#publish(String, Object) */
   public void publish(String topicName, Object evtObj) {
      publish(null, topicName, evtObj, getSubscribers(topicName), getVetoSubscribers(topicName), null);
   }

   /**
    * All publish methods call this method.  Extending classes only have to override this method to handle all
    * publishing cases.
    *
    * @param event the event to publish, null if publishing on a topic
    * @param topic if publishing on a topic, the topic to publish on, else null
    * @param evtObj if publishing on a topic, the evtObj to publish, else null
    * @param subscribers the subscribers to publish to - must be a snapshot copy
    * @param vetoSubscribers the veto subscribers to publish to - must be a snapshot copy.
    *
    * @throws IllegalArgumentException if eh or o is null
    */
   protected void publish(final EventServiceEvent event, final String topic, final Object evtObj,
           final List subscribers, final List vetoSubscribers, StackTraceElement[] callingStack) {

      if (event == null && topic == null) {
         throw new IllegalArgumentException("Can't publish to null topic/event.");
      }

      //topic or event
      if (LOG.isLoggable(Level.FINE)) {
         if (event != null) {
            LOG.fine("Publishing event: class=" + event.getClass() + ", event=" + event);
         } else if (topic != null) {
            LOG.fine("Publishing event: topic=" + topic + ", evtObj=" + evtObj);
         }
      }

      if (subscribers == null || subscribers.isEmpty()) {
         if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("No subscribers for event or topic. Event:"+event+ ", Topic:"+topic);
         }
         return;
      }

      //Check all veto subscribers, if any veto, then don't publish
      if (vetoSubscribers != null && !vetoSubscribers.isEmpty()) {
         for (Iterator vlIter = vetoSubscribers.iterator(); vlIter.hasNext();) {
            VetoEventListener vl = (VetoEventListener) vlIter.next();
            long start = System.currentTimeMillis();
            try {
               if (vl.shouldVeto(event)) {
                  handleVeto(event, vl, topic, evtObj);
                  checkTimeLimit(start, event, null, vl);
                  if (LOG.isLoggable(Level.FINE)) {
                     LOG.fine("Publication vetoed. Event:"+event+ ", Topic:"+topic+", veto subscriber:"+vl);
                  }
                  return;
               }
            } catch (Throwable ex) {
               checkTimeLimit(start, event, null, vl);
               handleVetoException(event, topic, evtObj, ex, callingStack, vl);
            }
         }
      }

      if (LOG.isLoggable(Level.FINE)) {
         LOG.fine("Publishing to subscribers :"+subscribers);
      }

      for (int i = 0; i < subscribers.size(); i++) {
         Object eh = subscribers.get(i);
         if (event != null) {
            EventHandler eventHandler = (EventHandler) eh;
            long start = System.currentTimeMillis();
            try {
               eventHandler.handleEvent(event);
               checkTimeLimit(start, event, eventHandler, null);
            } catch (Throwable e) {
               checkTimeLimit(start, event, eventHandler, null);
               handleEventHandlingException(event, e, callingStack, eventHandler);
            }
         } else {
            EventTopicHandler eventTopicHandler = (EventTopicHandler) eh;
            try {
               eventTopicHandler.handleEvent(topic, evtObj);
            } catch (Throwable e) {
               handleEventHandlingException(topic, evtObj, e, callingStack, eventTopicHandler);
            }
         }
      }
   }

   public List getSubscribers(Class eventClass) {
      synchronized (listenerLock) {
         return getSubscribers(eventClass, handlersByEventClass);
      }
   }

   public List getSubscribers(String topic) {
      synchronized (listenerLock) {
         return getSubscribers(topic, handlersByTopic);
      }
   }

   public List getVetoSubscribers(Class eventClass) {
      synchronized (listenerLock) {
         return getSubscribers(eventClass, vetoListenersByClass);
      }
   }

   public List getVetoSubscribers(String topic) {
      synchronized (listenerLock) {
         return getSubscribers(topic, vetoListenersByTopic);
      }
   }

   private List getSubscribers(Object classOrTopic, Map subscriberMap) {
      synchronized (listenerLock) {
         List subscribers = (List) subscriberMap.get(classOrTopic);
         //Make a defensive copy of handlers and veto listeners so listeners
         //can change the listener list while the listeners are being called
         //Resolve WeakReferences and unsubscribe if necessary.
         return createCopyOfContentsRemoveWeakRefs(subscribers);
      }
   }

   private void checkTimeLimit(long start, EventServiceEvent event, EventHandler handler, VetoEventListener l) {
      if (timeThresholdForEventTimingEventPublication == null) {
         return;
      }
      long end = System.currentTimeMillis();
      if (end - start > timeThresholdForEventTimingEventPublication.longValue()) {
         publish(new EventHandlerTimingEvent(this, new Long(start), new Long(end), timeThresholdForEventTimingEventPublication, event, handler, l));
      }
   }

   protected void handleTiming(EventHandlerTimingEvent evt) {
      LOG.log(Level.WARNING, evt + "");
   }

   private void handleVeto(final EventServiceEvent event, VetoEventListener vl, final String topic,
           final Object evtObj) {
      //@todo register object that want to know about the veto and notify them of the veto
      if (LOG.isLoggable(Level.FINE)) {
         if (event != null) {
            LOG.fine("Vetoing event: class=" + event.getClass() + ", event=" + event + ", vetoer:" + vl);
         } else {
            LOG.fine("Vetoing event: topic=" + topic + ", evtObj=" + evtObj + ", vetoer:" + vl);
         }
      }
   }

   /**
    * Given a Map (of Lists of handlers or veto listeners), removes the toRemove element from the List in the map for
    * the given key.  If WeakReferences are encountered,
    *
    * @param map map of lists
    * @param key key for a List in the map
    * @param toRemove the object to remove form the list with the key of the map
    *
    * @return a copy of the set
    */
   private boolean removeFromSetResolveWeakReferences(Map map, Object key, Object toRemove) {
      List handlers = (List) map.get(key);
      if (handlers == null) {
         return false;
      }
      if (handlers.remove(toRemove)) {
         return true;
      }

      //search for a WeakReference
      for (Iterator iter = handlers.iterator(); iter.hasNext();) {
         Object item = iter.next();
         if (item instanceof WeakReference) {
            WeakReference wr = (WeakReference) item;
            if (wr.get() == null) {
               //clean up a garbage collected reference
               iter.remove();
            } else if (wr.get() == toRemove) {
               iter.remove();
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Given a set (or handlers or veto listeners), makes a copy of the set, resolving WeakReferences to hard
    * references, and removing garbage collected referenences from the original set.
    *
    * @param handlersOrVetoListeners
    *
    * @return a copy of the set
    */
   private List createCopyOfContentsRemoveWeakRefs(Collection handlersOrVetoListeners) {
      if (handlersOrVetoListeners == null) {
         return null;
      }
      List copyOfHandlersOrVetolisteners = new ArrayList(handlersOrVetoListeners.size());
      for (Iterator iter = handlersOrVetoListeners.iterator(); iter.hasNext();) {
         Object elem = iter.next();
         if (elem instanceof WeakReference) {
            Object hardRef = ((WeakReference) elem).get();
            if (hardRef == null) {
               //Was reclaimed, unsubscribe
               iter.remove();
            } else {
               copyOfHandlersOrVetolisteners.add(hardRef);
            }
         } else {
            copyOfHandlersOrVetolisteners.add(elem);
         }
      }
      return copyOfHandlersOrVetolisteners;
   }

   /** Called during veto exceptions, calls handleException*/
   protected void handleVetoException(final EventServiceEvent event, final String topic, final Object evtObj,
           Throwable e, StackTraceElement[] callingStack, VetoEventListener vetoer) {
      String str = "EventService veto event listener r:" + vetoer;
      if (vetoer != null) {
         str = str + ".  Vetoer class:" + vetoer.getClass();
      }
      handleException("vetoing", event, topic, evtObj, e, callingStack, str);
   }

   /** Called during event handling exceptions, calls handleException*/
   protected void handleEventHandlingException(final String topic, final Object evtObj, Throwable e,
           StackTraceElement[] callingStack, EventTopicHandler eventTopicHandler) {
      String str = "EventService topic handler:" + eventTopicHandler;
      if (eventTopicHandler != null) {
         str = str + ".  Handler class:" + eventTopicHandler.getClass();
      }
      handleException("handling event", null, topic, evtObj, e, callingStack, str);
   }

   /** Called during event handling exceptions, calls handleException*/
   protected void handleEventHandlingException(final EventServiceEvent event, Throwable e,
           StackTraceElement[] callingStack, EventHandler eventHandler) {
      String str = "EventService handler:" + eventHandler;
      if (eventHandler != null) {
         str = str + ".  Handler class:" + eventHandler.getClass();
      }
      handleException("handling event topic", event, null, null, e, callingStack, str);
   }

   /** All exception handling goes through this method.  Logs a warning by default.*/
   protected void handleException(final String action, final EventServiceEvent event, final String topic,
           final Object evtObj, Throwable e, StackTraceElement[] callingStack, String sourceString) {
      String contextMsg = "Exception " + action + " event class=" + event == null ? "none" : event.getClass().getName()
              + ", event=" + event + ", topic=" + topic + ", evtObj=" + evtObj;
      SwingException clientEx = new SwingException(contextMsg, e, callingStack);
      String msg = "Exception thrown by;" + sourceString;
      LOG.log(Level.WARNING, msg, clientEx);
   }
}
