/**
 * Copyright (c) 2014 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.aws.services.sqs;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.Option;
import com.kolich.http.common.response.HttpFailure;

import java.net.URI;

public interface SQSClient {

	/**
	 * List all queues.
	 * @return a {@link ListQueuesResult} representing all queues
	 */
	public Either<HttpFailure,ListQueuesResult> listQueues();
	
	/**
	 * Create a new queue.
	 * @param queueName the name of the queue
	 * @param defaultVisibilityTimeout the default message visibility timeout
	 * @return a {@link CreateQueueResult} representing the new queue
	 */
	public Either<HttpFailure,CreateQueueResult> createQueue(final String queueName,
                                                             final Integer defaultVisibilityTimeout);
	
	public Either<HttpFailure,CreateQueueResult> createQueue(final String queueName);
	
	/**
	 * Delete a queue.
	 * @param queueURI the {@link URI} of the queue
	 */
	public Option<HttpFailure> deleteQueue(final URI queueURI);
	
	/**
	 * Send a message to a queue.
	 * @param queueURI the {@link URI} of the queue
	 * @param message the message to push onto the queue
	 * @return
	 */
	public Either<HttpFailure,SendMessageResult> sendMessage(final URI queueURI,
                                                             final String message);
	
	/**
	 * Receive a message.
	 * @param queueURI the {@link URI} of the queue
	 * @param maxNumberOfMessages the maximum number of messages to fetch
	 * from the queue
	 * @return
	 */
	public Either<HttpFailure,ReceiveMessageResult> receiveMessage(final URI queueURI,
                                                                   final Integer longPollWaitSecs,
                                                                   final Integer maxNumberOfMessages);
	
	public Either<HttpFailure,ReceiveMessageResult> receiveMessage(final URI queueURI,
                                                                   final Integer longPollWaitSecs);
	
	public Either<HttpFailure,ReceiveMessageResult> receiveMessage(final URI queueURI);
	
	/**
	 * Delete a message.
	 * @param queueURI the {@link URI} of the queue
	 * @param receiptHandle the receipt handle that represents the message
	 * to delete
	 */
	public Option<HttpFailure> deleteMessage(final URI queueURI,
                                             final String receiptHandle);
	
	/**
	 * Change a message's visiblity in a queue.
	 * @param queueURI the {@link URI} of the queue
	 * @param receiptHandle the receipt handle that represents the message
	 * to change
	 * @param visibilityTimeout the new visibility timeout value
	 */
	public Option<HttpFailure> changeMessageVisibility(final URI queueURI,
                                                       final String receiptHandle,
                                                       final Integer visibilityTimeout);
		
}
