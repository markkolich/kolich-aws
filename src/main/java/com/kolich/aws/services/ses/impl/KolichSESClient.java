package com.kolich.aws.services.ses.impl;

import static com.kolich.aws.services.ses.SESRegion.US_EAST;

import java.util.List;

import org.apache.http.client.HttpClient;

import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import com.amazonaws.services.simpleemail.model.GetSendStatisticsResult;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import com.kolich.aws.services.AbstractAwsService;
import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.services.ses.SESClient;
import com.kolich.aws.services.ses.SESRegion;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.Option;
import com.kolich.http.common.response.HttpFailure;

public final class KolichSESClient extends AbstractAwsService implements SESClient {
	
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

	@Override
	public Option<HttpFailure> verifyEmailAddress(final String emailAddress) {
		
		return null;
	}

	@Override
	public Option<HttpFailure> deleteVerifiedEmailAddress(
		final String emailAddress) {
		
		return null;
	}

	@Override
	public Either<HttpFailure,ListVerifiedEmailAddressesResult>
		listVerifiedEmailAddresses() {
		
		return null;
	}

	@Override
	public Either<HttpFailure,GetSendQuotaResult> getSendQuota() {
		
		return null;
	}

	@Override
	public Either<HttpFailure,GetSendStatisticsResult> getSendStatistics() {
		
		return null;
	}

	@Override
	public Either<HttpFailure,SendEmailResult> sendEmail(final String from,
		final String to, final String returnPath, final String subject,
		final String body) {
		
		return null;
	}

	@Override
	public Either<HttpFailure,SendEmailResult> sendEmail(
		final Destination destination, final Message message,
		final List<String> replyToAddresses, final String returnPath,
		final String senderAddress) {
		
		return null;
	}

	@Override
	public Either<HttpFailure, SendRawEmailResult> sendRawEmail(
		final RawMessage message, final String senderAddress,
		final List<String> destinations) {
		
		return null;
	}

}