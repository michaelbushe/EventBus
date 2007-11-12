package org.bushe.swing.event.annotation;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.ProxySubscriber;

/** Common base class for EventService Proxies */
public abstract class AbstractProxySubscriber implements ProxySubscriber {
   private Object proxiedSubscriber;
   private Method subscriptionMethod;
   private ReferenceStrength referenceStrength;
   private EventService eventService;

   protected AbstractProxySubscriber(Object proxiedSubscriber, Method subscriptionMethod,
           ReferenceStrength referenceStrength, EventService es) {
      this.referenceStrength = referenceStrength;
      eventService = es;
      if (proxiedSubscriber == null) {
         throw new IllegalArgumentException("The realSubscriber cannot be null when constructing a proxy subscriber.");
      }
      if (subscriptionMethod == null) {
         throw new IllegalArgumentException("The subscriptionMethod cannot be null when constructing a proxy subscriber.");
      }
      if (ReferenceStrength.WEAK.equals(referenceStrength)) {
         this.proxiedSubscriber = new WeakReference(proxiedSubscriber);
      } else {
         this.proxiedSubscriber = proxiedSubscriber;
      }
      this.subscriptionMethod = subscriptionMethod;
   }

   /** @return the object this proxy is subscribed on behalf of */
   public Object getProxiedSubscriber() {
      if (proxiedSubscriber instanceof WeakReference) {
         return ((WeakReference)proxiedSubscriber).get();
      }
      return proxiedSubscriber;
   }
   
   /** @return the subscriptionMethod passed in the constructor */
   public Method getSubscriptionMethod() {
      return subscriptionMethod;
   }

   /** @return the EventService passed in the constructor */
   public EventService getEventService() {
      return eventService;
   }

   /** @return the ReferenceStregth passed in the constructor */
   public ReferenceStrength getReferenceStrength() {
      return referenceStrength;
   }

   /**
    * Called by EventServices to inform the proxy that it is unsubscribed.  
    * The ProxySubscriber should perform any necessary cleanup.
    * <p>
    * <b>Overridding classes must call super.proxyUnsubscribed() or risk
    * things not being cleanup up properly.</b>
    */
   public void proxyUnsubscribed() {
      proxiedSubscriber = null;
   }

   @Override
   public final int hashCode() {
      throw new RuntimeException("Proxy subscribers are not allowed in Hash " +
              "Maps, since the underlying values use Weak References that" +
              "may disappear, the calculations may not be the same in" +
              "successive calls as required by hashCode.");
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof AbstractProxySubscriber) {
         AbstractProxySubscriber bps = (AbstractProxySubscriber) obj;
         if (referenceStrength != bps.referenceStrength) {
            return false;
         }
         if (subscriptionMethod != bps.subscriptionMethod) {
            return false;
         }
         if (ReferenceStrength.WEAK == referenceStrength) {
            if (((WeakReference)proxiedSubscriber).get() != ((WeakReference)bps.proxiedSubscriber).get()) {
               return false;
            }            
         } else {
            if (proxiedSubscriber != bps.proxiedSubscriber) {
               return false;
            }
         }
         if (eventService != bps.eventService) {
            return false;
         }
         return true;
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return "AbstractProxySubscriber{" +
              "realSubscriber=" + (proxiedSubscriber instanceof WeakReference?
                       ((WeakReference)proxiedSubscriber).get():proxiedSubscriber) +
              ", subscriptionMethod=" + subscriptionMethod +
              ", referenceStrength=" + referenceStrength +
              ", eventService=" + eventService +
              '}';
   }
}
