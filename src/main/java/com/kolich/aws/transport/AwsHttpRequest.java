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
