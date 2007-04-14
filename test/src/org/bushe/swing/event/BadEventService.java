package org.bushe.swing.event;

public class BadEventService extends ThreadSafeEventService {


   /** @see org.bushe.swing.event.EventService#subscribe(String,org.bushe.swing.event.EventTopicSubscriber) */
   public boolean subscribe(String topic, EventTopicSubscriber eh) {
      throw new RuntimeException("For testing");
   }
}
