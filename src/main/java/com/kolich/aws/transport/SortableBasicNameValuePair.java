package com.kolich.aws.transport;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * AWS requires that we sort request parameters for signing. By default,
 * {@link BasicNameValuePair} does not implement {@link Comparable} so we
 * extend and do that here.
 */
public final class SortableBasicNameValuePair extends BasicNameValuePair
	implements Comparable<SortableBasicNameValuePair> {

	private static final long serialVersionUID = -3515848847409095067L;

	public SortableBasicNameValuePair(final String name, final String value) {
		super(name, value);
	}
	
	@Override
	public int compareTo(final SortableBasicNameValuePair s) {
		return getName().compareTo(s.getName());
	}

	public static final List<SortableBasicNameValuePair> sortParams(
    	final List<NameValuePair> params) {
    	final List<SortableBasicNameValuePair> sortable =
    		new ArrayList<SortableBasicNameValuePair>();
    	for(final NameValuePair pair : params) {
    		sortable.add(new SortableBasicNameValuePair(
    			pair.getName(), pair.getValue()));
    	}
    	sort(sortable); // Merge sort.
    	return unmodifiableList(sortable);
    }
	
}
