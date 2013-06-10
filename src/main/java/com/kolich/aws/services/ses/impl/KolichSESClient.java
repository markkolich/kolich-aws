package com.kolich.aws.services.ses.impl;

import static com.amazonaws.ResponseMetadata.AWS_REQUEST_ID;
import static com.amazonaws.util.StringUtils.fromByteBuffer;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.kolich.aws.services.ses.SESRegion.US_EAST;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static java.util.regex.Pattern.compile;
import static javax.xml.stream.XMLInputFactory.newInstance;
import static org.apache.http.HttpStatus.SC_OK;

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import com.amazonaws.services.simpleemail.model.GetSendStatisticsResult;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import com.amazonaws.services.simpleemail.model.transform.GetSendQuotaResultStaxUnmarshaller;
import com.amazonaws.services.simpleemail.model.transform.GetSendStatisticsResultStaxUnmarshaller;
import com.amazonaws.services.simpleemail.model.transform.ListVerifiedEmailAddressesResultStaxUnmarshaller;
import com.amazonaws.services.simpleemail.model.transform.SendEmailResultStaxUnmarshaller;
import com.amazonaws.services.simpleemail.model.transform.SendRawEmailResultStaxUnmarshaller;
import com.amazonaws.transform.StaxUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.kolich.aws.services.AbstractAwsService;
import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.services.ses.SESClient;
import com.kolich.aws.services.ses.SESRegion;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.None;
import com.kolich.common.functional.option.Option;
import com.kolich.common.functional.option.Some;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public final class KolichSESClient extends AbstractAwsService implements SESClient {
	
	private static final String SES_ACTION_PARAM = "Action";
    private static final String SES_EMAILADDRESS_PARAM = "EmailAddress";
    
    private static final String SES_SOURCE_ADDRESS_PARAM = "Source";
    private static final String SES_DESTINATIONS_PARAM = "Destinations.member";
    private static final String SES_DESTINATION_TO_PARAM = "Destination.ToAddresses.member";
    private static final String SES_DESTINATION_CC_PARAM = "Destination.CcAddresses.member";
    private static final String SES_DESTINATION_BCC_PARAM = "Destination.BccAddresses.member";
    private static final String SES_REPLY_TO_PARAM = "ReplyToAddresses.member";
    private static final String SES_RETURN_PATH_PARAM = "ReturnPath";
    private static final String SES_SUBJECT_PARAM = "Message.Subject.Data";
    private static final String SES_SUBJECT_CHARSET_PARAM = "Message.Subject.Charset";
    private static final String SES_BODY_TEXT_PARAM = "Message.Body.Text.Data";
    private static final String SES_BODY_TEXT_CHARSET_PARAM = "Message.Body.Text.Charset";
    private static final String SES_BODY_HTML_PARAM = "Message.Body.Html.Data";
    private static final String SES_BODY_HTML_CHARSET_PARAM = "Message.Body.Html.Charset";
    
    private static final String SES_RAW_MESSAGE_DATA_PARAM = "RawMessage.Data";
    
    private static final String SES_ACTION_SENDEMAIL = "SendEmail";
    private static final String SES_ACTION_VERIFY_EMAILADDRESS = "VerifyEmailAddress";
    private static final String SES_ACTION_DELETE_VERIFIED_EMAILADDRESS = "DeleteVerifiedEmailAddress";
    private static final String SES_ACTION_GETSENDQUOTA = "GetSendQuota";
    private static final String SES_ACTION_GETSENDSTATISTICS = "GetSendStatistics";
    private static final String SES_ACTION_LISTVERIFIED_ADDRESSES = "ListVerifiedEmailAddresses";
    
    private static final Pattern VALID_EMAIL_ADDRESS_PATTERN = compile(
    	"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@" +
    	"[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,4})$");
	
	private final HttpClient client_;
	
	public KolichSESClient(final HttpClient client,
		final AbstractAwsSigner signer, final SESRegion region) {
		super(signer, region.getApiEndpoint());
		client_ = client;
	}
	
	public KolichSESClient(final HttpClient client, final String key,
		final String secret, final SESRegion region) {
		this(client, new KolichSESSigner(key, secret), region);
	}
	
	public KolichSESClient(final HttpClient client, final String key,
		final String secret) {
		this(client, new KolichSESSigner(key, secret), US_EAST);
	}
	
	private abstract class AwsSESHttpClosure<S> extends AwsBaseHttpClosure<S> {
		private final Unmarshaller<S,StaxUnmarshallerContext> unmarshaller_;
		public AwsSESHttpClosure(final HttpClient client, final int expectStatus,
			final Unmarshaller<S,StaxUnmarshallerContext> unmarshaller) {
			super(client, expectStatus);
			unmarshaller_ = unmarshaller;
		}
		public AwsSESHttpClosure(final HttpClient client, final int expectStatus) {
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
		public final Option<HttpFailure> postOption() {
			final Either<HttpFailure,S> either = post();
			return either.success() ?
				None.<HttpFailure>none() :
				Some.<HttpFailure>some(either.left());
		}
	}

	@Override
	public Option<HttpFailure> verifyEmailAddress(final String emailAddress) {
		return new AwsSESHttpClosure<Void>(client_, SC_OK) {
			@Override
			public void validate() throws Exception {
				checkNotNull(emailAddress, "Email address cannot be null.");
				checkState(isValidEmail(emailAddress), "Invalid email address, " +
					"did not match expected email pattern.");	
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM, SES_ACTION_VERIFY_EMAILADDRESS);
				request.addParameter(SES_EMAILADDRESS_PARAM, emailAddress);
			}
		}.postOption();
	}

	@Override
	public Option<HttpFailure> deleteVerifiedEmailAddress(
		final String emailAddress) {
		return new AwsSESHttpClosure<Void>(client_, SC_OK) {
			@Override
			public void validate() throws Exception {
				checkNotNull(emailAddress, "Email address cannot be null.");
				checkState(isValidEmail(emailAddress), "Invalid email address, " +
					"did not match expected email pattern.");				
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM,
					SES_ACTION_DELETE_VERIFIED_EMAILADDRESS);
				request.addParameter(SES_EMAILADDRESS_PARAM, emailAddress);
			}
		}.postOption();
	}

	@Override
	public Either<HttpFailure,ListVerifiedEmailAddressesResult>
		listVerifiedEmailAddresses() {
		return new AwsSESHttpClosure<ListVerifiedEmailAddressesResult>(client_,
			SC_OK, new ListVerifiedEmailAddressesResultStaxUnmarshaller()) {
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM,
					SES_ACTION_LISTVERIFIED_ADDRESSES);
			}
		}.post();
	}

	@Override
	public Either<HttpFailure,GetSendQuotaResult> getSendQuota() {
		return new AwsSESHttpClosure<GetSendQuotaResult>(client_,
			SC_OK, new GetSendQuotaResultStaxUnmarshaller()) {
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM, SES_ACTION_GETSENDQUOTA);
			}
		}.post();
	}

	@Override
	public Either<HttpFailure,GetSendStatisticsResult> getSendStatistics() {
		return new AwsSESHttpClosure<GetSendStatisticsResult>(client_,
			SC_OK, new GetSendStatisticsResultStaxUnmarshaller()) {
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM,
					SES_ACTION_GETSENDSTATISTICS);
			}
		}.post();
	}

	@Override
	public Either<HttpFailure,SendEmailResult> sendEmail(final String from,
		final String to, final String returnPath, final String subject,
		final String body) {
		return sendEmail(
			// A single TO: destination address
			new Destination().withToAddresses(to),
			// A UTF-8 encoded message and subject.
			new Message()
				.withBody(new Body().withText(
					new Content().withData(body).withCharset(UTF_8)))
				.withSubject(
					new Content().withData(subject).withCharset(UTF_8)),
			// This method does not supply a reply-to address
			null,
			// Where to send a bounce back, if any.
			returnPath,
			// The sender's address, required
			from);
	}

	@Override
	public Either<HttpFailure,SendEmailResult> sendEmail(
		final Destination destination, final Message message,
		final List<String> replyToAddresses, final String returnPath,
		final String from) {
		return new AwsSESHttpClosure<SendEmailResult>(client_,
			SC_OK, new SendEmailResultStaxUnmarshaller()) {
			@Override
			public void validate() throws Exception {
				checkNotNull(destination, "Destination cannot be null.");
				checkNotNull(message, "Message cannot be null.");
				checkNotNull(from, "From email address cannot be null.");
				checkState(isValidEmail(from), "Invalid 'from' email address, " +
					"did not match expected email pattern.");	
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM, SES_ACTION_SENDEMAIL);
				request.addParameter(SES_SOURCE_ADDRESS_PARAM, from);
				// To
				for(int i = 0; i < destination.getToAddresses().size(); i++) {
					final String to = destination.getToAddresses().get(i);
					request.addParameterOpt(
						String.format("%s.%s", SES_DESTINATION_TO_PARAM, i), to);
				}
				// CC
				for(int i = 0; i < destination.getCcAddresses().size(); i++) {
					final String cc = destination.getCcAddresses().get(i);
					request.addParameterOpt(
						String.format("%s.%s", SES_DESTINATION_CC_PARAM, i), cc);
				}
				// BCC
				for(int i = 0; i < destination.getBccAddresses().size(); i++) {
					final String bcc = destination.getBccAddresses().get(i);
					request.addParameterOpt(
						String.format("%s.%s", SES_DESTINATION_BCC_PARAM, i), bcc);
				}
				// Subject
				final Content subject;
				if((subject = message.getSubject()) != null) {
					request.addParameterOpt(SES_SUBJECT_PARAM, subject.getData());
					request.addParameterOpt(SES_SUBJECT_CHARSET_PARAM,
						subject.getCharset());
				}
				// Body
				final Body body;
				if((body = message.getBody()) != null) {
					// Text body
					final Content text;
					if((text = body.getText()) != null) {
						request.addParameterOpt(SES_BODY_TEXT_PARAM, text.getData());
						request.addParameterOpt(SES_BODY_TEXT_CHARSET_PARAM,
							text.getCharset());
					}
					final Content html;
					if((html = body.getHtml()) != null) {
						request.addParameterOpt(SES_BODY_HTML_PARAM, html.getData());
						request.addParameterOpt(SES_BODY_HTML_CHARSET_PARAM,
							html.getCharset());
					}
				}
				// Reply-To
				if(replyToAddresses != null) {
					for(int i = 0; i < replyToAddresses.size(); i++) {
						final String replyTo = replyToAddresses.get(i);
						request.addParameterOpt(
							String.format("%s.%s", SES_REPLY_TO_PARAM, i), replyTo);
					}
				}
				// Return path
				request.addParameterOpt(SES_RETURN_PATH_PARAM, returnPath);
			}
		}.post();
	}

	@Override
	public Either<HttpFailure,SendRawEmailResult> sendRawEmail(
		final RawMessage message, final String from,
		final List<String> destinations) {
		return new AwsSESHttpClosure<SendRawEmailResult>(client_,
			SC_OK, new SendRawEmailResultStaxUnmarshaller()) {
			@Override
			public void validate() throws Exception {
				checkNotNull(message, "Raw message cannot be null.");
				checkNotNull(from, "From email address cannot be null.");
				checkState(isValidEmail(from), "Invalid 'from' email address, " +
					"did not match expected email pattern.");
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				request.addParameter(SES_ACTION_PARAM, SES_ACTION_SENDEMAIL);
				request.addParameter(SES_SOURCE_ADDRESS_PARAM, from);
				// Destination addresses
				for(int i = 0; i < destinations.size(); i++) {
					final String destination = destinations.get(i);
					request.addParameterOpt(
						String.format("%s.%s", SES_DESTINATIONS_PARAM, i),
							destination);
				}
				// Message body
				request.addParameter(SES_RAW_MESSAGE_DATA_PARAM,
					fromByteBuffer(message.getData()));
			}
		}.post();
	}
	
	private static final boolean isValidEmail(final String emailAddress) {
    	return VALID_EMAIL_ADDRESS_PATTERN.matcher(emailAddress).matches();
    }

}