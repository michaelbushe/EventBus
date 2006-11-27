package org.bushe.swing.event.annotation;

import java.lang.reflect.Method;
import java.lang.ref.WeakReference;

import org.bushe.swing.event.EventService;

/**
 * Common base class for EventService Proxies
 */
public abstract class AbstractProxySubscriber {
   protected Object realSubscriber;
   protected Method subscriptionMethod;
   protected ReferenceStrength referenceStrength;
   protected EventService eventService;

   protected AbstractProxySubscriber(Object realSubscriber, Method subscriptionMethod, ReferenceStrength referenceStrength,
           EventService es) {
      this.referenceStrength = referenceStrength;
      eventService = es;
      if (realSubscriber == null) {
         throw new IllegalArgumentException("The realSubscriber cannot be null when constructing a proxy subscriber.");
      }
      if (subscriptionMethod == null) {
         throw new IllegalArgumentException("The subscriptionMethod cannot be null when constructing a proxy subscriber.");
      }
      if (referenceStrength == ReferenceStrength.WEAK) {
         this.realSubscriber = new WeakReference(realSubscriber);
      } else {
         this.realSubscriber = realSubscriber;
      }
      this.subscriptionMethod = subscriptionMethod;
   }
}
