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

package com.kolich.aws.transport;

import com.google.common.collect.ImmutableList;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AwsHttpRequest {
	
	private final HttpRequestBase request_;	
	private final ImmutableList.Builder<SortableBasicNameValuePair> paramBuilder_;
    private final String resource_;
	
	public AwsHttpRequest(final HttpRequestBase request,
                          final String resource) {
        request_ = checkNotNull(request, "The request cannot be null.");
		resource_ = resource;
        paramBuilder_ = ImmutableList.builder();
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
		return request_.getURI();
	}
	
	public void setURI(final URI uri) {
		checkNotNull(uri, "The URI cannot be null.");
		request_.setURI(uri);
	}

	public String getResource() {
		return resource_;
	}
	
	public void addHeader(final String name,
                          final String value) {
		checkNotNull(name, "The header name cannot be null.");
		checkNotNull(value, "The header value cannot be null.");
		request_.addHeader(name, value);
	}
	
	public Header getFirstHeader(final String name) {
		return request_.getFirstHeader(name);
	}
	
	public void addParameter(final SortableBasicNameValuePair pair) {
		checkNotNull(pair, "The name-value pair cannot be null.");
        paramBuilder_.add(pair);
	}
	
	public void addParameter(final String name,
                             final String value) {
		checkNotNull(name, "The pair name cannot be null.");
		checkNotNull(value, "The pair value cannot be null.");
		addParameter(new SortableBasicNameValuePair(name, value));
	}
	
	public void addParameterOpt(final String name,
                                final String value) {
		if(name != null && value != null) {
			addParameter(name, value);
		}
	}
		
	/**
	 * Returns an unmodifiable view of the current parameters list.
	 * @return
	 */
	public List<SortableBasicNameValuePair> getParameters() {
        return paramBuilder_.build();
	}

}
