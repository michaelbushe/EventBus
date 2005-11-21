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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * This abstract class ties the Swing Actions with the Event Bus.  When fired, this action publishes an event on an
 * EventService - either the global EventBus or a Container EventService.
 * <p/>
 * There are two derivatives of this class: The EventBusAction, which publishes the ActionEvent on the global EventBus,
 * and the ContainerEventServiceAction, which publishes the Action on the a local ContainerEventService.
 * <p/>
 * By default a topic is published on the EventService.  The topic name is the action's id (which is usually also the
 * action's command name), and the value published on the topic is the ActionEvent.
 * <p/>
 * To publish an EventServiceEvent instead of publishing on a topic name, override
 * {@link #getEventServiceEvent(ActionEvent)}
 *
 * @author Michael Bushe michael@bushe.com
 */
public abstract class EventServiceAction extends AbstractAction {
   public static final String EVENT_SERVICE_TOPIC_NAME = "event-service-topic";

   private boolean throwsExceptionOnNullEventService = true;

   public EventServiceAction() {
   }

   public EventServiceAction(String actionName, ImageIcon icon) {
      super(actionName, icon);
   }

   /**
    * @param evt the event passed to #execute(ActionEvent)
    * <p>
    * Gets the EventService from {@link #getEventService(ActionEvent evt)}.
    * Gets event from {@link #getEventServiceEvent(java.awt.event.ActionEvent)}
    * if null, gets the topic name from {@link #getTopicName(java.awt.event.ActionEvent)} and
    * the topic value from if null, gets the topic name from {@link #getTopicValue(java.awt.event.ActionEvent)}.
    * Published the event or the value on the topic on the EventService.
    *
    * @return the event service to publish on, if null and getThrowsExceptionOnNullEventService() is true (default) and
    *         exception is thrown
    */
   protected abstract EventService getEventService(ActionEvent evt);

   /**
    * Publishes the event on the EventService returned by getSwingEventService(evt)
    * @param evt the action event to turn into an event bus event.
    * @throws RuntimeException if getThrowsExceptionOnNullEventService() &&  getSwingEventService(evt) == null
    */
   public void actionPerformed(ActionEvent evt) {
      EventService eventService = getEventService(evt);
      if (eventService == null) {
         if (getThrowsExceptionOnNullEventService()) {
            throw new RuntimeException("Null EventService supplied to EventServiceAction with name:" + getName());
         } else {
            return;
         }
      }
      EventServiceEvent event = getEventServiceEvent(evt);
      if (event != null) {
         eventService.publish(event);
      } else {
         String topic = getTopicName(evt);
         Object topicValue = getTopicValue(evt);
         eventService.publish(topic, topicValue);
      }
   }

   /**@return the name of the action (Action.NAME)
    */
   public Object getName() {
      return getValue(Action.NAME);
   }

   /**
    * Override to publish and event service event instead of publishing on a topic.
    *
    * @param evt the event passed to #execute(ActionEvent)
    *
    * @return an EventServiceEvent to publish, if null, a topic name and value is used.
    */
   protected EventServiceEvent getEventServiceEvent(ActionEvent evt) {
      return null;
   }

   /**
    * The topic name is the first non-null value out of:
    * <ol>
    * <li>the action's getValue("event-service-topic")  {@link EVENT_SERVICE_TOPIC_NAME}
    * <li>the action's getValue("ID") (for compatibility with the SAM ActionManager's ID)
    * <li>the action's {@link Action.ACTION_COMMAND_KEY}
    * <li>the action event's {@link Action.ACTION_COMMAND_KEY}
    * <li>the aciton's {@link Action.NAME}
    * the value is used (if the value is not a String, the value's toString() is used). This can be configured via XML
    * by like so: <code> <name-value-pair name="event-service-topic" value="com.wellmanage.trading.nts.client.fx.MyTopicName"/>
    * </code>
    * <p/>
    * To use a different name, override this method.
    *
    * @param evt the event passed to #execute(ActionEvent)
    *
    * @return the topic name to publish on, getId() by default
    */
   protected String getTopicName(ActionEvent evt) {
      Object topicName = getValue(EVENT_SERVICE_TOPIC_NAME);
      if (topicName != null) {
         return topicName+"";
      } else {
         topicName = getValue("ID");
         if (topicName != null) {
            return topicName+"";
         } else {
            topicName = getValue(Action.ACTION_COMMAND_KEY);
            if (topicName != null) {
               return topicName+"";
            } else {
               topicName = evt.getActionCommand();
               if (topicName != null) {
                  return topicName+"";
               } else {
                  return (String) getName();
               }
            }
         }
      }
   }

   /**
    * By default, the id of the action is used as the topic name to publish on.  To use a different name, override this
    * method.
    *
    * @param evt the event passed to #execute(ActionEvent)
    *
    * @return the topic value to publish, getId() by default
    */
   protected Object getTopicValue(ActionEvent evt) {
      return evt;
   }

   /**
    * By default, exceptions are throw if getSwingEventService(0 returns null.
    *
    * @return false to suppress this behavior
    */
   public boolean getThrowsExceptionOnNullEventService() {
      return throwsExceptionOnNullEventService;
   }

   /**
    * By default, exceptions are throw if getSwingEventService(0 returns null.
    *
    * @param throwsExceptionOnNullEventService true to suppress the exception when there is no event service
    */
   public void setThrowsExceptionOnNullEventService(boolean throwsExceptionOnNullEventService) {
      this.throwsExceptionOnNullEventService = throwsExceptionOnNullEventService;
   }
}
