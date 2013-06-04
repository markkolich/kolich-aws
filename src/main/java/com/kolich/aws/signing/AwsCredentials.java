package com.kolich.aws.signing;

public final class AwsCredentials {
	
	private final String key_;
	private final String secret_;
	
	public AwsCredentials(final String key, final String secret) {
		key_ = key;
		secret_ = secret;
	}
	
	public String getKey() {
		return key_;
	}
	
	public String getSecret() {
		return secret_;
	}
	
	@Override
	public String toString() {
		return String.format("AwsCredentials(%s, %s)", key_, secret_);
	}

}