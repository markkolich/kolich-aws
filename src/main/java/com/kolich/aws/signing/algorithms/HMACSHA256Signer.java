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

package com.kolich.aws.signing.algorithms;

import static com.kolich.aws.signing.algorithms.AwsSigningAlgorithm.HmacSHA256;
import static com.kolich.common.util.crypt.Base64Utils.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.kolich.aws.KolichAwsException;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;

/**
 * Computes an RFC-2104 compliant HMAC-SHA256 signature.
 */
public final class HMACSHA256Signer implements AwsSigner {
		
	/**
     * Computes an RFC-2104 compliant HMAC-SHA256 signature.
     */
	@Override
	public String sign(final AwsCredentials credentials, final String input) {
		try {
			final String algoName = getAlgorithmName();
			// Get a new instance of the HMAC-SHA1 algorithm.
			final Mac mac = Mac.getInstance(algoName);
			// Init it with our secret and the secret-key algorithm.
			mac.init(new SecretKeySpec(getBytesUtf8(credentials.getSecret()),
				algoName));
			// Sign the input.
			return newStringUtf8(encodeBase64(mac.doFinal(getBytesUtf8(input))));
		} catch (Exception e) {
			throw new KolichAwsException("Failed to sign input " +
				"string: " + input, e);
		}
	}
	
	@Override
	public String getAlgorithmName() {
		return HmacSHA256.toString();
	}
	
}