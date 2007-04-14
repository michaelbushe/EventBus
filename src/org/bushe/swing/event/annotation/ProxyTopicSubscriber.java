package org.bushe.swing.event.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.ref.WeakReference;

import org.bushe.swing.event.EventService;

/**
 * A class that subscribes to an EventService on behalf of another object.
 */
public class ProxyTopicSubscriber extends AbstractProxySubscriber implements org.bushe.swing.event.EventTopicSubscriber  {
   private String topic;

   /**
    * Creates a proxy.  This does not subscribe it.
    * @param realSubscriber the subscriber that the proxy will call when an event is publixhed
    * @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
    * @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber should be too
    * @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer exist
    * @param topic the topic to subscribe to, used for unsubscription only
    */
   public ProxyTopicSubscriber(Object realSubscriber, Method subscriptionMethod, ReferenceStrength referenceStrength,
           EventService es, String topic) {
      super(realSubscriber, subscriptionMethod, referenceStrength, es);
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
         Object obj = realSubscriber;
         subscriptionMethod.invoke(obj, args);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("IllegalAccessException when invoking annotated method from EventService publication.  Topic:"+topic+", data:"+data + ", subscriber:"+realSubscriber+", subscription Method="+subscriptionMethod, e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("InvocationTargetException when invoking annotated method from EventService publication.  Topic:"+topic+", data:"+data + ", subscriber:"+realSubscriber+", subscription Method="+subscriptionMethod, e);
      }
   }

   protected void unsubscribe(String topic) {
      eventService.unsubscribe(topic, this);
   }
}
