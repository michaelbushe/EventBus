package org.bushe.swing.event.annotation;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.ProxySubscriber;

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
}
