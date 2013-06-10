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

package com.kolich.aws.services.sqs.impl;

import static com.kolich.aws.signing.impl.KolichAwsSigner.AwsSigningAlgorithm.HmacSHA256;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.DATE;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import com.kolich.aws.KolichAwsException;
import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;
import com.kolich.aws.signing.impl.KolichAwsSigner;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.aws.transport.SortableBasicNameValuePair;
import com.kolich.common.date.ISO8601DateFormat;
import com.kolich.common.date.RFC822DateFormat;

public final class KolichSQSSigner extends AbstractAwsSigner {
	
	private static final String SQS_DEFAULT_VERSION = "2012-11-05";
	private static final String SQS_DEFAULT_SIGNATURE_VERSION = "2";
	
	private static final String SQS_PARAM_AWS_ACCESSKEY_ID = "AWSAccessKeyId";
	private static final String SQS_PARAM_VERSION = "Version";
	private static final String SQS_PARAM_SIGNATURE_VERSION = "SignatureVersion";
	private static final String SQS_PARAM_TIMESTAMP = "Timestamp";
	private static final String SQS_PARAM_SIGNATURE = "Signature";
	private static final String SQS_PARAM_SIGNATURE_METHOD = "SignatureMethod";
	
	private static final String APPLICATION_FORM_URLENCODED_TYPE =
		APPLICATION_FORM_URLENCODED.toString();
	
	public KolichSQSSigner(final AwsCredentials credentials,
		final AwsSigner signer) {
		super(credentials, signer);
	}
	
	public KolichSQSSigner(final AwsCredentials credentials) {
		this(credentials, new KolichAwsSigner(HmacSHA256));
	}
	
	public KolichSQSSigner(final String key, final String secret) {
		this(new AwsCredentials(key, secret));
	}

	@Override
	public void signHttpRequest(final AwsHttpRequest request)
		throws Exception {
		final Date now = new Date();
		request.addHeader(DATE, RFC822DateFormat.format(now));
    	// Only add a Content-Type header to the request if one is not
    	// already there.  SQS expects this to be here.
		if(request.getFirstHeader(CONTENT_TYPE) == null) {
    		request.addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_TYPE);
    	}
    	final List<SortableBasicNameValuePair> params =
    		new ArrayList<SortableBasicNameValuePair>();
    	params.add(new SortableBasicNameValuePair(SQS_PARAM_AWS_ACCESSKEY_ID,
    		credentials_.getKey()));
    	params.add(new SortableBasicNameValuePair(SQS_PARAM_VERSION,
    		SQS_DEFAULT_VERSION));
    	params.add(new SortableBasicNameValuePair(SQS_PARAM_SIGNATURE_VERSION,
    		SQS_DEFAULT_SIGNATURE_VERSION));
    	params.add(new SortableBasicNameValuePair(SQS_PARAM_TIMESTAMP,
    		ISO8601DateFormat.format(now)));
    	// Tell SQS what method we used to sign the request.  This will be
    	// Hmac256 for SQS.
    	params.add(new SortableBasicNameValuePair(SQS_PARAM_SIGNATURE_METHOD,
    		HmacSHA256.toString()));
    	params.addAll(request.getParameters());
    	// Sort the request parameters for signing.
    	Collections.sort(params);
    	// Get the string to sign.
		final String toSign = getSQSCanonicalString(params, request);
		// Compute the signature and attach it to the request.
		final String signature = signer_.sign(credentials_, toSign);
		params.add(new SortableBasicNameValuePair(SQS_PARAM_SIGNATURE,
			signature));		
		((HttpPost)request.getRequestBase())
			.setEntity(new UrlEncodedFormEntity(params, UTF_8));
	}
	
	private static final String getSQSCanonicalString(
		final List<SortableBasicNameValuePair> params,
		final AwsHttpRequest request) {
		final StringBuilder sb = new StringBuilder();
		sb.append(request.getMethod()).append(LINE_SEPARATOR_UNIX);
		sb.append(request.getURI().getHost().toLowerCase())
			.append(LINE_SEPARATOR_UNIX);
		sb.append(getSQSCanonicalPath(request)).append(LINE_SEPARATOR_UNIX);
		sb.append(getSQSCanonicalQuery(params));
        return sb.toString();
	}
	
	private static final String getSQSCanonicalPath(
		final AwsHttpRequest request) {
		String path = request.getURI().getPath();
		if(path == null || path.isEmpty()) {
			path = "/";
		}
		return path;
	}
	
	private static final String getSQSCanonicalQuery(
		final List<SortableBasicNameValuePair> params) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			new UrlEncodedFormEntity(params, UTF_8).writeTo(os);
			return os.toString(UTF_8).replace("+", "%20")
				.replace("*", "%2A").replace("%7E", "~");
		} catch (Exception e) {
			throw new KolichAwsException("Failed to get SQS cannonical " +
				"query string.", e);
		}
	}
	
	@Override
	public String toString() {
    	return String.format("%s(%s, %s)",
    		getClass().getSimpleName(),
    		credentials_.toString(), signer_.toString());
    }

}
