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

package com.kolich.aws.transport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

public final class AwsHttpRequest {
	
	private final HttpRequestBase request_;	
	private final List<SortableBasicNameValuePair> params_;	
	private final String resource_;
	
	public AwsHttpRequest(final HttpRequestBase request,
		final String resource) {
		request_ = request;
		resource_ = resource;
		params_ = new ArrayList<SortableBasicNameValuePair>();
	}
	
	public AwsHttpRequest(final HttpRequestBase request) {
		this(request, null);
	}

	public HttpRequestBase getRequestBase() {
		return request_;
	}
	
	public String getMethod() {
		return request_.getMethod();
	}
	
	public URI getURI() {
		return (request_ != null) ? request_.getURI() : null;
	}
	
	public void setURI(final URI uri) {
		checkNotNull(uri, "The URI cannot be null!");
		request_.setURI(uri);
	}

	public String getResource() {
		return resource_;
	}
	
	public void addHeader(final String name, final String value) {
		checkNotNull(name, "The header name cannot be null!");
		checkNotNull(value, "The header value cannot be null!");
		request_.addHeader(name, value);
	}
	
	public Header getFirstHeader(final String name) {
		checkNotNull(name, "The header name cannot be null!");
		return request_.getFirstHeader(name);
	}
	
	public void addParameter(final SortableBasicNameValuePair pair) {
		checkNotNull(pair, "The name-value pair cannot be null!");
		params_.add(pair);
	}
	
	public void addParameter(final String name, final String value) {
		checkNotNull(name, "The pair name cannot be null!");
		checkNotNull(value, "The pair value cannot be null!");
		addParameter(new SortableBasicNameValuePair(name, value));
	}
	
	/**
	 * Same as {@link AwsHttpRequest#addParameter(String, String)}
	 * except that this method will only add the name/value parameter pair
	 * if and only if the name and value are not null.
	 * @param name
	 * @param value
	 */
	public void addParameterOpt(final String name, final String value) {
		if(name != null && value != null) {
			addParameter(name, value);
		}
	}
	
	/**
	 * Returns an unmodifiable view of the current parameters list.
	 * @return
	 */
	public List<SortableBasicNameValuePair> getParameters() {
		return unmodifiableList(params_);
	}

}
