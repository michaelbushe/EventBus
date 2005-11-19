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

import junit.framework.TestCase;

/**
 * The DefaultEventService is NOT Swing-safe!  But it's easier to test...
 */
public class TestDefaultEventService extends TestCase {

    private ThreadSafeEventService eventService = null;
    private EventHandler eventHandler = null;
    private int eventsHandledCount;
    private int handleExceptionCount;

    public TestDefaultEventService(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        eventService = new ThreadSafeEventService(null, true);
    }

    protected void tearDown() throws Exception {
        eventService = null;
    }

    private EventServiceEvent createEvent() {
        return new EventServiceEvent() {
            public Object getSource() {
                return "";
            }
        };
    }
    private Class getEventClass() {
        return createEvent().getClass();
    }

    private EventHandler createEventHandler(boolean throwException) {
        return new TestHandler(throwException);
    }

    private EventHandler  getEventHandler() {
        if (eventHandler == null) {
            eventHandler = createEventHandler(true);
        }
        return eventHandler;
    }

    public void testSubscribe() {
        boolean actualReturn;
        EventHandler handler = createEventHandler(false);

        actualReturn = eventService.subscribe(getEventClass(), handler);
        assertTrue("testSubscribe(new handler)", actualReturn);

        actualReturn = eventService.subscribe(getEventClass(), handler);
        assertFalse("testSubscribe(duplicate handler)", actualReturn);

        eventsHandledCount = 0;
        handleExceptionCount = 0;
        eventService.publish(createEvent());

        //The test passes if 1 handlers completed and 0 handlers threw exception.
        assertEquals("testPublish(total)", 1, eventsHandledCount);
        assertEquals("testPublish(exceptions)", 0, handleExceptionCount);

        try {
            actualReturn = eventService.subscribe((Class)null, getEventHandler());
            fail("subscribe(null, x) should have thrown exception");
        } catch (Exception e) {
        }

        try {
            actualReturn = eventService.subscribe(getEventClass(), null);
            fail("subscribe(x, null) should have thrown exception");
        } catch (Exception e) {
        }

    }

    public void testVeto() {
        boolean actualReturn;
        EventHandler handler = createEventHandler(false);

        actualReturn = eventService.subscribe(getEventClass(), handler);

        actualReturn = eventService.subscribeVetoListener(getEventClass(), new VetoEventListener() {
            public boolean shouldVeto(EventServiceEvent evt) {
                return true;
            }
        });

        eventsHandledCount = 0;
        handleExceptionCount = 0;
        eventService.publish(createEvent());

        //The test passes if 1 handlers completed and 0 handlers threw exception.
        assertEquals("testVeto(total)", 0, eventsHandledCount);
        assertEquals("testVeto(exceptions)", 0, handleExceptionCount);
    }

    public void testUnsubscribe() {
        eventService.subscribe(getEventClass(), getEventHandler());

        boolean actualReturn;

        try {
            actualReturn = eventService.unsubscribe((Class)null, getEventHandler());
            fail("unsubscribe(null, x) should have thrown exception");
        } catch (Exception e) {
        }

        try {
            actualReturn = eventService.unsubscribe(getEventClass(), null);
            fail("unsubscribe(x, null) should have thrown exception");
        } catch (Exception e) {
        }

        actualReturn = eventService.unsubscribe(getEventClass(), getEventHandler());
        assertTrue("return value", actualReturn);
    }

    /**
     * Test that the publish method works and that execptions thrown in event
     * handlers don't halt publishing. In the test 2 handlers are good and
     * 2 handlers throw exceptions.
     */
    public void testPublish() {
        try {
            eventService.publish(null);
            fail("publish(null) should have thrown exception");
        } catch (Exception e) {
        }

        try {
            eventService.publish((String)null, createEvent());
            fail("publish(null, x) should have thrown exception");
        } catch (Exception e) {
        }

        eventService.subscribe(getEventClass(), createEventHandler(true));
        eventService.subscribe(getEventClass(), createEventHandler(false));
        eventService.subscribe(getEventClass(), createEventHandler(true));
        eventService.subscribe(getEventClass(), createEventHandler(false));

        eventsHandledCount = 0;
        handleExceptionCount = 0;
        eventService.publish(createEvent());

        //The test passes if 2 handlers completed and 2 handlers threw exception.
        assertEquals("testPublish(completed)", 4, eventsHandledCount);
        assertEquals("testPublish(exceptions)", 2, handleExceptionCount);
    }

    private class TestHandler implements EventHandler {

        private boolean throwException;

        public TestHandler(boolean throwException) {
            this.throwException = throwException;
        }

        public void handleEvent(EventServiceEvent evt) {
            eventsHandledCount++;
            if (throwException) {
                handleExceptionCount++;
                throw new IllegalArgumentException();
            }
        }
    }
}
