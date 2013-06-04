package com.kolich.aws.services;

import org.apache.http.client.methods.HttpRequestBase;

import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;
import com.kolich.aws.transport.AwsHttpRequest;

public abstract class AbstractAwsSigner {
	
	/**
	 * Our AWS credentials, usually the access key and access key secret.
	 */
	protected final AwsCredentials credentials_;
	
	/**
	 * The signer that does the real work to generate a proper outgoing
	 * HTTP Authorization request header.
	 */
	protected final AwsSigner signer_;
	
	public AbstractAwsSigner(final AwsCredentials credentials,
		final AwsSigner signer) {
		credentials_ = credentials;
		signer_ = signer;
	}
	
	/**
	 * Called when an AWS client needs to sign a request before it's sent.
	 * The {@link HttpRequestBase} is modified directly in place,
	 * which usually involves checking the service endpoint and adding an
	 * Authorization HTTP request header.
	 * @param request
	 */
	public abstract void signHttpRequest(final AwsHttpRequest request);
	
}