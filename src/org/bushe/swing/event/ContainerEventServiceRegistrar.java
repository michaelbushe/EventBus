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

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Registers a component with it's Container's EventService.
 * <p>
 * Registering with a component's ContainerEventService is tricky since components may not be in their hierarchy when
 * they want to register with it, or components may move (though rarely).  This class handles the of registering a
 * component with it's container event service.  It automatically registers when the Container becomes available to a
 * component, and unregisters and re-registers when the component is moved.
 *
 * @author Michael Bushe michael@bushe.com
 */
public class ContainerEventServiceRegistrar {
   private JComponent jComp;
   private EventHandler eventHandler;
   private Class[] eventClasses;
   private EventTopicHandler eventTopicHandler;
   private String[] topics;
   private EventService containerEventService;

   /**
    * Create a registrar that will keep track of the container event service, typically used in the
    * publish-only cases where the getContainerEventServer() call will be made before publication.
    *
    * @param jComp the component whose container to monitor
    */
   public ContainerEventServiceRegistrar(JComponent jComp) {
      this(jComp, null, (Class[]) null, null, null);
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the handler
    * to the eventClass when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventHandler the handler to register to the Container EventServer
    * @param eventClass the class of event to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, EventHandler eventHandler, Class eventClass) {
      this(jComp, eventHandler, new Class[]{eventClass}, null, null);
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the handler
    * to the topic when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventTopicHandler the topic handler to register to the Container EventServer
    * @param topic the event topic name to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, EventTopicHandler eventTopicHandler, String topic) {
      this(jComp, null, null, eventTopicHandler, new String[]{topic});
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the handler
    * to the event classes when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventHandler the handler to register to the Container EventServer
    * @param eventClasses the classes of event to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, EventHandler eventHandler, Class[] eventClasses) {
      this(jComp, eventHandler, eventClasses, null, null);
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the handler
    * to the topics when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventTopicHandler the topic handler to register to the Container EventServer
    * @param topics the event topic names to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, EventTopicHandler eventTopicHandler, String[] topics) {
      this(jComp, null, null, eventTopicHandler, topics);
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the handler to the topics
    * and the event classes when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventHandler the handler to register to the Container EventServer
    * @param eventClasses the classes of event to register for
    * @param topics the event topic names to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, EventHandler eventHandler, Class[] eventClasses,
           EventTopicHandler eventTopicHandler, String[] topics) {
      this.jComp = jComp;
      this.eventHandler = eventHandler;
      this.eventClasses = eventClasses;
      this.eventTopicHandler = eventTopicHandler;
      this.topics = topics;
      if (jComp == null) {
         throw new NullPointerException("JComponent is null");
      }
      updateContainerEventService();
      jComp.addHierarchyListener(new HierarchyListener() {
         public void hierarchyChanged(HierarchyEvent e) {
            updateContainerEventService();
         }
      });
      jComp.addContainerListener(new ContainerListener() {
         public void componentAdded(ContainerEvent e) {
            updateContainerEventService();
         }

         public void componentRemoved(ContainerEvent e) {
            updateContainerEventService();
         }
      });
      jComp.addAncestorListener(new AncestorListener() {
         public void ancestorAdded(AncestorEvent event) {
            updateContainerEventService();
         }

         public void ancestorMoved(AncestorEvent event) {
            updateContainerEventService();
         }

         public void ancestorRemoved(AncestorEvent event) {
            updateContainerEventService();
         }
      });
   }

   /**
    * Called by this class when the container may have changed.
    * <p/>
    * Override this method and call super if your class wants to be notified when the container changes (compare the
    * references of getContainerEventService() around the calls to super.updateContainerEventService()).
    */
   protected void updateContainerEventService() {
      if (containerEventService != null) {
         if (eventClasses != null) {
            for (int i = 0; i < eventClasses.length; i++) {
               Class eventClass = eventClasses[i];
               containerEventService.unsubscribe(eventClass, eventHandler);
            }
         }
         if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
               String topic = topics[i];
               containerEventService.unsubscribe(topic, eventTopicHandler);
            }
         }
      }

      containerEventService = ContainerEventServiceFinder.getEventService(jComp);
      if (containerEventService != null) {
         if (eventClasses != null) {
            for (int i = 0; i < eventClasses.length; i++) {
               Class eventClass = eventClasses[i];
               containerEventService.subscribe(eventClass, eventHandler);
            }
         }
         if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
               String topic = topics[i];
               containerEventService.subscribe(topic, eventTopicHandler);
            }
         }
      }
   }

   /**
    * @return the container event service, if null, it tries to find it, but it still may be null if this object is not
    *         in a container.
    */
   public EventService getContainerEventService() {
      if (containerEventService != null) {
         return containerEventService;
      } else {
         updateContainerEventService();
         return containerEventService;
      }
   }
}
