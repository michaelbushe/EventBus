package org.bushe.swing.event.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.ProxySubscriber;
import org.bushe.swing.event.EventBus;

/** Common base class for EventService Proxies */
public abstract class AbstractProxySubscriber implements ProxySubscriber {
   protected Object realSubscriber;
   protected Method subscriptionMethod;
   protected ReferenceStrength referenceStrength;
   protected EventService eventService;

   protected AbstractProxySubscriber(Object realSubscriber, Method subscriptionMethod,
           ReferenceStrength referenceStrength, EventService es) {
      this.referenceStrength = referenceStrength;
      eventService = es;
      if (realSubscriber == null) {
         throw new IllegalArgumentException("The realSubscriber cannot be null when constructing a proxy subscriber.");
      }
      if (subscriptionMethod == null) {
         throw new IllegalArgumentException("The subscriptionMethod cannot be null when constructing a proxy subscriber.");
      }
      //Always strong, see:
      //https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=19499&forumID=1834
      this.realSubscriber = realSubscriber;
      this.subscriptionMethod = subscriptionMethod;
   }

   /** @return the object this proxy is subscribed on behalf of */
   public Object getProxiedSubscriber() {
      return realSubscriber;
   }

   /**
    * Called by EventServices to inform the proxy that it is unsubscribed.  The ProxySubscriber should null the
    * reference to it's proxied subscriber
    */
   public void proxyUnsubscribed() {
      realSubscriber = null;
   }

   public int hashCode() {
      int result = 0;
      if (realSubscriber != null) {
         result = realSubscriber.hashCode();
      }
      if (eventService != null) {
         result = result^eventService.hashCode();
      }
      if (referenceStrength != null) {
         result = result^referenceStrength.hashCode();
      }
      return result;
   }

   public boolean equals(Object obj) {
      if (obj instanceof AbstractProxySubscriber) {
         AbstractProxySubscriber bps = (AbstractProxySubscriber) obj;
         if (realSubscriber != bps.realSubscriber) {
            return false;
         }
         if (eventService != bps.eventService) {
            return false;
         }
         if (referenceStrength != bps.referenceStrength) {
            return false;
         }
         if (subscriptionMethod != bps.subscriptionMethod) {
            return false;
         }
         return true;
      } else {
         return false;
      }
   }


   public String toString() {
      return "AbstractProxySubscriber{" +
              "realSubscriber=" + realSubscriber +
              ", subscriptionMethod=" + subscriptionMethod +
              ", referenceStrength=" + referenceStrength +
              ", eventService=" + eventService +
              '}';
   }
}
