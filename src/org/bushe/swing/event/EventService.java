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


/**
 * The core interface.  An EventService provides publish/subscribe event services to a single JVM.
 * <p>
 * The EventService publishes {@link EventServiceEvent}s for type-safe event listening or publishes
 * any object on a topic name (String).  See package documentation for usage details.
 * <p>
 * Subscribers are subscribed by event class or topic name.
 * <p>
 * Subscribing an EventSubscriber to an EventServiceEvent class subscribes a subscriber to be notified on the publication
 * of an EventServiceEvent class or any of its subclasses. Optionally, EventSubscribers can be subscribed the exact
 * class (and not subsclasses) by using the {@link #subscribeExactly(Class, EventSubscriber)} method.
 * <p>
 * Similarly, an EventTopicSubscriber can be subscribed to a specific topic name, or a RegEx Pattern such that a
 * subscriber will be notified whenever an event is published on a topic name that matches the subscribing pattern.
 * <p>
 * A single subscriber cannot subscribe more than once to an event or topic name.  EventService implementations
 * should handle double-subscription requests by returing false on subscribe().  A single EventSubscriber can subscribe
 * to more than one event class, and a single EventTopicSubscriber can subscribe to more than one topic name or pattern.
 * A single object can implement both EventSubscriber and EventTopicSubscriber interfaces.  Subscribers are guaranteed
 * to only be called for the classes and/or topic names they subscribe to.  If a subscriber subscribes to a topic and
 * to a regular expression that matches the topic name, this is considered two different subscriptions and the
 * subscriber will be called twice for the publication on the topic.  Similarly, if a subscriber subscribes to a class
 * and its subclasses using subscribe() and again to a class of the same type using subscribeExactly(), this is
 * considered two different subscriptions and the subscriber will be called twice for the publication for a single event
 * of the exact type.
 * <p>
 * By default the EventService only holds WeakReferences to subscribers.  If a subscriber has no references to it, then
 * it can be garbage collected.  This avoids memory leaks in exchange for the risk of accidently adding a listener and
 * have it disappear unexpectedly.  If you want to subscribe a subscriber that will have no other reference to it, then
 * use one of the subscribeStrongly() methods, which will prevent garbage collection.
 * <p>
 * Unless garbage collected, EventSubscribers will remain subscribed until they are passed to one of the
 * unsubscribe() methods with the event class or topic name to which there are subscribed.
 * <p>
 * Publication on a class or topic name can be vetoed by a {@link VetoEventListener}.
 * All VetoEventListeners are checked before any EventSubscribers or EventTopicSubscribers are called.
 * This is unlike the JavaBean's VetoPropertyEventListener which can leave side effects of half-propogated events.
 * VetoEventListeners are subscribed in the same manner as EventSubscribers and EventTopicSubscribers.
 * <p>
 * Subscribers are called in the order in which they are subscribed by default (FIFO).  This is also unlike Swing, where
 * event listeners are called in the reverse order of when they were subscribed (FILO).
 * <p>
 * This simple example prints "Hello World"
 * <pre>
 * //Create a subscriber
 * EventTopicSubscriber subscriber = new EventTopicSubscriber() {
 *    public void onEvent(String topic, Object evt) {
 *        System.out.println(topic+" "+evt);
 *    }
 * });
 * EventBus.subscribe("Hello", subscriber);
 * EventBus.publish("Hello", "World");
 * System.out.println(subscriber + " This is just to make sure the subscriber didn't get garbage collected, not necessary if you use subscribeStrongly()");
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
    * Subscribes a <b>WeakReference</b> to an EventSubscriber to the publication of events of an event class and
    * its subclasses.
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
    * Subscribes a <b>WeakReference</b> to an EventSubscriber to the publication of events of an event class (not
    * its subclasses).
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
   public boolean subscribeExactly(Class eventClass, EventSubscriber subscriber);

   /**
    * Subscribes a <b>WeakReference</b> to an EventSubscriber to the publication of a topic name.
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
    * Subscribes a <b>WeakReference</b> to an EventSubscriber to the publication of all the
    * topic names that match a RegEx Pattern.
    * <p>
    * Subscription is weak by default to avoid having to call unsubscribe(), and to avoid the memory leaks that would
    * occur if unsubscribe was not called.  The service will respect the WeakReference semantics.  In other words, if
    * the subscriber has not been garbage collected, then the onEvent will be called normally.  If the hard reference
    * has been garbage collected, the service will unsubscribe it's WeakReference.
    * <p>
    * It's allowable to call unsubscribe() with the same EventSubscriber hard reference to stop a subscription
    * immediately.
    * <p>
    * @param topicPattern pattern that matches to the name of the topic published to
    * @param subscriber The topic subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribe(Pattern topicPattern, EventTopicSubscriber subscriber);

   /**
    * Subscribes a subscriber to an event class and its subclasses.
    * <p>
    * The subscriber will remain subscribed until {@link #unsubscribe(Class, EventSubscriber)}  is called.
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribeStrongly(Class eventClass, EventSubscriber subscriber);

   /**
    * Subscribes a subscriber to an event class (and not its subclasses).
    * <p>
    * The subscriber will remain subscribed until {@link #unsubscribe(Class, EventSubscriber)}  is called.
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribeExactlyStrongly(Class eventClass, EventSubscriber subscriber);

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
    * Subscribes a subscriber to all the event topic names that match a RegEx expression.
    * <p>
    * The subscriber will remain subscribed until {@link #unsubscribe(String, EventTopicSubscriber)}  is called.
    * @param topicPattern the name of the topic listened to
    * @param subscriber The topic subscriber that will accept the events when published.
    *
    * @return true if the subscriber was subscribed sucessfully, false otherwise
    */
   public boolean subscribeStrongly(Pattern topicPattern, EventTopicSubscriber subscriber);

   /**
    * Stop the subscription for a subscriber that is subscribed to an event class and its subclasses.
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that is subscribed to the event.  The same reference as that was subscribed.
    *
    * @return true if the subscriber was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(Class eventClass, EventSubscriber subscriber);

   /**
    * Stop the subscription for a subscriber that is subscribed to an event class (and not its subclasses).
    *
    * @param eventClass the class of EventServiceEvent listened to
    * @param subscriber The subscriber that is subscribed to the event.  The same reference as that was subscribed.
    *
    * @return true if the subscriber was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribeExactly(Class eventClass, EventSubscriber subscriber);

   /**
    * Stop the subscription for a subscriber that is subscribed to an event topic
    *
    * @param topic the topic listened to
    * @param subscriber The subscriber that is subscribed to the topic. The same reference as that was subscribed.
    *
    * @return true if the subscriber was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(String topic, EventTopicSubscriber subscriber);

   /**
    * Stop the subscription for a subscriber that is subscribed to an event topic
    *
    * @param topicPattern the regex expression matching topics listened to
    * @param subscriber The subscriber that is subscribed to the topic. The same reference as that was subscribed.
    *
    * @return true if the subscriber was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribe(Pattern topicPattern, EventTopicSubscriber subscriber);

   /**
    * Subscribes a <b>WeakReference</b> to a VetoListener to a event class and its subclasses.
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
    * Subscribes a <b>WeakReference</b> to a VetoListener to a event class (but not its subclasses).
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
   public boolean subscribeVetoListenerExactly(Class eventClass, VetoEventListener vetoListener);

   /**
    * Subscribes a <b>WeakReference</b> to a VetoTopicEventListener to a topic name.
    *
    * @param topic the name of the topic listened to
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListener(String topic, VetoTopicEventListener vetoListener);

   /**
    * Subscribes a <b>WeakReference</b> to an VetoTopicEventListener to all the topic names that
    * match the RegEx Pattern.
    *
    * @param topicPattern the RegEx pattern to match topics with
    * @param vetoListener The vetoListener that can determine whether an event is published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListener(Pattern topicPattern, VetoTopicEventListener vetoListener);

   /**
    * Subscribes a VetoListener for an event class and its subclasses.
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
    * Subscribes a VetoListener for an event class (but not its subclasses).
    * <p>
    * The VetoListener will remain subscribed until {@link #unsubscribeVetoListener(Class, VetoEventListener)}
    * is called.
    * @param eventClass the class of EventServiceEvent listened to
    * @param vetoListener The vetoListener that will accept the events when published.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    */
   public boolean subscribeVetoListenerExactlyStrongly(Class eventClass, VetoEventListener vetoListener);

   /**
    * Subscribes a VetoListener to a topic name.
    * <p>
    * The VetoListener will remain subscribed until {@link #unsubscribeVetoListener(String, VetoTopicEventListener)}
    * is called.
    * @param topic the name of the topic listened to
    * @param vetoListener The topic vetoListener that will accept or reject publication.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    *
    * @see #subscribeVetoListenerStrongly(Class, VetoEventListener)
    */
   public boolean subscribeVetoListenerStrongly(String topic, VetoTopicEventListener vetoListener);

   /**
    * Subscribes a VetoTopicEventListener to a set of topics that match a RegEx expression.
    * <p>
    * The VetoListener will remain subscribed until {@link #unsubscribeVetoListener(Pattern, VetoTopicEventListener)}
    * is called.
    * @param topicPattern the RegEx pattern that matches the name of the topics listened to
    * @param vetoListener The topic vetoListener that will accept or reject publication.
    *
    * @return true if the vetoListener was subscribed sucessfully, false otherwise
    *
    * @see #subscribeVetoListenerStrongly(Pattern, VetoTopicEventListener)
    */
   public boolean subscribeVetoListenerStrongly(Pattern topicPattern, VetoTopicEventListener vetoListener);

   /**
    * Stop the subscription for a vetoListener that is subscribed to an event class and its subclasses.
    *
    * @param eventClass the class of EventServiceEvent that can be vetoed
    * @param vetoListener The vetoListener that will accept or reject publication of an event.
    *
    * @return true if the vetoListener was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribeVetoListener(Class eventClass, VetoEventListener vetoListener);

   /**
    * Stop the subscription for a vetoListener that is subscribed to an event class (but not its subclasses).
    *
    * @param eventClass the class of EventServiceEvent that can be vetoed
    * @param vetoListener The vetoListener that will accept or reject publication of an event.
    *
    * @return true if the vetoListener was subscribed to the event, false if it wasn't
    */
   public boolean unsubscribeVetoListenerExactly(Class eventClass, VetoEventListener vetoListener);

   /**
    * Stop the subscription for a VetoTopicEventListener that is subscribed to an event topic name.
    *
    * @param topic the name of the topic that is listened to
    * @param vetoListener The vetoListener that can determine whether an event is published on that topic
    *
    * @return true if the vetoListener was subscribed to the topic, false if it wasn't
    */
   public boolean unsubscribeVetoListener(String topic, VetoTopicEventListener vetoListener);

   /**
    * Stop the subscription for a VetoTopicEventListener that is subscribed to an event topic RegEx pattern.
    *
    * @param topicPattern the RegEx pattern matching the name of the topics listened to
    * @param vetoListener The vetoListener that can determine whether an event is published on that topic
    *
    * @return true if the vetoListener was subscribed to the topicPattern, false if it wasn't
    */
   public boolean unsubscribeVetoListener(Pattern topicPattern, VetoTopicEventListener vetoListener);

   /**
    * Union of getSubscribersToType(Class) and getSubscribersToExactClass(Class)
    * @param eventClass the eventClass of interest
    * @return the subscribers that will be called when an event of eventClass is published,
    * this includes those subscribed that match by exact class and those that match to a class and its subtypes
    */
   public List getSubscribers(Class eventClass);

   /**
    * @param eventClass the eventClass of interest
    * @return the subscribers that are subscribed to match to a class and its subtypes,
    * but not those subscribed by exact class
    */
   public List getSubscribersToType(Class eventClass);

   /**
    * @param eventClass the eventClass of interest
    * @return the subscribers that are subscribed by exact class
    * but not those subscribed to match to a class and its subtypes
    */
   public List getSubscribersToExactClass(Class eventClass);

   /**
    * Union of getSubscribersByPattern(String) and geSubscribersToTopic(String)
    * @param topic the topic of interest
    * @return the subscribers that will be called when an event is published on the topic.  This includes subscribers
    * subsribed to match the exact topic name and those subscribed by a RegEx Pattern that matches the topic name.
    */
   public List getSubscribers(String topic);

   /**
    * @param topic the topic of interest
    * @return the subscribers that subscribed to the exact topic name.
    */
   public List getSubscribersToTopic(String topic);

   /**
    * @param pattern the RegEx pattern for the topic of interest
    * @return the subscribers that subscribed by a RegEx Pattern that matches the topic name.
    */
   public List getSubscribersByPattern(String pattern);

   /**
    * @param eventClass the eventClass of interest
    * @return the veto subscribers that will be called when an event of eventClass or its subclasses is published.
    */
   public List getVetoSubscribers(Class eventClass);

   /**
    * @param eventClass the eventClass of interest
    * @return the veto subscribers that will be called when an event of eventClass (but not its subclasses) is published.
    */
   public List getVetoSubscribersToExactClass(Class eventClass);

   /**
    * @param eventClass the eventClass of interest
    * @return the veto subscribers that are subscribed to the eventClass and its subclasses
    */
   public List getVetoSubscribersToType(Class eventClass);

   /**
    * @param topic the topic of interest
    * @return the veto subscribers that will be called when an event is published on the topic.
    */
   public List getVetoSubscribers(String topic);

   /**
    * @param pattern the RegEx pattern for the topic of interest
    * @return the veto subscribers that will be called when an event is published on the topic.
    */
   public List getVetoSubscribers(Pattern pattern);

   /**
    * Clears all current subscribers and veto subscribers
    */
   public void clearAllSubscribers();
}
