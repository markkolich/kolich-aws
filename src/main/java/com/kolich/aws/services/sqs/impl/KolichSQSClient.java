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

package com.kolich.aws.services.sqs.impl;

import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.transform.CreateQueueResultStaxUnmarshaller;
import com.amazonaws.services.sqs.model.transform.ListQueuesResultStaxUnmarshaller;
import com.amazonaws.services.sqs.model.transform.ReceiveMessageResultStaxUnmarshaller;
import com.amazonaws.services.sqs.model.transform.SendMessageResultStaxUnmarshaller;
import com.amazonaws.transform.StaxUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.kolich.aws.services.AbstractAwsService;
import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.services.sqs.SQSClient;
import com.kolich.aws.services.sqs.SQSRegion;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.None;
import com.kolich.common.functional.option.Option;
import com.kolich.common.functional.option.Some;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.net.URI;
import java.util.regex.Pattern;

import static com.amazonaws.ResponseMetadata.AWS_REQUEST_ID;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.kolich.aws.services.sqs.SQSRegion.DEFAULT;
import static java.util.regex.Pattern.compile;
import static javax.xml.stream.XMLInputFactory.newInstance;
import static org.apache.http.HttpStatus.SC_OK;

public final class KolichSQSClient extends AbstractAwsService
    implements SQSClient {
	    
    /**
     * SQS visibility timeouts can only be at most 43200 seconds (12-hours).
     */
    private static final int SQS_MAX_VISIBILITY_TIMEOUT = 43200; // seconds
    
    /**
     * The maximum number of messages to receive on any given request
     * cannot be more than 10.
     */
    private static final int SQS_MAX_MESSAGES_PER_REQUEST = 10;
    
    /**
     * The maximum amount of time SQS will allow any client to long
     * poll waiting for message delivery.
     */
    private static final int SQS_MAX_LONG_POLL_WAIT_TIME_SECS = 20; // seconds
	
	private static final String SQS_ACTION_PARAM = "Action";
	private static final String SQS_QUEUE_NAME_PARAM = "QueueName";
    private static final String SQS_DEFAULT_VISIBILITY_TIMEOUT_PARAM = "DefaultVisibilityTimeout";
    private static final String SQS_LONG_POLL_WAIT_TIME_PARAM = "WaitTimeSeconds";    
    private static final String SQS_VISIBILITY_TIMEOUT_PARAM = "VisibilityTimeout";
    private static final String SQS_MESSAGE_BODY_PARAM = "MessageBody";
    private static final String SQS_RECEIPT_HANDLE_PARAM = "ReceiptHandle";
    private static final String SQS_MAX_MESSAGES_PARAM = "MaxNumberOfMessages";
    
    private static final String SQS_ACTION_LIST_QUEUES = "ListQueues";
    private static final String SQS_ACTION_CREATE_QUEUE = "CreateQueue";
    private static final String SQS_ACTION_DELETE_QUEUE = "DeleteQueue";
    private static final String SQS_ACTION_SEND_MESSAGE = "SendMessage";
    private static final String SQS_ACTION_RECEIVE_MESSAGE = "ReceiveMessage";
    private static final String SQS_ACTION_DELETE_MESSAGE = "DeleteMessage";
    private static final String SQS_ACTION_CHANGE_VISIBILITY = "ChangeMessageVisibility";
    
    /**
     * Queue names can only contain alphanumeric characters, hyphens,
     * and underscores and must be between 1 and 80-characters in length.
     */
    private static final Pattern VALID_QUEUE_NAME_PATTERN =
    	compile("\\A[a-z0-9_\\-]{1,80}\\Z");
    
    private final HttpClient client_;
	
	public KolichSQSClient(final HttpClient client,
                           final AbstractAwsSigner signer,
                           final SQSRegion region) {
		super(signer, region.getApiEndpoint());
		client_ = client;
	}
	
	public KolichSQSClient(final HttpClient client,
                           final String key,
                           final String secret,
                           final SQSRegion region) {
		this(client, new KolichSQSSigner(key, secret), region);
	}
	
	public KolichSQSClient(final HttpClient client,
                           final String key,
                           final String secret) {
		this(client, key, secret, DEFAULT);
	}
	
	private abstract class AwsSQSHttpClosure<S> extends AwsBaseHttpClosure<S> {
		private final Unmarshaller<S,StaxUnmarshallerContext> unmarshaller_;
		public AwsSQSHttpClosure(final HttpClient client,
                                 final int expectStatus,
                                 final Unmarshaller<S,StaxUnmarshallerContext> unmarshaller) {
			super(client, expectStatus);
			unmarshaller_ = unmarshaller;
		}
		public AwsSQSHttpClosure(final HttpClient client,
                                 final int expectStatus) {
			this(client, expectStatus, null);
		}
		@Override
		public final void before(final HttpRequestBase request) throws Exception {
			final AwsHttpRequest wrapped = new AwsHttpRequest(request);
			validate();
			prepare(wrapped);
			signRequest(wrapped);
		}
		public void validate() throws Exception {
			// Default, nothing.
		}
		public void prepare(final AwsHttpRequest request) throws Exception {
			// Default, nothing.
		}
		@Override
		public S success(final HttpSuccess success) throws Exception {
			return (unmarshaller_ != null) ? unmarshall(success) : null;
		}
		private final S unmarshall(final HttpSuccess success) throws Exception {
			XMLEventReader reader = null;
			try {
				final XMLInputFactory xmlInputFactory = newInstance();
	    		reader = xmlInputFactory.createXMLEventReader(success.getContent());
	    		final StaxUnmarshallerContext stax =
	    			new StaxUnmarshallerContext(reader);
	    		stax.registerMetadataExpression("ResponseMetadata/RequestId",
	    			2, AWS_REQUEST_ID);
	    		stax.registerMetadataExpression("requestId", 2, AWS_REQUEST_ID);
	    		return unmarshaller_.unmarshall(stax);
			} finally {
				if(reader != null) {
					reader.close();
				}
			}
	    }
		public final Either<HttpFailure,S> post() {
			return post(SLASH_STRING);
		}
		public final Option<HttpFailure> postOption(final URI uri) {
			final Either<HttpFailure,S> either = post(uri);
			return either.success() ?
				None.<HttpFailure>none() :
				Some.<HttpFailure>some(either.left());
		}
	}

	@Override
	public Either<HttpFailure,ListQueuesResult> listQueues() {
		return new AwsSQSHttpClosure<ListQueuesResult>(client_, SC_OK,
			new ListQueuesResultStaxUnmarshaller()) {
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_LIST_QUEUES);
			}
		}.post();
	}

	@Override
	public Either<HttpFailure,CreateQueueResult> createQueue(final String queueName,
                                                             final Integer defaultVisibilityTimeout) {
		return new AwsSQSHttpClosure<CreateQueueResult>(client_, SC_OK,
			new CreateQueueResultStaxUnmarshaller()) {
			@Override
			public void validate() throws Exception {
				checkNotNull(queueName, "Queue name cannot be null.");
				checkState(isValidQueueName(queueName), "Invalid queue name, " +
					"did not match expected queue name pattern.");
				if(defaultVisibilityTimeout != null) {
					checkState(defaultVisibilityTimeout <= SQS_MAX_VISIBILITY_TIMEOUT,
						"Default visibility timeout cannot be greater than: " +
						SQS_MAX_VISIBILITY_TIMEOUT);
				}
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_CREATE_QUEUE);
				request.addParameter(SQS_QUEUE_NAME_PARAM, queueName);
				if(defaultVisibilityTimeout != null) {
					request.addParameter(SQS_DEFAULT_VISIBILITY_TIMEOUT_PARAM,
						Integer.toString(defaultVisibilityTimeout));
				}
			}
		}.post();
	}
	
	@Override
	public Either<HttpFailure,CreateQueueResult> createQueue(final String queueName) {
		return createQueue(queueName, null);
	}

	@Override
	public Option<HttpFailure> deleteQueue(final URI queueURI) {
		return new AwsSQSHttpClosure<Void>(client_, SC_OK) {
			@Override
			public void validate() throws Exception {
				checkNotNull(queueURI, "Queue URI cannot be null.");
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_DELETE_QUEUE);
			}
		}.postOption(queueURI);
	}

	@Override
	public Either<HttpFailure,SendMessageResult> sendMessage(final URI queueURI,
                                                             final String message) {
		return new AwsSQSHttpClosure<SendMessageResult>(client_, SC_OK,
			new SendMessageResultStaxUnmarshaller()) {
			@Override
			public void validate() throws Exception {
				checkNotNull(queueURI, "Queue URI cannot be null.");
				checkNotNull(message, "Message to send cannot be null.");
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_SEND_MESSAGE);
				request.addParameter(SQS_MESSAGE_BODY_PARAM, message);
			}
		}.post(queueURI);
	}

	@Override
	public Either<HttpFailure,ReceiveMessageResult> receiveMessage(final URI queueURI,
                                                                   final Integer longPollWaitSecs,
                                                                   final Integer maxNumberOfMessages) {
		return new AwsSQSHttpClosure<ReceiveMessageResult>(client_, SC_OK,
			new ReceiveMessageResultStaxUnmarshaller()) {
			@Override
			public void validate() throws Exception {
				checkNotNull(queueURI, "Queue URI cannot be null.");
				if(longPollWaitSecs != null) {
					checkState(longPollWaitSecs <= SQS_MAX_LONG_POLL_WAIT_TIME_SECS,
						"Cannot long poll wait on a queue longer than (secs): " +
						SQS_MAX_LONG_POLL_WAIT_TIME_SECS);
				}
				if(maxNumberOfMessages != null) {
					checkState(maxNumberOfMessages <= SQS_MAX_MESSAGES_PER_REQUEST,
						"Max number of messages to receive cannot be greater than: " +
						SQS_MAX_MESSAGES_PER_REQUEST);
				}
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_RECEIVE_MESSAGE);
				if(longPollWaitSecs != null) {
					request.addParameter(SQS_LONG_POLL_WAIT_TIME_PARAM,
						Integer.toString(longPollWaitSecs));
				}
				if(maxNumberOfMessages != null) {
					request.addParameter(SQS_MAX_MESSAGES_PARAM,
						Integer.toString(maxNumberOfMessages));
				}
			}
		}.post(queueURI);
	}
	
	@Override
	public Either<HttpFailure,ReceiveMessageResult> receiveMessage(final URI queueURI,
                                                                   final Integer longPollWaitSecs) {
		return receiveMessage(queueURI, longPollWaitSecs, null);
	}
	
	@Override
	public Either<HttpFailure,ReceiveMessageResult> receiveMessage(final URI queueURI) {
		return receiveMessage(queueURI, null);
	}

	@Override
	public Option<HttpFailure> deleteMessage(final URI queueURI,
                                             final String receiptHandle) {
		return new AwsSQSHttpClosure<Void>(client_, SC_OK) {
			@Override
			public void validate() throws Exception {
				checkNotNull(queueURI, "Queue URI cannot be null.");
				checkNotNull(receiptHandle, "Message receipt handle cannot " +
					"be null.");
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_DELETE_MESSAGE);
				request.addParameter(SQS_RECEIPT_HANDLE_PARAM, receiptHandle);
			}
		}.postOption(queueURI);
	}

	@Override
	public Option<HttpFailure> changeMessageVisibility(final URI queueURI,
                                                       final String receiptHandle,
                                                       final Integer visibilityTimeout) {
		return new AwsSQSHttpClosure<Void>(client_, SC_OK) {
			@Override
			public void validate() throws Exception {
				checkNotNull(queueURI, "Queue URI cannot be null.");
				checkNotNull(receiptHandle, "Message receipt handle cannot " +
					"be null.");
				checkNotNull(visibilityTimeout, "Message visibility timeout " +
					"cannot be null.");
				checkState(visibilityTimeout <= SQS_MAX_VISIBILITY_TIMEOUT,
					"Message visibility timeout cannot be greater than: " +
					SQS_MAX_VISIBILITY_TIMEOUT);
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SQS_ACTION_PARAM, SQS_ACTION_CHANGE_VISIBILITY);
				request.addParameter(SQS_RECEIPT_HANDLE_PARAM, receiptHandle);
				request.addParameter(SQS_VISIBILITY_TIMEOUT_PARAM,
					Integer.toString(visibilityTimeout));
			}
		}.postOption(queueURI);
	}
	
	private static final boolean isValidQueueName(final String queueName) {
    	return VALID_QUEUE_NAME_PATTERN.matcher(queueName).matches();
    }

}
