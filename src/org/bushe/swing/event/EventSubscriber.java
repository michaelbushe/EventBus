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
 * Interface for classes that subscribe to {@link EventServiceEvent}s from an {@link EventService}.
 *
 * @author Michael Bushe michael@bushe.com
 */
public interface EventSubscriber {

   /**
    * Handle a published event.
    * <p>The EventService calls this method on each publication of subscribed EventServiceEvents.
    * <p>Prequisite: EventSubscriber has subscribed for the EventServiceEvent type with the EventService.</p>
    * See {@link EventService}</p>
    *
    * @param evt The EventServiceEvent that is being published.
    */
   public void onEvent(EventServiceEvent evt);
}
