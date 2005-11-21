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
import java.util.logging.Level;
import javax.swing.SwingUtilities;


/**
 * The EventService for Swing.
 * <p/>
 * This class is Swing thread-safe.  All publish() calls NOT on the Swing EventDispatchThread thread are
 * queued onto the EDT.  If the calling thread is the EDT, then this is a simple pass-through
 * (i.e the subscribers are notified on the same stack frame, just like they would be had they added
 * themselves via Swing addXXListener methods).
 *
 * @author Michael Bushe michael@bushe.com
 */
public class SwingEventService extends ThreadSafeEventService {

   public SwingEventService() {
      super(new Long(200), true);
   }


   /**
    * Same as ThreadSafeEventService.publish(), except if the call is coming from a thread that is not the
    * Swing Event Dispatch Thread, the request is put on the EDT through a a call to SwingUtilities.invokeLater().
    * Otherwise this DOES NOT post a new event on the EDT.  The subscribers are called on the same EDT event,
    * just like addXXXListeners would be.
    */
   protected final void publish(final EventServiceEvent event, final String topic,
           final Object evtObj, final Map handlerMap,
           final Map vetoableListenerMap,
           final StackTraceElement[] callingStack) {
      if (SwingUtilities.isEventDispatchThread()) {
         super.publish(event, topic, evtObj, handlerMap, vetoableListenerMap, callingStack);
      } else {
         //Make call to this method - stick on the EDT if not on the EDT
         //Check the params first so that this thread can get the exception thrown
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               if (LOG.isLoggable(Level.FINE)) {
                  LOG.fine("publish(" + event + "," + topic + "," + evtObj
                          + "), called from non-EDT Thread:" + callingStack);
               }
               SwingEventService.super.publish(event, topic, evtObj, handlerMap,
                       vetoableListenerMap, callingStack);
            }
         });
      }
   }
}
