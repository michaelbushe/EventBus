package org.bushe.swing.event.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.ref.WeakReference;

import org.bushe.swing.event.EventService;

/**
 * A class is subscribed to an EventService on behalf of another object.
 */
public class ProxySubscriber extends AbstractProxySubscriber implements org.bushe.swing.event.EventSubscriber {
   private Class subscription;

   /**
    * Creates a proxy.  This does not subscribe it.
    * @param realSubscriber the subscriber that the proxy will call when an event is publixhed
    * @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
    * @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber should be too
    * @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer exist
    * @param subscription the class to subscribe to, used for unsubscription only
    */
   public ProxySubscriber(Object realSubscriber, Method subscriptionMethod, ReferenceStrength referenceStrength,
           EventService es, Class subscription) {
      super(realSubscriber, subscriptionMethod, referenceStrength, es);
      this.subscription = subscription;
      Class[] params = subscriptionMethod.getParameterTypes();
      if (params == null || params.length != 1 || params[0].isPrimitive()) {
         throw new IllegalArgumentException("The subscriptionMethod must have a single non-primitive parameter.");
      }
   }

   /**
    * Handles the event publication by pushing it to the real subscriber's subcription Method.
    *
    * @param event The Object that is being published.
    */
   public void onEvent(Object event) {
      Object[] args = new Object[]{event};
      try {
         Object obj = realSubscriber;
         if (referenceStrength == ReferenceStrength.WEAK) {
            obj = ((WeakReference)realSubscriber).get();
            if (obj == null) {
               eventService.unsubscribe(subscription, this);
               realSubscriber = null;
               subscriptionMethod = null;
               referenceStrength = null;
               eventService = null;
               subscription = null;
               return;
            }
         }
         subscriptionMethod.invoke(obj, args);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("IllegalAccessException when invoking annotated method from EventService publication.  Event class:"+event.getClass()+", Event:"+event + ", subscriber:"+realSubscriber+", subscription Method="+subscriptionMethod, e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("InvocationTargetException when invoking annotated method from EventService publication.  Event class:"+event.getClass()+", Event:"+event + ", subscriber:"+realSubscriber+", subscription Method="+subscriptionMethod, e);
      }
   }
}
