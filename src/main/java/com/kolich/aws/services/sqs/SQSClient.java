package com.kolich.aws.services.sqs;

import java.net.URI;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;

public interface SQSClient {

	/**
	 * List all queues.
	 * @return a {@link ListQueuesResult} representing all queues
	 */
	public ListQueuesResult listQueues();
	
	/**
	 * Create a new queue.
	 * @param queueName the name of the queue
	 * @param defaultVisibilityTimeout the default message visibility timeout
	 * @return a {@link CreateQueueResult} representing the new queue
	 */
	public CreateQueueResult createQueue(final String queueName,
	    final int defaultVisibilityTimeout);
	
	/**
	 * Delete a queue.
	 * @param queueURI the {@link URI} of the queue
	 */
	public void deleteQueue(final URI queueURI);
	
	/**
	 * Send a message to a queue.
	 * @param queueURI the {@link URI} of the queue
	 * @param message the message to push onto the queue
	 * @return
	 */
	public SendMessageResult sendMessage(final URI queueURI,
		final String message);
	
	/**
	 * Receive a message.
	 * @param queueURI the {@link URI} of the queue
	 * @param maxNumberOfMessages the maximum number of messages to fetch
	 * from the queue
	 * @return
	 */
	public ReceiveMessageResult receiveMessage(final URI queueURI,
		final int maxNumberOfMessages);
	
	/**
	 * Delete a message.
	 * @param queueURI the {@link URI} of the queue
	 * @param receiptHandle the receipt handle that represents the message
	 * to delete
	 */
	public void deleteMessage(final URI queueURI,
		final String receiptHandle);
	
	/**
	 * Change a message's visiblity in a queue.
	 * @param queueURI the {@link URI} of the queue
	 * @param receiptHandle the receipt handle that represents the message
	 * to change
	 * @param visibilityTimeout the new visibility timeout value
	 */
	public void changeMessageVisibility(final URI queueURI,
		final String receiptHandle, final int visibilityTimeout);
		
}
