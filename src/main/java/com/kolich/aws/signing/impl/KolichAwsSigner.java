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

package com.kolich.aws.signing.impl;

import com.kolich.aws.KolichAwsException;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.util.crypt.Base64Utils.encodeBase64ToString;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

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
	public final String sign(final AwsCredentials credentials,
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
	
	@Override
	public final AwsSigningAlgorithm getAlgorithm() {
		return algorithm_;
	}
	
	@Override
	public final String toString() {
    	return String.format("%s(%s)",
    		getClass().getSimpleName(),
    		algorithm_.toString());
    }
	
}
