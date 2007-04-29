package org.bushe.swing.event;

/**
 * @author Michael Bushe
 * @since Nov 19, 2005 11:00:42 PM
 */
class VetoTopicEventListenerForTest implements VetoTopicEventListener {
   private boolean throwException;

   VetoTopicEventListenerForTest() {
      this(false);
   }

   VetoTopicEventListenerForTest(boolean throwException) {
      this.throwException = throwException;
   }

   public boolean shouldVeto(String topic, Object data) {
      if (throwException) {
         throw new IllegalArgumentException("veto ex");
      }
      return true;
   }
}
