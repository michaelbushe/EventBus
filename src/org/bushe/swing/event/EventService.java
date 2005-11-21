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
 * An EventService provides publish/subscribe event services to a single JVM.
 * <p>
 * The EventService publishes {@link EventServiceEvent}s for type-safe event listening or
 * any object on a topic names String.
 * <p>
 * Subscribers are subscribed by event class or topic name.  Subscribing an EventHandler to an EventServiceEvent class
 * subscribes to the exact class.  The subscriber is subscribed only to that eventServiceEvent class and it will
 * not be notified of publication on subclasses or superclasses of the class specifically subscribed.
 * <p>
 * A single subscriber cannot subscribe more than once to an event or topic name, however implementations
 * should ignore double-subscription requests silently.  A single EventHandler can subscribe to more than one
 * event class, and a single EventTopicHandler can subscribe to more than one topic name.  Subscribers are
 * guaranteed to only be called for the class or topic name they subscribe to.
 * <p>
 * EventHandlers will remain subscribed until they are passed to {@link #unsubscribe(Class, EventHandler)} with the
 * event class to which there are subscribed, or until they are garbage collected if they are subscribed via
 * {@link #subscribeWeakly(Class, EventHandler)}.  EventTopicHandlers will remain subscribed until they are passed to
 * {@link #unsubscribe(String, EventTopicHandler)} with the topic name to which there are subscribed,
 * or until they are garbage collected if they are subscribed via {@link #subscribeWeakly(String, EventTopicHandler)}.
 * <p>
 * Publication of an EventServiceEvent or an object on a topic name can be vetoed by a {@link VetoEventListener}.
 * All VetoEventListeners are checked before any EventHandlers or EventTopicHandlers are called.
 * (This is unlike the JavaBean's VetoPropertyEventListener which can leave side effects of half-propogated events.)
 * VetoEventListeners are subscribed in the same manner as EventHandlers and EventTopicHandlers.
 * <p>
 * This simple example prints "Hello World"
 * <pre>
 * EventBus.subscribe("Hello", new EventTopicHandler() {
 *    public void handleEvent(String topic, Object evt) {
 *        System.out.println(topic+" "+evt);
 *    }
 * });
 * EventBus.publish("Hello", "World");
 * </pre>
 * @author Michael Bushe michael@bushe.com
 * @see {@link ThreadSafeEventService} for the default implementation
 * @see {@link SwingEventService} for the Swing-safe implementation
 * @see {@link EventBus} for simple access to the Swing-safe implementation
 */
public interface EventService {


   /**
    * Publishes an EventServiceEvent so that all subscribers to the EventServiceEvent class will be notified about it.
    *
    * @param evt The event that occured
    */
   public void publish(EventServiceEvent evt);

   /**
    * Publishes an object on a topic name so that all subscribers to that name will be notified about it.
    *
    * @param topic The name of the topic subscribed to
    * @param o the object to publish
    */
   public void publish(String topic, Object o);

   /**
    * Subscribes a handler to an event class.
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param handler The handler that will accept the events when published.
    *
    * @return true if the handler was subscribed sucessfully, false otherwise
    */
   public boolean subscribe(Class eventClass, EventHandler handler);

   /**
    * Subscribes a handler to a topic name
    *
    * @param topic the name of the topic listened to
    * @param handler The topic handler that will accept the events when published.
    *
    * @return true if the handler was subscribed sucessfully, false otherwise
    */
   public boolean subscribe(String topic, EventTopicHandler handler);

   /**
    * Stop the subscription for a handler to an event class
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param handler The handler that is subscribed to the event.  The same reference as that passed to subscribe
    *
    * @return true if the handler was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(Class eventClass, EventHandler handler);

   /**
    * Stop the subscription for a handler to an event topic
    *
    * @param topic the topic listened to
    * @param handler The handler that is subscribed to the topic. The same reference as that passed to subscribe
    *
    * @return true if the handler was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(String topic, EventTopicHandler handler);

   /**
    * Subscribes a <b>WeakReference</b> to an EventHandler to a event class.
    * <p/>
    * Use this method to avoid having to call unsubscribe(), though with care since garbage collection semantics is
    * indeterminate.  The service will respect the WeakReference semantics.  In other words, if the handler has not
    * been garbage collected, then the handleEvent will be called normally.  If the hard reference has been garbage
    * collected, the service will unsubscribe it's WeakReference.
    * <p/>
    * It's allowable to call unsubscribe() with the same EventHandler hard reference to stop a subscription
    * immediately.
    * <p/>
    * The service will create the WeakReference
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param handler The handler that will accept the events when published.
    *
    * @return true if the handler was subscribed sucessfully, false otherwise
    */
   public boolean subscribeWeakly(Class eventClass, EventHandler handler);

   /**
    * Subscribes a <b>WeakReference</b> to an EventTopicHandler to a topic name.
    * <p/>
    * For WeakReference semantics, see {@link #subscribeWeakly(Class, EventHandler)}
    * <p/>
    *
    * @param topic the name of the topic listened to
    * @param handler The topic handler that will accept the events when published.
    *
    * @return true if the handler was subscribed sucessfully, false otherwise
    *
    * @see #subscribeWeakly(Class, EventHandler)
    */
   public boolean subscribeWeakly(String topic, EventTopicHandler handler);

   /**
    * Subscribes a VetoListener for an event class.
    *
    * @param eventClass the class of EventServiceEvent that can be vetoed
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener);

   /**
    * Subscribes a VetoListener to a topic name.
    *
    * @param topic the name of the topic listened to
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListener(String topic, VetoEventListener vetoListener);

   /**
    * Stop the subscription for a vetoListener to an event class.
    *
    * @param eventClass the class of EventServiceEvent that can be vetoed
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener);

   /**
    * Stop the subscription for a vetoListener to an event topic name.
    *
    * @param topic the name of the topic that is listened to
    * @param vetoListener The vetoListener that can determine whether an event is published on that topic
    *
    * @return true if the vetoListener was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribeVetoListener(String topic, VetoEventListener vetoListener);

   /**
    * Subscribes a <b>WeakReference</b> to an VetoListener to a event class.
    * <p/>
    * Use this method to avoid having to call unsubscribe(), though with care since garbage collection semantics is
    * indeterminate.  The service will respect the WeakReference semantics.  In other words, if the vetoListener has
    * not been garbage collected, then the handleEvent will be called normally.  If the hard reference has been garbage
    * collected, the service will unsubscribe it's WeakReference.
    * <p/>
    * It's allowable to call unsubscribe() with the same VetoListener hard reference to stop a subscription
    * immediately.
    * <p/>
    * The service will create the WeakReference
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param vetoListener The vetoListener that will accept the events when published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListenerWeakly(Class eventClass, VetoEventListener vetoListener);

   /**
    * Subscribes a <b>WeakReference</b> to an EventTopicHandler to a topic name.
    * <p/>
    * For WeakReference semantics, see {@link #subscribeVetoListenerWeakly(Class, VetoEventListener)}
    * <p/>
    *
    * @param topic the name of the topic listened to
    * @param vetoListener The topic vetoListener that will accept the events when published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    *
    * @see #subscribeVetoListenerWeakly(Class, VetoEventListener)
    */
   public boolean subscribeVetoListenerWeakly(String topic, VetoEventListener vetoListener);

   /**
    * Clears all current subscribers and veto subscribers
    */
   public void clearAllSubscribers();
}
