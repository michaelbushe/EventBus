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

import java.util.Map;
import java.util.HashMap;

/**
 * A ServiceLocator pattern class for getting an instance of an EventService.
 * Nothin' fancy, since the EventService is intended for a single VM. Allows mulitple
 * named EventServices to be discovered.
 * <p>
 * Holds the singleton Swing event service, which is wrapped by the EventBus,
 * which is mapped to the service name SERVICE_NAME_EVENT_BUS ("EventBus").
 * <p>
 * Since the default EventService implementation is thread safe, and since it's not
 * good to have lots of events on the EventDispatchThread you may want multiple EventServices
 * running on multiple threads, perhaps pulling events from a server and coalescing them
 * into one or more events that are pushed onto the EDT.
 * @author Michael Bushe michael@bushe.com
 */
public class EventServiceLocator {
   /** The name "EventBus" is reserved for the service that the EventBus wraps.*/
   public static final String SERVICE_NAME_EVENT_BUS = "EventBus";
   
   private static EventService SWING_EVENT_SERVICE;
   private static final Map EVENT_SERVICES = new HashMap();

   /** @return the singleton default instance of a SwingEventService  */
   public static synchronized EventService getSwingEventService() {
         if (SWING_EVENT_SERVICE == null) {
             SWING_EVENT_SERVICE = new SwingEventService();
             EVENT_SERVICES.put(SERVICE_NAME_EVENT_BUS, SWING_EVENT_SERVICE);
         }
         return SWING_EVENT_SERVICE;
   }

   /** @return a named event service instance */
   public static synchronized EventService getEventService(String serviceName) {
      return (EventService) EVENT_SERVICES.get(serviceName);
   }

   /**
    * Add a named EventService to the locator
    * @param serviceName a named event service instance
    */
   public static synchronized void setEventService(String serviceName, EventService es) {
      EVENT_SERVICES.put(serviceName, es);
   }
}
