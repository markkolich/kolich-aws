package com.kolich.aws.signing.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.util.crypt.Base64Utils.encodeBase64ToString;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.kolich.aws.KolichAwsException;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;

public final class KolichAwsSigner implements AwsSigner {
	
	public static enum AwsSigningAlgorithm {		
		HmacSHA1, HmacSHA256;
	};
	
	private final AwsSigningAlgorithm algorithm_;
	
	public KolichAwsSigner(final AwsSigningAlgorithm algorithm) {
		checkNotNull(algorithm, "Signing algorithm cannot be null.");
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
			mac.init(new SecretKeySpec(credentials.getSecretBytes(), algoName));
			// Sign the input.
			return encodeBase64ToString(mac.doFinal(getBytesUtf8(input)));
		} catch (Exception e) {
			throw new KolichAwsException("Failed to sign input " +
				"string (algorithm=" + algorithm_ + ", input=" + input + ")",
					e);
		}
	}
	
}
