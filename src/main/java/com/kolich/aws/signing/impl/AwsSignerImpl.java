package com.kolich.aws.signing.impl;

import static com.kolich.common.util.crypt.Base64Utils.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.kolich.aws.KolichAwsException;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;

public final class AwsSignerImpl implements AwsSigner {
	
	private final AwsSigningAlgorithm algorithm_;
	
	public AwsSignerImpl(final AwsSigningAlgorithm algorithm) {
		algorithm_ = algorithm;
	}

	@Override
	public String sign(final AwsCredentials credentials,
		final String input) {
		try {
			final String algoName = algorithm_.toString();
			// Get a new instance of the HMAC-SHA1 algorithm.
			final Mac mac = Mac.getInstance(algoName);
			// Init it with our secret and the secret-key algorithm.
			mac.init(new SecretKeySpec(getBytesUtf8(credentials.getSecret()),
				algoName));
			// Sign the input.
			return newStringUtf8(encodeBase64(mac.doFinal(getBytesUtf8(input))));
		} catch (Exception e) {
			throw new KolichAwsException("Failed to sign input " +
				"string (algorithm=" + algorithm_ + ", input=" + input + ")",
					e);
		}
	}
	
}
