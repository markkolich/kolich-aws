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

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import com.kolich.aws.KolichAwsException;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.http.blocking.helpers.definitions.OrHttpFailureClosure;

public abstract class AbstractAwsService {
	
	protected static final String HTTPS = "https://";
	
	protected static final String SLASH_STRING = "/";
	protected static final String EMPTY_STRING = "";
	protected static final String QUERY_STRING = "?";
	protected static final String DOT_STRING = ".";
	
	private final AbstractAwsSigner signer_;
	private final URI apiEndpoint_;
	
	protected abstract class AwsBaseHttpClosure<S> extends OrHttpFailureClosure<S> {
		private final int expectStatus_;
		public AwsBaseHttpClosure(final HttpClient client, final int expectStatus) {
			super(client);
			expectStatus_ = expectStatus;
		}
		@Override
		public boolean check(final HttpResponse response,
			final HttpContext context) {
			return expectStatus_ == response.getStatusLine().getStatusCode();
		}
	}
	
	public AbstractAwsService(final AbstractAwsSigner signer,
		final String apiEndpoint) {
		checkNotNull(signer, "The signer cannot be null!");
		checkNotNull(apiEndpoint, "The service client API endpoint " +
			"cannot be null!");
		signer_ = signer;
		apiEndpoint_ = getApiEndpoint(apiEndpoint);
	}
	
	private final URI getApiEndpoint(String apiEndPoint) {
		checkNotNull(apiEndPoint, "Endpoint URL or authority cannot be null!");
		// If the communication endpoint does not start with https://
		// then we assume that we need to add it.
		if(!apiEndPoint.startsWith(HTTPS)) {
			apiEndPoint = HTTPS + apiEndPoint;
		} else {
			// Our communication endpoint can only be HTTPS.
			throw new KolichAwsException("Oops! AWS endpoints must start " +
				"with " + HTTPS + " but you gave me something else: " +
					apiEndPoint);
		}
		// Create our endpoint URI.
		return URI.create(apiEndPoint);
	}
	
	protected final void signRequest(final AwsHttpRequest request) {
		checkNotNull(request, "Request cannot be null!");
		// Compute the final endpoint for the request and set it.
		request.setURI(getFinalEndpoint(request));
		// Sign the request using an appropriate request signer.
		signer_.signHttpRequest(request);
	}
	
	private URI getFinalEndpoint(final AwsHttpRequest request) {
		URI endPointURI = request.getURI();
		// If the request URI already starts with https:// then we don't
		// have to build a full endpoint URL anymore since it's already
		// been provided.  This assumes the caller knows what they are
		// doing and have built a complete and proper URL for AWS.
		if(!isComplete(endPointURI)) {
			endPointURI = URI.create(
				// AWS endpoint URL's always start with https://	
				HTTPS +
				// The sub-domain is most often how we refer to S3 buckets.
				// For example, on a bucket named "foo" the subdomain would
				// be "foo" and the resulting endpoint we actually send
				// the request to is "foo.s3.amazonaws.com".
				((request.getResource() != null) ?
					// If there is a "resource" (usually a bucket name to
					// represent a sub-domain, then append it followed by
					// a single dot.
					request.getResource() + DOT_STRING :
					// If the request has no resource, usually for all AWS
					// services other than S3, then just append an empty
					// String.
					EMPTY_STRING) +
				// Returns the decoded authority component of this endpoint URI.
				// The authority of a URI is basically the hostname, otherwise
				// called the endpoint here.
				apiEndpoint_.getAuthority() +
				// Returns the decoded path component of the request URI.
				// The path of a URI is the piece of the URI after the hostname,
				// not including the query parameters.
				getPath(endPointURI) +
				// Returns the decoded query component of this URI.
				// The query parameters, if any.
				getQuery(endPointURI));
		}
		return endPointURI;
	}
	
	/**
	 * Given a {@link URI} returns the path component of that
	 * {@link URI}.  The path of a URI is the piece of the URI after
	 * the hostname, not including the query parameters.  If the URI
	 * is null, a single "/" is returned.  If the URI is not null, but
	 * the path is empty, an "" empty string is returned.
	 * @param uri the URI to extract the path from
	 * @return
	 */
	private static final String getPath(final URI uri) {
		if(uri == null) {
			return SLASH_STRING;
		} else {
			final String path = uri.getRawPath();
			return (path == null) ? EMPTY_STRING : path;
		}
	}
	
	/**
	 * Given a {@link URI} returns the query string component of that
	 * {@link URI}.  The query of a URI is the piece after the "?". If the
	 * URI is null, an "" empty string is returned.  If the URI is not null,
	 * but the query is empty, an "" empty string is returned.
	 * @param uri the URI to extract the query from
	 * @return
	 */
	private static final String getQuery(final URI uri) {
		if(uri == null) {
			return EMPTY_STRING;
		} else {
			final String query = uri.getRawQuery();
			return (query == null) ? EMPTY_STRING : QUERY_STRING + query;
		}
	}
	
	/**
	 * Checks if the given URI is non-null, and if it's a complete endpoint
	 * URI that already starts with "https://".
	 * @param uri
	 * @return
	 */
	private static final boolean isComplete(final URI uri) {
		return uri != null && uri.toString().startsWith(HTTPS);
	}
	
}
