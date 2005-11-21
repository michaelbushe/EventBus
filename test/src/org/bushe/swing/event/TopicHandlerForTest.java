package org.bushe.swing.event;

/**
 * @author Michael Bushe
 * @since Nov 19, 2005 11:00:53 PM
 */
class TopicHandlerForTest implements EventTopicHandler {
   private boolean throwException;
   private Long waitTime;
   private EBTestCounter testDefaultEventService;

   public TopicHandlerForTest(EBTestCounter testDefaultEventService, Long waitTime) {
      this.testDefaultEventService = testDefaultEventService;
      this.waitTime = waitTime;
   }

   public TopicHandlerForTest(EBTestCounter testDefaultEventService, boolean throwException) {
      this.testDefaultEventService = testDefaultEventService;
      this.throwException = throwException;
   }

   public void handleEvent(String topic, Object evt) {
      if (waitTime != null) {
         try {
            Thread.sleep(waitTime.longValue());
         } catch (InterruptedException e) {
         }
      }
      testDefaultEventService.eventsHandledCount++;
      if (throwException) {
         testDefaultEventService.handleExceptionCount++;
         throw new IllegalArgumentException();
      }
   }
}
