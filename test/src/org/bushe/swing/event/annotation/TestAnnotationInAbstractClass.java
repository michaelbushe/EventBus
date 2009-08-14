package org.bushe.swing.event.annotation;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EDTUtil;

/**
 * Testing: 
 * https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=30702&forumID=1834
 */
public class TestAnnotationInAbstractClass extends TestCase {
    public void testAbsract() {
        ConcreteSubscriber concrete = new ConcreteSubscriber();
        AnnotationProcessor.process(concrete);
        EventBus.publish(new MyData());
        EDTUtil.waitForEDT();
        Assert.assertTrue(concrete.isInitialized());
    }
}
