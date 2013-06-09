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

package com.kolich.aws.services.sqs;

import static java.net.URI.create;

import java.net.URI;

public enum SQSRegion {
	
	DEFAULT("queue.amazonaws.com"),
	
	US_EAST("sqs.us-east-1.amazonaws.com"),
	
	US_WEST_OREGON("sqs.us-west-2.amazonaws.com"),
	US_WEST_NORCAL("sqs.us-west-1.amazonaws.com"),
	
	EU("sqs.eu-west-1.amazonaws.com"),
	
	ASIA_SINGAPORE("sqs.ap-southeast-1.amazonaws.com"),
	ASIA_SYDNEY("sqs.ap-southeast-2.amazonaws.com"),
	ASIA_TOKYO("sqs.ap-northeast-1.amazonaws.com"),
	
	SOUTH_AMERICA("sqs.sa-east-1.amazonaws.com");
	
	private URI regionUri_;
	private SQSRegion(final String regionUri) {
		regionUri_ = create(regionUri);
	}
	
	public String getApiEndpoint() {
		return regionUri_.toString();
	}

}