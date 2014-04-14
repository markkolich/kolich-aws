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

package com.kolich.aws.services.s3;

import java.net.URI;

import static java.net.URI.create;

public enum S3Region {
	
	US_EAST(null, "s3.amazonaws.com"),

    US_WEST_NORCAL("us-west-1", "s3-us-west-1.amazonaws.com"),
	US_WEST_OREGON("us-west-2", "s3-us-west-2.amazonaws.com"),
	
	EU("eu-west-1", "s3-eu-west-1.amazonaws.com"),
	
	ASIA_SINGAPORE("ap-southeast-1", "s3-ap-southeast-1.amazonaws.com"),
	ASIA_SYDNEY("ap-southeast-2", "s3-ap-southeast-2.amazonaws.com"),

	ASIA_TOKYO("ap-northeast-1", "s3-ap-northeast-1.amazonaws.com"),
	
	SOUTH_AMERICA("sa-east-1", "s3-sa-east-1.amazonaws.com");

    private final String regionId_;
	private final URI regionUri_;
	private S3Region(final String regionId,
                     final String regionUri) {
        regionId_ = regionId;
		regionUri_ = create(regionUri);
	}

    public final String getRegionId() {
        return regionId_;
    }
	
	public final String getApiEndpoint() {
		return regionUri_.toString();
	}

}
