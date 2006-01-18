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


/**
 * The core interface.  An EventService provides publish/subscribeStrongly event services to a single JVM.
 * <p>
 * The EventService publishes {@link EventServiceEvent}s for type-safe event listening or
 * any object on a topic names String.
 * <p>
 * Subscribers are subscribed by event class or topic name.  Subscribing an EventSubscriber to an EventServiceEvent class
 * subscribes to the exact class.  The subscriber is subscribed only to that eventServiceEvent class and it will
 * not be notified of publication on subclasses or superclasses of the class specifically subscribed.
 * <p>
 * A single subscriber cannot subscribeStrongly more than once to an event or topic name, however implementations
 * should ignore double-subscription requests silently.  A single EventSubscriber can subscribeStrongly to more than one
 * event class, and a single EventTopicSubscriber can subscribeStrongly to more than one topic name.  A single object cna implement
 * both interfaces.  Subscribers are guaranteed to only be called for the classes and/or topic names they subscribeStrongly to.
 * <p>
 * By default the EventService only holds WeakReferences to subscribers.  If a subscriber has no references to it, then
 * it can be garbage collected .  This avoids memory leaks in exchange for the risk of accidently adding a listener and
 * have it disappear unexpectedly.  If a subscriber is a non-static inner class, then it will stay subscribed at least
 * until the instance of its parent class is garbage collected since all inner classes maintain an implicit reference
 * to their parent instance.  If you want to subscribeStrongly a subscriber that will have no other reference to it, then
 * use one of the subscribeStrongly() methods, which will prevent garbage collection.
 * <p>
 * Unless garbage collected, EventSubscribers will remain subscribed until they are passed to one of the
 * unsubscribe() methods with the event class or topic name to which there are subscribed.
 * <p>
 * Publication of an EventServiceEvent or an object on class or topic name can be vetoed by a {@link VetoEventListener}.
 * All VetoEventListeners are checked before any EventSubscribers or EventTopicSubscribers are called.
 * This is unlike the JavaBean's VetoPropertyEventListener which can leave side effects of half-propogated events.
 * VetoEventListeners are subscribed in the same manner as EventSubscribers and EventTopicSubscribers.
 * <p>
 * Subscribers are called in the order in which they are subscribed by default (FIFO).  This is also unlike Swing, where
 * event listeners are called in the reverse order of when they were subscribed (FILO).
 * <p>
 * This simple example prints "Hello World"
 * <pre>
 * EventBus.subscribeStrongly("Hello", new EventTopicSubscriber() {
 *    public void onEvent(String topic, Object evt) {
 *        System.out.println(topic+" "+evt);
 *    }
 * });
 * EventBus.publish("Hello", "World");
 * </pre>
 * @author Michael Bushe michael@bushe.com
 * @see ThreadSafeEventService for the default implementation
 * @see SwingEventService for the Swing-safe implementation
 * @see EventBus for simple access to the Swing-safe implementation
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
    * Subscribes a <b>WeakReference</b> to an EventSubscriber to the publication of events of an event class.
    * <p>
    * Note that Java inner classes have an implicit reference to their parent class.  So an inner class
    * that is subscribed to an EventService will remian subscribed at least until their parent class instance is
    * garbage collected (or unsubscribe is called).
    * <p>
    * Subscription is weak by default to avoid having to call unsubscribe(), and to avoid the memory leaks that would
    * occur if unsubscribe was not called.  The service will respect the WeakReference semantics.  In other words, if
    * the subscriber has not been garbage collected, then the onEvent will be called normally.  If the hard reference
    * has been garbage collected, the service will unsubscribe it's WeakReference.
    * <p>
    * It's allowable to call unsubscribe() with the same EventSubscriber hard reference to stop a subscription
    * immediately.
    * <p>
    * The service will create the WeakReference
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribe(Class eventClass, EventSubscriber subscriber);

   /**
    * Subscribes a <b>WeakReference</b> to an EventSubscriber to the publication of a topic name.
    * <p>
    * Note that Java inner classes have an implicit reference to their parent class.  So an inner class
    * that is subscribed to an EventService will remian subscribed at least until their parent class instance is
    * garbage collected (or unsubscribe is called).
    * <p>
    * Subscription is weak by default to avoid having to call unsubscribe(), and to avoid the memory leaks that would
    * occur if unsubscribe was not called.  The service will respect the WeakReference semantics.  In other words, if
    * the subscriber has not been garbage collected, then the onEvent will be called normally.  If the hard reference
    * has been garbage collected, the service will unsubscribe it's WeakReference.
    * <p>
    * It's allowable to call unsubscribe() with the same EventSubscriber hard reference to stop a subscription
    * immediately.
    * <p>
    * @param topic the name of the topic listened to
    * @param subscriber The topic subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribe(String topic, EventTopicSubscriber subscriber);

   /**
    * Subscribes a subscriber to an event class.
    * <p>
    * The subscriber will remain subscribed until {@link #unsubscribe(Class, EventSubscriber)}  is called.
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribeStrongly(Class eventClass, EventSubscriber subscriber);

   /**
    * Subscribes a subscriber to an event topic name.
    * <p>
    * The subscriber will remain subscribed until {@link #unsubscribe(String, EventTopicSubscriber)}  is called.
    * @param topic the name of the topic listened to
    * @param subscriber The topic subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribeStrongly(String topic, EventTopicSubscriber subscriber);

   /**
    * Stop the subscription for a subscriber to an event class
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that is subscribed to the event.  The same reference as that passed to subscribeStrongly
    *
    * @return true if the subscriber was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(Class eventClass, EventSubscriber subscriber);

   /**
    * Stop the subscription for a subscriber to an event topic
    *
    * @param topic the topic listened to
    * @param subscriber The subscriber that is subscribed to the topic. The same reference as that passed to subscribeStrongly
    *
    * @return true if the subscriber was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(String topic, EventTopicSubscriber subscriber);

   /**
    * Subscribes a <b>WeakReference</b> to an VetoListener to a event class.
    * <p/>
    * Use this method to avoid having to call unsubscribe(), though with care since garbage collection semantics is
    * indeterminate.  The service will respect the WeakReference semantics.  In other words, if the vetoListener has
    * not been garbage collected, then the onEvent will be called normally.  If the hard reference has been garbage
    * collected, the service will unsubscribe it's WeakReference.
    * <p/>
    * It's allowable to call unsubscribe() with the same VetoListener hard reference to stop a subscription
    * immediately.
    * <p/>
    * The service will create the WeakReference
    * @param eventClass the class of EventServiceEvent that can be vetoed
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListener(Class eventClass, VetoEventListener vetoListener);

   /**
    * Subscribes a <b>WeakReference</b> to an EventTopicSubscriber to a topic name.
    * <p/>
    * For WeakReference semantics, see {@link #subscribeVetoListener(Class, VetoEventListener)}
    * <p/>
    *
    * @param topic the name of the topic listened to
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListener(String topic, VetoEventListener vetoListener);


   /**
    * Subscribes a VetoListener for an event class.
    * <p>
    * The VetoListener will remain subscribed until {@link #unsubscribeVetoListener(Class, VetoEventListener)}
    * is called.
    * @param eventClass the class of EventServiceEvent listened to
    * @param vetoListener The vetoListener that will accept the events when published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListenerStrongly(Class eventClass, VetoEventListener vetoListener);

   /**
    * Subscribes a VetoListener to a topic name.
    * <p>
    * The VetoListener will remain subscribed until {@link #unsubscribeVetoListener(String, VetoEventListener)}
    * is called.
    * @param topic the name of the topic listened to
    * @param vetoListener The topic vetoListener that will accept the events when published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    *
    * @see #subscribeVetoListenerStrongly(Class, VetoEventListener)
    */
   public boolean subscribeVetoListenerStrongly(String topic, VetoEventListener vetoListener);

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
    * @param eventClass the eventClass of interest
    * @return the subscribers that will be called when an event of eventClass is published.
    */
   public List getSubscribers(Class eventClass);

   /**
    * @param topic the topic of interest
    * @return the subscribers that will be called when an event is published on the topic.
    */
   public List getSubscribers(String topic);

   /**
    * @param eventClass the eventClass of interest
    * @return the veto subscribers that will be called when an event of eventClass is published.
    */
   public List getVetoSubscribers(Class eventClass);

   /**
    * @param topic the topic of interest
    * @return the veto subscribers that will be called when an event is published on the topic.
    */
   public List getVetoSubscribers(String topic);

   /**
    * Clears all current subscribers and veto subscribers
    */
   public void clearAllSubscribers();
}
