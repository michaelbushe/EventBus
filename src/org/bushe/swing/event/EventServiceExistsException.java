package org.bushe.swing.event;

/** Exception thrown by the EventServiceLocator when an exception exists. */
public class EventServiceExistsException extends Exception {
   public EventServiceExistsException(String msg) {
      super(msg);
   }
}
