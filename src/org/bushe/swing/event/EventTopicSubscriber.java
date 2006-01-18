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

/**
 * Interface for classes that listen to topics on the {@link EventService}.
 *
 * @author Michael Bushe michael@bushe.com
 */
public interface EventTopicSubscriber {

   /**
    * Handle an event published on a topic.
    * <p>
    * The EventService calls this method on each publication on a subscribed topic name.
    * <p>Prequisite: EventTopicSubscriber has subscribed for a topic name with the EventService.</p>
    * @param topic the name of the topic published on
    * @param data a data object associated with the event publication, anything you want
    */
   public void onEvent(String topic, Object data);
}
