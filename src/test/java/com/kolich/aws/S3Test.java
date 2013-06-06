package com.kolich.aws;

import org.apache.http.client.HttpClient;

import com.amazonaws.services.s3.model.Bucket;
import com.kolich.aws.services.s3.S3Client;
import com.kolich.aws.services.s3.impl.KolichS3Client;
import com.kolich.common.functional.either.Either;
import com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory;
import com.kolich.http.common.response.HttpFailure;

public class S3Test {

	public static void main(String[] args) {
		
		final HttpClient client = KolichHttpClientFactory.getNewInstanceNoProxySelector();
		
		final S3Client s3 = new KolichS3Client(client, "foo", "bar");
		
		final Either<HttpFailure,Bucket> bucket = s3.createBucket("39824728473294723483274ldjlkafj");
		if(bucket.success()) {
			System.out.println("worked!!");
		} else {
			System.out.println(bucket.left().getStatusCode());
		}
		
		final Either<HttpFailure,Void> delete = s3.deleteBucket("39824728473294723483274ldjlkafj");
		if(delete.success()) {
			System.out.println("deleted!!");
		}

	}

}
