package com.kolich.aws.signing;

public interface AwsSigner {

	/**
	 * Signs the input String and returns the signature.
	 * @param credentials the {@link AwsCredentials} object where our
	 * AWS access key and access key secret live
	 * @param input the input String to sign
	 */
	public String sign(final AwsCredentials credentials, final String input);
	
	/**
	 * A convenience method to return the signing algorithm by name.
	 */
	public String getAlgorithmName();
		
}