package org.bushe.swing.event;

/**
 * @author Michael Bushe
 * @since Nov 19, 2005 11:00:42 PM
 */
class VetoEventListenerForTest implements VetoEventListener {
   private boolean throwException;

   VetoEventListenerForTest() {
      this(false);
   }
   VetoEventListenerForTest(boolean throwException) {
      this.throwException = throwException;
   }
   public boolean shouldVeto(Object evt) {
      if (throwException) {
         throw new IllegalArgumentException("veto ex");
      }
      return true;
   }
}
