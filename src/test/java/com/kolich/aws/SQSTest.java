/**
 * Copyright (c) 2013 Mark S. Kolich
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

import org.apache.http.client.HttpClient;

import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.kolich.aws.services.sqs.SQSClient;
import com.kolich.aws.services.sqs.impl.KolichSQSClient;
import com.kolich.common.functional.either.Either;
import com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory;
import com.kolich.http.common.response.HttpFailure;

public class SQSTest {
	
	private static final String AWS_ACCESS_KEY_PROPERTY = "aws.key";
    private static final String AWS_SECRET_PROPERTY = "aws.secret";
	
	public static void main(String[] args) {
		
		final String key = System.getProperty(AWS_ACCESS_KEY_PROPERTY);
		final String secret = System.getProperty(AWS_SECRET_PROPERTY);
		if (key == null || secret == null) {
			throw new IllegalArgumentException("You are missing the "
				+ "-Daws.key and -Daws.secret required VM "
				+ "properties on your command line.");
		}

		final HttpClient client = KolichHttpClientFactory.getNewInstanceNoProxySelector();
		
		final SQSClient sqs = new KolichSQSClient(client, key, secret);
		
		final Either<HttpFailure,ListQueuesResult> list = sqs.listQueues();
		if(list.success()) {
			for(final String queueUrl : list.right().getQueueUrls()) {
				System.out.println("Queue: " + queueUrl);
			}
		} else {
			System.err.println("Listing queues failed.");
		}
		
		

	}

}
