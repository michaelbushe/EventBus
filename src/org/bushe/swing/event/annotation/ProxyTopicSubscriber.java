package org.bushe.swing.event.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bushe.swing.event.EventService;

/** A class that subscribes to an EventService on behalf of another object. */
public class ProxyTopicSubscriber extends AbstractProxySubscriber implements org.bushe.swing.event.EventTopicSubscriber {
   private String topic;

   /**
    * Creates a proxy.  This does not subscribe it.
    *
    * @param proxiedSubscriber the subscriber that the proxy will call when an event is published
    * @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
    * @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber should
    * be too
    * @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer
    * exist
    * @param topic the topic to subscribe to, used for unsubscription only
    */
   public ProxyTopicSubscriber(Object proxiedSubscriber, Method subscriptionMethod, ReferenceStrength referenceStrength,
           EventService es, String topic) {
      super(proxiedSubscriber, subscriptionMethod, referenceStrength, es);
      this.topic = topic;
      Class[] params = subscriptionMethod.getParameterTypes();
      if (params == null || params.length != 2 || !String.class.equals(params[0]) || params[1].isPrimitive()) {
         throw new IllegalArgumentException("The subscriptionMethod must have the two parameters, the first one must be a String and the second a non-primitive (Object or derivative).");
      }
   }

   /**
    * Handles the event publication by pushing it to the real subscriber's subcription Method.
    *
    * @param topic the topic on which the object is being published
    * @param data The Object that is being published on the topic.
    */
   public void onEvent(String topic, Object data) {
      Object[] args = new Object[]{topic, data};
      try {
         Object obj = getProxiedSubscriber();
         if (obj == null) {
            return;
         }
         getSubscriptionMethod().invoke(obj, args);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("IllegalAccessException when invoking annotated method from EventService publication.  Topic:" + topic + ", data:" + data + ", subscriber:" + getProxiedSubscriber() + ", subscription Method=" + getSubscriptionMethod(), e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("InvocationTargetException when invoking annotated method from EventService publication.  Topic:" + topic + ", data:" + data + ", subscriber:" + getProxiedSubscriber() + ", subscription Method=" + getSubscriptionMethod(), e);
      }
   }

   protected void unsubscribe(String topic) {
      getEventService().unsubscribe(topic, this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ProxyTopicSubscriber) {
         if (!super.equals(obj)) {
            return false;
         }
         ProxyTopicSubscriber proxyTopicSubscriber = (ProxyTopicSubscriber) obj;
         if (topic != proxyTopicSubscriber.topic) {
            if (topic == null) {
               return false;
            } else {
               if (!topic.equals(proxyTopicSubscriber.topic)) {
                  return false;
               }
            }
         }
         return true;
      } else {
         return false;
      }
   }


   @Override
   public String toString() {
      return "ProxyTopicSubscriber{" +
              "topic='" + topic + '\'' +
              "realSubscriber=" + getProxiedSubscriber() +
              ", subscriptionMethod=" + getSubscriptionMethod() +
              ", referenceStrength=" + getReferenceStrength() +
              ", eventService=" + getEventService() +
              '}';
   }
}
