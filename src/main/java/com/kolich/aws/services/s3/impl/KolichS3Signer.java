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

package com.kolich.aws.services.s3.impl;

import static com.kolich.aws.signing.impl.KolichAwsSigner.AwsSigningAlgorithm.HmacSHA1;
import static com.kolich.aws.transport.AwsHeaders.AMAZON_PREFIX;
import static com.kolich.aws.transport.AwsHeaders.S3_ALTERNATE_DATE;
import static com.kolich.aws.transport.SortableBasicNameValuePair.sortParams;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_MD5;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.DATE;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;
import com.kolich.aws.signing.impl.KolichAwsSigner;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.aws.transport.SortableBasicNameValuePair;
import com.kolich.common.date.RFC822DateFormat;

public final class KolichS3Signer extends AbstractAwsSigner {
	
	private static final String APPLICATION_FORM_URLENCODED_TYPE =
		APPLICATION_FORM_URLENCODED.toString();
	
	public KolichS3Signer(final AwsCredentials credentials,
		final AwsSigner signer) {
		super(credentials, signer);
	}
	
	public KolichS3Signer(final AwsCredentials credentials) {
		this(credentials, new KolichAwsSigner(HmacSHA1));
	}
	
	public KolichS3Signer(final String key, final String secret) {
		this(new AwsCredentials(key, secret));
	}

	/**
     * The set of request parameters which must be included in
     * the canonical string to sign.  Note that these must be
     * sorted alphabetically, something that the AWS documentation
     * does not make very clear upfront.
     */
    private static final List<String> INTERESTING_PARAMETERS =
    	Arrays.asList(new String[]{
    		"acl", "torrent", "logging", "location", "policy",
    		"requestPayment", "versioning", "versions", "versionId",
    		"notification"
    	});
    
    @Override
	public void signHttpRequest(final AwsHttpRequest request)
		throws Exception {
    	// Add a Date header to the request.
    	request.addHeader(DATE, RFC822DateFormat.format(new Date()));
    	// Only add a Content-Type header to the request if one is not
    	// already there. AWS expects something useful here.
    	if(request.getFirstHeader(CONTENT_TYPE) == null) {
    		request.addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_TYPE);
    	}
    	final String toSign = getS3CanonicalString(request);
		final String signature = signer_.sign(credentials_, toSign);
		// Add the resulting Authorization header to the request.
		request.addHeader(AUTHORIZATION,
			// The format of the AWS required Authorization header.
			String.format("AWS %s:%s",
			// The Access Key ID uniquely identifies an AWS account. You
			// include it in AWS service requests to identify yourself as
			// the sender of the request.
			credentials_.getKey(),
			// The computed S3 signature for this request.
			signature));
	}
    	
	/**
     * Calculate the canonical string for a REST/HTTP request to S3.
     */
    private static final String getS3CanonicalString(
    	final AwsHttpRequest request) {
    	// A few standard headers we extract for conveinence.
    	final String contentType = CONTENT_TYPE.toLowerCase(),
    		contentMd5 = CONTENT_MD5.toLowerCase(),
    		date = DATE.toLowerCase();
    	// Start with the empty string ("").
    	final StringBuilder buf = new StringBuilder();
    	// Next is the HTTP verb and a newline.
        buf.append(request.getMethod() + LINE_SEPARATOR_UNIX);
        // Add all interesting headers to a list, then sort them.
        // "Interesting" is defined as Content-MD5, Content-Type, Date,
        // and x-amz-... headers.
        final Map<String,String> headersMap = getHeadersAsMap(request);
        final SortedMap<String,String> interesting = new TreeMap<String,String>();
        if(!headersMap.isEmpty()) {
            Iterator<Map.Entry<String,String>> it = headersMap.entrySet().iterator();
            while(it.hasNext()) {
            	Map.Entry<String,String> entry = it.next();
            	final String key = entry.getKey(), value = entry.getValue();
            	if(key == null) {
            		continue;
                }
            	final String lk = key.toString().toLowerCase(Locale.getDefault());
            	// Ignore any headers that are not interesting.
            	if(lk.equals(contentType) || lk.equals(contentMd5) ||
            		lk.equals(date) || lk.startsWith(AMAZON_PREFIX)) {
            		interesting.put(lk, value);
            	}
            }
        }
        // Remove default date timestamp if "x-amz-date" is set.
        if(interesting.containsKey(S3_ALTERNATE_DATE)) {
            interesting.put(date, "");
        }
        // These headers require that we still put a new line in after them,
        // even if they don't exist.
        if(!interesting.containsKey(contentType)) {
            interesting.put(contentType, "");
        }        
        if(!interesting.containsKey(contentMd5)) {
            interesting.put(contentMd5, "");
        }
		// Add all the interesting headers
		for(Iterator<Map.Entry<String, String>> i =
			interesting.entrySet().iterator(); i.hasNext();) {
			final Map.Entry<String, String> entry = i.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			if(key.startsWith(AMAZON_PREFIX)) {
				buf.append(key).append(':').append(value);
			} else {
				buf.append(value);
			}
			buf.append(LINE_SEPARATOR_UNIX);
		}
		// The CanonicalizedResource this request is working with.
		// If the request specifies a bucket using the HTTP Host header
		// (virtual hosted-style), append the bucket name preceded by a
		// "/" (e.g., "/bucketname"). For path-style requests and requests
		// that don't address a bucket, do nothing.
        if(request.getResource() != null) {
        	buf.append("/" + request.getResource() +
        		request.getURI().getRawPath());
        } else {
        	buf.append(request.getURI().getRawPath());
        }
        // Amazon requires us to sort the query string parameters.
        final List<SortableBasicNameValuePair> params =
        	sortParams(URLEncodedUtils.parse(request.getURI(), UTF_8));
		String separator = "?";
		for (final NameValuePair pair : params) {
			final String name = pair.getName(), value = pair.getValue();
			// Skip any parameters that aren't part of the
			// canonical signed string.
			if(!INTERESTING_PARAMETERS.contains(name)) {
				continue;
			}
			buf.append(separator).append(name);
			if(value != null) {
				buf.append("=").append(value);
			}
			separator = "&";
		}
		return buf.toString();
    }
    
    /**
     * Given an HttpRequestBase, extracts a Map containing each header to
     * value pair.  The returned Map is unmodifiable.
     */
    private static final Map<String, String> getHeadersAsMap(
    	final AwsHttpRequest request) {
    	final Map<String, String> map = new HashMap<String, String>();
    	for(final Header h : request.getRequestBase().getAllHeaders()) {
    		map.put(h.getName(), h.getValue());
    	}
    	return unmodifiableMap(map);
    }
    
    @Override
	public String toString() {
    	return String.format("%s(%s, %s)",
    		getClass().getSimpleName(),
    		credentials_.toString(), signer_.toString());
    }
    
}
