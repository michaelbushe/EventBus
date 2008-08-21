package org.bushe.swing.event;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestEventServiceLocatorConfiguration3 extends TestCase {

   public static class ES1 extends ThreadSafeEventService {

   }

   public static class ES2 extends ThreadSafeEventService {

   }

   public TestEventServiceLocatorConfiguration3(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      System.clearProperty(EventServiceLocator.SWING_EVENT_SERVICE_CLASS);
      System.clearProperty(EventServiceLocator.EVENT_BUS_CLASS);
   }

   public void testConfigurableEventService3() {
      System.setProperty(EventServiceLocator.EVENT_BUS_CLASS, ES2.class.getName());
      EventService es = EventServiceLocator.getEventBusService();
      assertTrue(es instanceof ES2);
      es = EventServiceLocator.getSwingEventService();
      assertTrue(es instanceof SwingEventService);
   }

}