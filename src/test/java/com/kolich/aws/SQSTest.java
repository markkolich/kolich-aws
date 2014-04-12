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

package com.kolich.aws;

import com.amazonaws.services.sqs.model.*;
import com.kolich.aws.services.sqs.SQSClient;
import com.kolich.aws.services.sqs.impl.KolichSQSClient;
import com.kolich.common.date.ISO8601DateFormat;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.Option;
import com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory;
import com.kolich.http.common.response.HttpFailure;
import org.apache.http.client.HttpClient;

import java.net.URI;
import java.util.Date;

public class SQSTest {
	
	private static final String AWS_ACCESS_KEY_PROPERTY = "aws.key";
    private static final String AWS_SECRET_PROPERTY = "aws.secret";
	
	public static void main(String[] args) throws Exception {
		
		final String key = System.getProperty(AWS_ACCESS_KEY_PROPERTY);
		final String secret = System.getProperty(AWS_SECRET_PROPERTY);
		if (key == null || secret == null) {
			throw new IllegalArgumentException("You are missing the "
				+ "-Daws.key and -Daws.secret required VM "
				+ "properties on your command line.");
		}

		final HttpClient client = KolichHttpClientFactory.getNewInstanceNoProxySelector();
		
		final SQSClient sqs = new KolichSQSClient(client, key, secret);
				
		URI queueURI = null;
		final Either<HttpFailure,CreateQueueResult> create = sqs.createQueue("----__________");
		if(create.success()) {
			System.out.println("Created queue successfully: " + create.right().getQueueUrl());
			queueURI = URI.create(create.right().getQueueUrl());
		} else {
			System.err.println("Failed to create queue: " + create.left().getStatusCode());
		}
		
		final Either<HttpFailure,ListQueuesResult> list = sqs.listQueues();
		if(list.success()) {
			for(final String queueUrl : list.right().getQueueUrls()) {
				System.out.println("Queue: " + queueUrl);
			}
		} else {
			System.err.println("Listing queues failed.");
		}
		
		for(int i = 0; i < 5; i++) {
			final Either<HttpFailure,SendMessageResult> send =
				sqs.sendMessage(queueURI, "test message: " +
					ISO8601DateFormat.format(new Date()));
			if(send.success()) {
				System.out.println("Sent message [" + i + "]: " +
					send.right().getMessageId());			
			} else {
				System.err.println("Failed to send message.");
			}
		}
		
		for(int fetched = 0, error = 0; fetched < 5 && error == 0;) {
			final Either<HttpFailure,ReceiveMessageResult> messages = sqs.receiveMessage(queueURI, 10, 5);
			if(messages.success()) {
				fetched += messages.right().getMessages().size();
				System.out.println("Loaded " + messages.right().getMessages().size() + " messages.");
				for(final Message m : messages.right().getMessages()) {
					System.out.println("Message [" + m.getMessageId() + "]: " + m.getBody());
					final Option<HttpFailure> deleteMsg =
						sqs.deleteMessage(queueURI, m.getReceiptHandle());
					if(deleteMsg.isNone()) {
						System.out.println("Deleted message [" + m.getMessageId() + "]");
					} else {
						System.err.println("Failed to delete message: " + m.getReceiptHandle());
					}
				}
			} else {
				error = 1;
				System.err.println("Loading messages failed.");
			}
		}
		
		System.out.println("No messages should be on queue... long poll waiting!");
		final Either<HttpFailure,ReceiveMessageResult> lp = sqs.receiveMessage(queueURI, 20, 5);
		if(lp.success()) {
			System.out.println("Long poll finished waiting successfully.");
		} else {
			System.err.println("Failed to long poll wait.");
		}
		
		final Option<HttpFailure> delete = sqs.deleteQueue(queueURI);
		if(delete.isNone()) {
			System.out.println("Deleted queue successfully: " + queueURI);
		} else {
			System.err.println("Deletion of queue failed: " + queueURI);
		}

	}

}
