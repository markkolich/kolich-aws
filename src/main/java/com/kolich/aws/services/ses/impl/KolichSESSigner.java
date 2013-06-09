package com.kolich.aws.services.ses.impl;

import static com.kolich.aws.signing.impl.KolichAwsSigner.AwsSigningAlgorithm.HmacSHA1;

import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;
import com.kolich.aws.signing.impl.KolichAwsSigner;
import com.kolich.aws.transport.AwsHttpRequest;

public final class KolichSESSigner extends AbstractAwsSigner {
	
	public KolichSESSigner(final AwsCredentials credentials,
		final AwsSigner signer) {
		super(credentials, signer);
	}
	
	public KolichSESSigner(final AwsCredentials credentials) {
		this(credentials, new KolichAwsSigner(HmacSHA1));
	}
	
	public KolichSESSigner(final String key, final String secret) {
		this(new AwsCredentials(key, secret));
	}

	@Override
	public void signHttpRequest(final AwsHttpRequest request) {
		
	}

}