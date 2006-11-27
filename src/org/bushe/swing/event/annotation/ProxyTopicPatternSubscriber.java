package org.bushe.swing.event.annotation;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.bushe.swing.event.EventService;

/**
 *
 */
public class ProxyTopicPatternSubscriber extends ProxyTopicSubscriber {
   private Pattern pattern;

   /**
    * Creates a proxy.  This does not subscribe it.
    *
    * @param realSubscriber the subscriber that the proxy will call when an event is publixhed
    * @param subscriptionMethod the method the proxy will call, must have an Object as it's first and only parameter
    * @param referenceStrength if the subscription is weak, the reference from the proxy to the real subscriber should
    * be too
    * @param es the EventService we will be subscribed to, since we may need to unsubscribe when weak refs no longer
    * exist
    * @param patternString the Regular Expression for topics to subscribe to, used for unsubscription only
    */
   public ProxyTopicPatternSubscriber(Object realSubscriber, Method subscriptionMethod,
           ReferenceStrength referenceStrength,
           EventService es, String patternString, Pattern pattern) {
      super(realSubscriber, subscriptionMethod, referenceStrength, es, patternString);
      this.pattern = pattern;
   }

   protected void unsubscribe(String topic) {
      eventService.unsubscribe(pattern, this);
      pattern = null;
   }
}
