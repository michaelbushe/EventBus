package org.bushe.swing.event.annotation.runtime;

public class Factory {

	public static SubscriberForTesting newRuntimeTopicSubcriber(String topic) {
		return new RuntimeTopicSubscriber(topic);
	}

	public static SubscriberForTesting newRuntimeTopicPatternSubscriber(String topicPattern) {
		return new RuntimeTopicPatternSubscriber(topicPattern);
	}
}
