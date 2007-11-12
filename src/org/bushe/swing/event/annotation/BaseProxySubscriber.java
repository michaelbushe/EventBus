package org.bushe.swing.event.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bushe.swing.event.EventService;

/** A class is subscribed to an EventService on behalf of another object. */
public class BaseProxySubscriber extends AbstractProxySubscriber implements org.bushe.swing.event.EventSubscriber {
   private Class subscriptionClass;

   /**
    * Creates a proxy.  This does not subscribe it.
    *
    * @param proxiedSubscriber the subscriber that the proxy will call when an event is publixhed
    * @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
    * @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber should
    * be too
    * @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer
    * exist
    * @param subscription the class to subscribe to, used for unsubscription only
    */
   public BaseProxySubscriber(Object proxiedSubscriber, Method subscriptionMethod, ReferenceStrength referenceStrength,
           EventService es, Class subscription) {
      super(proxiedSubscriber, subscriptionMethod, referenceStrength, es);
      this.subscriptionClass = subscription;
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
         Object obj = getProxiedSubscriber();
         if (obj == null) {
            //has been garbage collected
            return;
         }
         getSubscriptionMethod().invoke(obj, args);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("IllegalAccessException when invoking annotated method from EventService publication.  Event class:" + event.getClass() + ", Event:" + event + ", subscriber:" + getProxiedSubscriber() + ", subscription Method=" + getSubscriptionMethod(), e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("InvocationTargetException when invoking annotated method from EventService publication.  Event class:" + event.getClass() + ", Event:" + event + ", subscriber:" + getProxiedSubscriber() + ", subscription Method=" + getSubscriptionMethod(), e);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BaseProxySubscriber) {
         if (!super.equals(obj)) {
            return false;
         }
         BaseProxySubscriber bps = (BaseProxySubscriber) obj;
         if (subscriptionClass != bps.subscriptionClass) {
            if (subscriptionClass == null) {
               return false;
            } else {
               if (!subscriptionClass.equals(bps.subscriptionClass)) {
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
      return "BaseProxySubscriber{" +
              "subscription=" + subscriptionClass +
              "realSubscriber=" + getProxiedSubscriber() +
              ", subscriptionMethod=" + getSubscriptionMethod() +
              ", referenceStrength=" + getReferenceStrength() +
              ", eventService=" + getEventService() +
              '}';
   }
}
