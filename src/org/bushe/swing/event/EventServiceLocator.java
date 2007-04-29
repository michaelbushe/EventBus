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

import java.util.HashMap;
import java.util.Map;

/**
 * A ServiceLocator pattern class for getting an instance of an EventService. Nothin' fancy, since the EventService is
 * intended for a single VM. Allows mulitple named EventServices to be discovered.
 * <p/>
 * Holds the singleton Swing event service, which is wrapped by the EventBus, which is returned by getSwingEventService
 * and mapped to the service name SERVICE_NAME_EVENT_BUS ("EventBus").  This is not settable.
 * <p/>
 * Since the default EventService implementation is thread safe, and since it's not good to have lots of events on the
 * EventDispatchThread you may want multiple EventServices running on multiple threads, perhaps pulling events from a
 * server and coalescing them into one or more events that are pushed onto the EDT.
 * <p/>
 * The EventServiceLocator is used by the EventBus to locate the implementation of the SwingEventService the EventBus
 * delegates all its calls to.  To change the default implementation class from SwingEventService to your own class,
 * call:
 * <pre>
 * System.setProperty(EventServiceLocator.SWING_EVENT_SERVICE_CLASS, YourEventServiceImpl.class.getName());
 * </pre>
 * Likewise, you can set this on the command line via -Dorg.bushe.swing.event.swingEventServiceClass=foo.YourEventServiceImpl
 *
 * @author Michael Bushe michael@bushe.com
 */
public class EventServiceLocator {
   /** The name "EventBus" is reserved for the service that the EventBus wraps. */
   public static final String SERVICE_NAME_EVENT_BUS = "EventBus";
   /**
    * Set this Java property to a Class that implements EventService to use that class instead of SwingEventService as
    * the swing EventBus.  Must be set on startup or before the SwingEventService is created.
    */
   public static final String SWING_EVENT_SERVICE_CLASS = "org.bushe.swing.event.swingEventServiceClass";

   private static EventService SWING_EVENT_SERVICE;
   private static final Map EVENT_SERVICES = new HashMap();

   /** @return the singleton default instance of a SwingEventService */
   public static synchronized EventService getSwingEventService() {
      if (SWING_EVENT_SERVICE == null) {
         String swingEventServiceClass = System.getProperty(SWING_EVENT_SERVICE_CLASS);
         if (swingEventServiceClass != null) {
            Class sesClass;
            try {
               sesClass = Class.forName(swingEventServiceClass);
            } catch (ClassNotFoundException e) {
               throw new RuntimeException("Could not find class specified in the property " + SWING_EVENT_SERVICE_CLASS + ".  Class=" + swingEventServiceClass, e);
            }
            Object service;
            try {
               service = sesClass.newInstance();
            } catch (InstantiationException e) {
               throw new RuntimeException("InstantiationException creating instance of class set from Java property" + SWING_EVENT_SERVICE_CLASS + ".  Class=" + swingEventServiceClass, e);
            } catch (IllegalAccessException e) {
               throw new RuntimeException("IllegalAccessException creating instance of class set from Java property" + SWING_EVENT_SERVICE_CLASS + ".  Class=" + swingEventServiceClass, e);
            }
            try {
               SWING_EVENT_SERVICE = (EventService) service;
            } catch (ClassCastException ex) {
               throw new RuntimeException("ClassCastException casting to " + EventService.class + " from instance of class set from Java property" + SWING_EVENT_SERVICE_CLASS + ".  Class=" + swingEventServiceClass, ex);
            }
         } else {
            SWING_EVENT_SERVICE = new SwingEventService();
         }
         EVENT_SERVICES.put(SERVICE_NAME_EVENT_BUS, SWING_EVENT_SERVICE);
      }
      return SWING_EVENT_SERVICE;
   }

   /**
    * @param serviceName the service name of the EventService, as registered by #setEventService(String, EventService),
    * or SERVICE_NAME_EVENT_BUS.
    *
    * @return a named event service instance
    */
   public static synchronized EventService getEventService(String serviceName) {
      EventService es = (EventService) EVENT_SERVICES.get(serviceName);
      if (es == null && SERVICE_NAME_EVENT_BUS.equals(serviceName)) {
         es = getSwingEventService();
      }
      return es;
   }

   /**
    * Add a named EventService to the locator.
    * <p/>
    * <b>Using this method does not change the EventBus implementation or the getSwingEventService() result.</b>  See
    * this class' javadoc for information on how to change the EventBus implementation.
    *
    * @param serviceName a named event service instance
    * @param es the EventService to attach to the service name
    *
    * @throws EventServiceExistsException if a service by this name already exists or if serviceName is "EventBus"
    */
   public static synchronized void setEventService(String serviceName,
           EventService es) throws EventServiceExistsException {
      if (EVENT_SERVICES.get(serviceName) != null && es != null) {
         throw new EventServiceExistsException("An event service by the name " + serviceName + "already exists.  Perhaps multiple threads tried to create a service about the same time?");
      } else if (SERVICE_NAME_EVENT_BUS.equals(serviceName)) {
         throw new EventServiceExistsException("You cannot use this method to set the EventBus implementation.  Set the Java property " + SWING_EVENT_SERVICE_CLASS + " to the implementation class instead.");
      } else {
         EVENT_SERVICES.put(serviceName, es);
      }
   }
}