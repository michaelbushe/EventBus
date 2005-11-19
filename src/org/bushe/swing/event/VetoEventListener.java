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
 * Interface for classes that can veto {@link EventServiceEvent}s from the {@link EventService}.
 *
 * @author Michael Bushe michael@bushe.com
 */
public interface VetoEventListener {

   /**
    * Determine whether an event should be vetoed or published.
    * <p/>
    * The EventService calls this method <b>before</b> publication of EventServiceEvents.  If any of the
    * VetoEventListeners return true, then none of the handlers for that event are called. <p>Prequisite:
    * VetoEventListener has to be subscribed with the EventService for the EventServiceEvent or topic.</p>
    * <p>Guaranteed to be called in the SwingEventThread when using the SwingEventService (EventBus). See {@link
    * EventService}</p>
    *
    * @param evt The EventServiceEvent to veto or allow to be published.
    *
    * @return true if the event should be vetoed and not published, false if the event should be published.
    */
   public boolean shouldVeto(EventServiceEvent evt);
}
