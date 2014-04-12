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

package com.kolich.aws.signing;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.lang3.StringUtils.abbreviate;

public final class AwsCredentials {
	
	private final String key_;
	private final String secret_;
	
	public AwsCredentials(final String key,
                          final String secret) {
		key_ = checkNotNull(key, "AWS API key cannot be null.");
		secret_ = checkNotNull(secret, "AWS API secret cannot be null.");
	}
	
	public final String getKey() {
		return key_;
	}
	
	public final String getSecret() {
		return secret_;
	}
	
	public final byte[] getSecretBytes() {
		return getBytesUtf8(secret_);
	}
	
	@Override
	public final String toString() {
    	return String.format("%s(%s, %s)",
    		getClass().getSimpleName(),
    		key_, abbreviate(secret_, 10));
    }

}
