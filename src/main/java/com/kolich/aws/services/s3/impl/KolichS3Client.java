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

package com.kolich.aws.services.s3.impl;

import com.amazonaws.services.s3.internal.XmlWriter;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.transform.Unmarshallers;
import com.google.common.collect.Lists;
import com.kolich.aws.services.AbstractAwsService;
import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.services.s3.S3Client;
import com.kolich.aws.services.s3.S3Region;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.None;
import com.kolich.common.functional.option.Option;
import com.kolich.common.functional.option.Some;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.amazonaws.services.s3.internal.Constants.XML_NAMESPACE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.kolich.aws.services.s3.S3Region.US_EAST;
import static com.kolich.aws.transport.AwsHeaders.*;
import static com.kolich.common.util.URLEncodingUtils.urlDecode;
import static com.kolich.common.util.URLEncodingUtils.urlEncode;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public final class KolichS3Client extends AbstractAwsService
    implements S3Client {
	    
    /**
     * Specifies the key to start with when listing objects in a bucket.
     * Amazon S3 lists objects in alphabetical order.
     */
    private static final String S3_PARAM_MARKER = "marker";
    
    /**
     * Limits the response to keys that begin with the specified prefix.
     * You can use prefixes to separate a bucket into different groupings
     * of keys. (You can think of using prefix to make groups in the same
     * way you'd use a folder in a file system.)
     */
    private static final String S3_PARAM_PREFIX = "prefix";
    
    /**
     * Bucket names can ONLY contain lowercase letters, numbers, periods (.),
     * underscores (_), and dashes (-). Bucket names MUST start with a number
     * or letter. Bucket names MUST be between 3 and 255 characters long.
     */
    private static final Pattern VALID_BUCKET_NAME_PATTERN =
    	compile("\\A[a-z0-9]{1}[a-z0-9_\\-\\.]{1,253}[a-z0-9]{1}\\Z");
	
	private final HttpClient client_;

    private final S3Region region_;
	
	public KolichS3Client(final HttpClient client,
                          final AbstractAwsSigner signer,
                          final S3Region region) {
		super(signer, region.getApiEndpoint());
		client_ = client;
        region_ = region;
	}
	
	public KolichS3Client(final HttpClient client,
                          final String key,
                          final String secret,
                          final S3Region region) {
		this(client, new KolichS3Signer(key, secret), region);
	}
	
	public KolichS3Client(final HttpClient client,
                          final String key,
                          final String secret) {
		this(client, new KolichS3Signer(key, secret), US_EAST);
	}
	
	private abstract class AwsS3HttpClosure<S> extends AwsBaseHttpClosure<S> {
		private final String bucketName_;
		public AwsS3HttpClosure(final HttpClient client,
                                final int expectStatus,
                                final String bucketName) {
			super(client, expectStatus);
			bucketName_ = bucketName;
		}
		public AwsS3HttpClosure(final HttpClient client,
                                final int expectStatus) {
			this(client, expectStatus, null);
		}
		@Override
		public final void before(final HttpRequestBase request) throws Exception {
			final AwsHttpRequest wrapped = new AwsHttpRequest(request, bucketName_);
			validate();
			prepare(wrapped);
			signRequest(wrapped);
		}
		public void validate() throws Exception {
			// Default, nothing.
		}
		public void prepare(final AwsHttpRequest request) throws Exception {
			// Default, nothing.
		}
		@Override
		public S success(final HttpSuccess success) throws Exception {
			return null; // Default, return null on success.
		}		
		public final Either<HttpFailure,S> head(final String... path) {
			return super.head(buildPath(path));
		}
		public final Either<HttpFailure,S> get(final String... path) {
			return super.get(buildPath(path));
		}
		public final Either<HttpFailure,S> get() {
			return get((String[])null);
		}
		public final Either<HttpFailure,S> put(final String... path) {
			return super.put(buildPath(path));
		}
		public final Either<HttpFailure,S> put() {
			return put((String[])null);
		}
		public final Option<HttpFailure> putOption() {
			final Either<HttpFailure,S> either = put();
			return either.success() ?
				None.<HttpFailure>none() :
				Some.<HttpFailure>some(either.left());
		}
		public final Either<HttpFailure,S> delete(final String... path) {
			return super.delete(buildPath(path));
		}
		public final Option<HttpFailure> deleteOption(final String... path) {
			final Either<HttpFailure,S> either = delete(path);
			return either.success() ?
				None.<HttpFailure>none() :
				Some.<HttpFailure>some(either.left());
		}
		public final Option<HttpFailure> deleteOption() {
			return deleteOption((String[])null);
		}
		private final String buildPath(final String... path) {
			final StringBuilder sb = new StringBuilder(SLASH_STRING);
			if(path != null && path.length > 0) {
				sb.append(urlEncode(varargsToPathString(path)));
			}
			return sb.toString();
		}
	}
	    
	@Override
	public Either<HttpFailure,List<Bucket>> listBuckets() {
		return new AwsS3HttpClosure<List<Bucket>>(client_, SC_OK) {
			@Override
			public List<Bucket> success(final HttpSuccess success) throws Exception {
				return new Unmarshallers.ListBucketsUnmarshaller()
					.unmarshall(success.getContent());
			}
		}.get();
	}

	@Override
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName,
                                                         final String marker,
                                                         final String... path) {
		return new AwsS3HttpClosure<ObjectListing>(client_, SC_OK, bucketName) {
			@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				final URIBuilder builder = new URIBuilder(request.getURI());
				if(marker != null) {
					builder.addParameter(S3_PARAM_MARKER, marker);
				}
				// Add the prefix string to the request if we have one.
		    	if(path != null && path.length > 0) {
		    		builder.addParameter(S3_PARAM_PREFIX,
		    			varargsToPathString(path));
		    	}
		    	request.setURI(builder.build());
			}
			@Override
			public ObjectListing success(final HttpSuccess success) throws Exception {
				return new Unmarshallers.ListObjectsUnmarshaller()
					.unmarshall(success.getContent());
			}
		}.get();
	}
	
	@Override
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName,
                                                         final String marker) {
		return listObjects(bucketName, marker, (String[])null);
	}
	
	@Override
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName) {
		return listObjects(bucketName, null);
	}

	@Override
	public Option<HttpFailure> createBucket(final String bucketName) {
    	return new AwsS3HttpClosure<Bucket>(client_, SC_OK, bucketName) {
            @Override
            public void prepare(final AwsHttpRequest request) throws Exception {
                // https://github.com/markkolich/kolich-aws/issues/1
                // Can only send the CreateBucketConfiguration if we're *not*
                // creating a bucket in the US region.
                final String regionId;
                if((regionId = region_.getRegionId()) != null) {
                    final XmlWriter xml = new XmlWriter();
                    xml.start("CreateBucketConfiguration", "xmlns",
                        XML_NAMESPACE);
                    xml.start("LocationConstraint").value(regionId).end();
                    xml.end();
                    // Attach the XML entity to the request.
                    final HttpRequestBase base = request.getRequestBase();
                    ((HttpPut)base).setEntity(new ByteArrayEntity(
                        xml.getBytes()));
                }
            }
    		@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
		}.putOption();
	}

	@Override
	public Option<HttpFailure> deleteBucket(final String bucketName) {
    	return new AwsS3HttpClosure<Void>(client_, SC_NO_CONTENT, bucketName) {
    		@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
    	}.deleteOption();
	}

	@Override
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
                                                         final boolean rrs,
                                                         final ContentType type,
                                                         final InputStream input,
                                                         final long contentLength,
                                                         final String... path) {
		return new AwsS3HttpClosure<PutObjectResult>(client_, SC_OK, bucketName) {
			@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
			@Override
			public void prepare(final AwsHttpRequest request) throws Exception {
				final HttpRequestBase base = request.getRequestBase();
				if(rrs) {
					base.setHeader(STORAGE_CLASS, S3_REDUCED_REDUNDANCY);
		    	}
				// Although InputStreamEntity lets you specify a Content-Type,
				// we're intentionally forcing the issue here.  It seems that
				// setting the content type on the request through a vanilla
				// InputStreamEntity does not actually do the right thing.
				if(type != null) {
					base.setHeader(CONTENT_TYPE, type.toString());
				}
				((HttpPut)base).setEntity(new InputStreamEntity(input,
					contentLength));
			}
			@Override
			public PutObjectResult success(final HttpSuccess success)
                throws Exception {
				final PutObjectResult result = new PutObjectResult();
				result.setETag(success.getETag());
				result.setVersionId(success.getFirstHeader(S3_VERSION_ID));
				return result;
			}
    	}.put(path);
	}
	
	@Override
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
                                                         final ContentType type,
                                                         final InputStream input,
                                                         final long contentLength,
                                                         final String... path) {
		return putObject(bucketName, false, type, input, contentLength, path);
	}
	
	@Override
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
                                                         final InputStream input,
                                                         final long contentLength,
                                                         final String... path) {
		return putObject(bucketName, null, input, contentLength, path);
	}
	
	@Override
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
                                                         final boolean rrs,
                                                         final ContentType type,
                                                         final byte[] object,
                                                         final String... path) {
		return putObject(bucketName, rrs, type,
			new ByteArrayInputStream(object), object.length,
			path);
	}
	
	@Override
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
                                                         final ContentType type,
                                                         final byte[] object,
                                                         final String... path) {
		return putObject(bucketName, false, type, object, path);
	}
	
	@Override
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
                                                         final byte[] object,
                                                         final String... path) {
		return putObject(bucketName, null, object, path);
	}

	@Override
	public Option<HttpFailure> deleteObject(final String bucketName,
                                            final String... path) {
		return new AwsS3HttpClosure<Void>(client_, SC_NO_CONTENT, bucketName) {
			@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
		}.deleteOption(path);
	}
	
	@Override
	public Either<HttpFailure,List<Header>> getObject(final String bucketName,
                                                      final OutputStream destination,
                                                      final String... path) {
		return new AwsS3HttpClosure<List<Header>>(client_, SC_OK, bucketName) {
			@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
			@Override
			public List<Header> success(final HttpSuccess success) throws Exception {
				// Copy the object.
				copyLarge(success.getContent(), destination);
				// Get and return the headers on the HTTP response.
				// This is where stuff like "Content-Type" and
				// "Content-Length" live.
				return Arrays.asList(success.getResponse().getAllHeaders());
			}
    	}.get(path);
	}
	
	@Override
	public Either<HttpFailure,byte[]> getObject(final String bucketName,
                                                final String... path) {
		return new AwsS3HttpClosure<byte[]>(client_, SC_OK, bucketName) {
			@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
			@Override
			public byte[] success(final HttpSuccess success) throws Exception {
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				// Copy the object to the ByteArrayOutputStream.  The consumer
				// of this method should be keenly aware that this method
				// copies the response body entirely into memory in order to
				// ultimately return the response as a byte[] array.
				copyLarge(success.getContent(), os);
				return os.toByteArray();
			}
    	}.get(path);
	}
	
	@Override
	public boolean objectExists(final String bucketName,
                                final String... path) {
		return new AwsS3HttpClosure<Void>(client_, SC_OK, bucketName) {
			@Override
			public void validate() throws Exception {
				checkNotNull(bucketName, "Bucket name cannot be null.");
				checkState(isValidBucketName(bucketName), "Invalid bucket name, " +
					"did not match expected bucket name pattern.");
			}
		}.head(path).success();
	}
	
	private static final boolean isValidBucketName(final String bucketName) {
    	return VALID_BUCKET_NAME_PATTERN.matcher(bucketName).matches();
    }
	
	/**
	 * Given a variable list of arguments, prepare a fully qualified
	 * path to a key in an S3 bucket.  Each prefix in the list is
	 * separated by an appropriate path separator.  The resulting string
	 * is NOT URL-encoded, but each prefix component is URL-encoded before
	 * concatenated to the resulting path -- slashes and other special
	 * characters in a prefix component that may be interpreted wrong when
	 * used in a path are URL-encoded so we won't have any conflicts.
	 * Note that empty strings in the varargs prefix list will NOT be appended
	 * to the resulting prefix string.
	 * Example:
	 * <code>
	 * new String[]{"accounts", "", "silly/path+dog"}
	 * </code>
	 * is returned as
	 * <code>
	 * "accounts/silly%2Fpath%2Bdog"
	 * </code>
	 */
	public static final String varargsToPathString(final String... path) {
		checkNotNull(path, "The path string list cannot be null.");
		final StringBuilder sb = new StringBuilder();
		for(int i = 0, l = path.length; i < l; i++) {
			if(!EMPTY_STRING.equals(path[i])) {
				sb.append(urlEncode(path[i]));
				// Don't append a "/" if this element is the last in the list.
				sb.append((i < l-1) ? SLASH_STRING : EMPTY_STRING);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Given a prefix string, generated by
	 * {@link KolichS3Client#varargsToPathString(String...)}, returns
	 * a variable arguments compatible String[] array containing each prefix
	 * component.  Each component in the resulting prefix String[] array will be
	 * fully URL-decoded.  Note that any empty strings, once the prefix string
	 * is split around a path separator, are NOT added to the resulting
	 * varargs list.
	 */
	public static final String[] pathStringToVarargs(final String path) {
		checkNotNull(path, "The path string list cannot be null.");
		final List<String> prl = Lists.newLinkedList();
		for(final String p : path.split(quote(SLASH_STRING))) {
			if(!EMPTY_STRING.equals(p)) {
				prl.add(urlDecode(p));
			}
		}
		return prl.toArray(new String[]{});
	}
	
	/**
	 * Appends the given key to the end of the prefix list, then returns a
	 * a new String[] array representing that list.
	 */
	public static final String[] appendKeyToPath(final String key,
                                                 final String... path) {
		checkNotNull(key, "The key to append cannot be null.");
		checkNotNull(path, "The path string list cannot be null.");
		final List<String> prl = Lists.newArrayList(Arrays.asList(path));
        // The entity key becomes the last element in the prefix list.
    	prl.add(key);
    	return prl.toArray(new String[]{});
	}

}
