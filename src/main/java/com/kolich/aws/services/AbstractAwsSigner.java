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

package com.kolich.aws.services;

import static com.google.common.base.Preconditions.checkNotNull;

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
		checkNotNull(credentials, "AWS credentials cannot be null.");
		checkNotNull(signer, "AWS signer cannot be null.");
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