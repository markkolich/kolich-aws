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

import com.google.common.collect.Lists;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

/**
 * AWS requires that we sort request parameters for signing. By default,
 * {@link BasicNameValuePair} does not implement {@link Comparable} so we
 * extend and do that here.
 */
public final class SortableBasicNameValuePair extends BasicNameValuePair
	implements Comparable<SortableBasicNameValuePair> {

	private static final long serialVersionUID = -3515848847409095067L;

	public SortableBasicNameValuePair(final String name,
                                      final String value) {
		super(name, value);
	}
	
	@Override
	public int compareTo(final SortableBasicNameValuePair s) {
		return getName().compareTo(s.getName());
	}

	public static final List<SortableBasicNameValuePair> sortParams(final List<NameValuePair> params) {
    	final List<SortableBasicNameValuePair> sortable = Lists.newArrayList();
        for(final NameValuePair pair : params) {
    		sortable.add(new SortableBasicNameValuePair(
    			pair.getName(), pair.getValue()));
    	}
    	sort(sortable); // Merge sort.
    	return unmodifiableList(sortable);
    }
	
}
