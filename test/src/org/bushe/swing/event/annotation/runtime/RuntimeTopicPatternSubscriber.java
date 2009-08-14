package org.bushe.swing.event.annotation.runtime;

import java.util.List;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.RuntimeTopicPatternEventSubscriber;

class RuntimeTopicPatternSubscriber implements SubscriberForTesting {
	private final String topicPattern;
	private long timesCalled;

	public RuntimeTopicPatternSubscriber(String topicPattern) {
		this.topicPattern = topicPattern;

		AnnotationProcessor.process(this);
	}

	@RuntimeTopicPatternEventSubscriber
	public void handleEvent(String topic, List<String> event) {
		timesCalled++;
	}

	public String getTopicPatternName() {
		return topicPattern;
	}

	public long getTimesCalled() {
		return timesCalled;
	}
}
