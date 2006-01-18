/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bushe.swing.event;

import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestContainerEventService extends TestCase {
   private ArrayList subscribedEvents;
   private Object aSource = new Object();
   private JFrame frame;
   private JPanel panel;

   public TestContainerEventService(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      subscribedEvents = new ArrayList();
      frame = new JFrame();
      panel = new JPanel();
      frame.setContentPane(panel);
   }

   public void testContainerEventServiceFinder() {
      JButton button = new JButton("Foo");
      panel.add(button);
      JButton barButton = new JButton("Bar");
      panel.add(barButton);
      EventService es = ContainerEventServiceFinder.getEventService(button);
      assertTrue(EventBus.getGlobalEventService() != es);
      EventService esBar = ContainerEventServiceFinder.getEventService(barButton);
      assertEquals(esBar, es);
      assertEquals(0, subscribedEvents.size());
      es.subscribe("FooTopic", new EventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      esBar.publish("FooTopic", "Foo");
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(1, subscribedEvents.size());
   }

   public void testContainerEventServiceSupplier() {
      JButton button = new JButton("Foo");
      JPanel subPanel = new JPanel();
      subPanel.add(button);
      panel.add(subPanel);
      JButton barButton = new JButton("Bar");
      JPanel subPanel2 = new ContainerEventServiceSupplierPanel();
      subPanel2.add(barButton);
      panel.add(subPanel2);
      EventService es = ContainerEventServiceFinder.getEventService(button);
      assertTrue(EventBus.getGlobalEventService() != es);
      EventService esBar = ContainerEventServiceFinder.getEventService(barButton);
      assertTrue(esBar != es);
      assertEquals(0, subscribedEvents.size());
      es.subscribe("FooTopic", new EventTopicSubscriber() {
         public void onEvent(String topic, Object evt) {
            subscribedEvents.add(evt);
         }
      });
      esBar.publish("FooTopic", "Foo");
      try {
         Thread.sleep(500);//Calling hte EDT, need to slow this thread
      } catch (InterruptedException e) {
      }
      assertEquals(0, subscribedEvents.size());
   }

   public void testContainerEventServiceRegistrar() {
      final Object[] lastEventObject = new Object[1];
      JButton button = new JButton("Foo");
      EventTopicSubscriber buttonContainerTopicSubscriber = new EventTopicSubscriber() {
         public void onEvent(String topic, Object data) {
             lastEventObject[0] = data;
         }
      };
      ContainerEventServiceRegistrar reg = new ContainerEventServiceRegistrar(button, buttonContainerTopicSubscriber, "RegEvent");
      EventService es = reg.getContainerEventService();
      assertTrue(es != null);
      EventBus.publish("RegEvent", "WrongBus");
      assertEquals(lastEventObject[0], null);
      JPanel subPanel = new JPanel();
      subPanel.add(button);
      ContainerEventServiceSupplierPanel subPanel2 = new ContainerEventServiceSupplierPanel();
      panel.add(subPanel);
      panel.add(subPanel2);
      EventService es2 = reg.getContainerEventService();
      assertTrue(es2 != es);
      EventBus.publish("RegEvent", "WrongBus");
      assertEquals(lastEventObject[0], null);
      EventService topPanelES = ContainerEventServiceFinder.getEventService(panel);
      topPanelES.publish("RegEvent", "TopLevelBus");
      assertEquals("TopLevelBus", lastEventObject[0]);
      EventService subPanel2ES = subPanel2.getContainerEventService();
      subPanel2ES.publish("RegEvent", "SuppliedBus");
      assertEquals( "TopLevelBus", lastEventObject[0]);//still
      subPanel2.add(button);
      subPanel2ES.publish("RegEvent", "SuppliedBus");
      assertEquals("SuppliedBus", lastEventObject[0]);//detected move
      subPanel.add(button);
      topPanelES.publish("RegEvent", "TopLevelBus");
      assertEquals("TopLevelBus", lastEventObject[0]);
      subPanel2ES.publish("RegEvent", "SuppliedBus");
      assertEquals("TopLevelBus", lastEventObject[0]);
   }

   class ContainerEventServiceSupplierPanel extends JPanel implements ContainerEventServiceSupplier {
      private EventService es = new SwingEventService();

      public EventService getContainerEventService() {
         return es;
      }
   }
}
