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

package com.kolich.aws.services.s3;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.entity.ContentType;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.Option;
import com.kolich.common.util.URLEncodingUtils;
import com.kolich.http.common.response.HttpFailure;

public interface S3Client {
		
	/**
	 * List all buckets.
	 * @return a list of {@link Bucket} objects
	 */
	public Either<HttpFailure,List<Bucket>> listBuckets();
	
	/**
	 * List all objects inside of a bucket.  Note that the caller should always
	 * assume the keys contained in the resulting {@link ObjectListing} may be
	 * URL-encoded, and therefore, the caller should call
	 * {@link URLEncodingUtils#urlDecode(String)} on each key to obtain its
	 * "real" unencoded String value.
	 * @param bucketName the name of the bucket
	 * @param marker the pagination marker, may be null
	 * @param path limits the response to keys that begin with
	 * the specified path. The path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 */
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName,
		final String marker, final String... path);
	
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName,
		final String marker);
	
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName);
	
	/**
	 * Create a bucket.
	 * @param bucketName the name of the bucket
	 * @return a {@link Bucket} object
	 */
	public Option<HttpFailure> createBucket(final String bucketName);

	/**
	 * Without the "recursive" parameter, we don't try to delete the
	 * contents first, and we will immediately fail if it is not empty.
	 * @param bucketName the name of the bucket to delete
	 */
	public Option<HttpFailure> deleteBucket(final String bucketName);
	
	/**
	 * Put an object into a bucket.
	 * @param bucketName the name of the bucket
	 * @param type the {@link AwsContentType} of the object
	 * @param object the byte[] array representing the object iself
	 * @param rrs set to true to suggest the storage engine use
	 * reduced redundancy storage mode. Set to false to use a standard
	 * storage mode
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 * @return a {@link PutObjectResult} object that contains metadata
	 * about the object flushed to S3
	 */
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final boolean rrs, final ContentType type, final InputStream input,
		final long contentLength, final String... path);
	
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final ContentType type, final InputStream input,
		final long contentLength, final String... path);
	
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final InputStream input, final long contentLength,
		final String... path);
	
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final boolean rrs, final ContentType type, final byte[] object,
		final String... path);
	
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final ContentType type, final byte[] object, final String... path);
	
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final byte[] object, final String... path);
	
	/**
	 * Delete an object.
	 * @param bucketName the name of the bucket
	 * @param name the name (key) of the object in the bucket
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 */
	public Option<HttpFailure> deleteObject(final String bucketName,
		final String... path);
	
	/**
	 * Get an object.
	 * @param bucketName the name of the bucket
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 */
	public Either<HttpFailure,List<Header>> getObject(final String bucketName,
		final OutputStream destination, final String... path);
	
	public Either<HttpFailure,byte[]> getObject(final String bucketName,
		final String... path);
		
	/**
	 * Check if an object exists.
	 * @param bucketName the name of the bucket
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 * @return true if an object with the given path (key) exists in
	 * the bucket, false if it does not
	 */
	public boolean objectExists(final String bucketName,
		final String... path);
	
}
