/*
 * File: TweetMojo.java
 * 
 * Copyright 2013 OSFramework Project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osframework.maven.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Sends a message to Twitter (aka 'tweet') via the Twitter API v1.1.
 * 
 * @goal tweet
 * @phase deploy
 * @requiresProject true
 * @requiresOnline true
 * 
 * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
 * @see <a href="https://dev.twitter.com/docs/api/1.1">Twitter - REST API v1.1 Resources</a>
 */
public class TweetMojo extends AbstractMojo {

	/**
	 * Reference to the enclosing Maven project.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Generated consumer key for this project.
	 * 
	 * @parameter expression="${consumerKey}"
	 * @required
	 */
	private String consumerKey;

	/**
	 * Generated consumer secret for this project.
	 * 
	 * @parameter expression="${consumerSecret}"
	 * @required
	 */
	private String consumerSecret;

	/**
	 * Generated access token for this project.
	 * 
	 * @parameter expression="${accessToken}"
	 */
	private String accessToken;

	/**
	 * Generated access token secret for this project.
	 * 
	 * @parameter expression="${accessTokenSecret}"
	 */
	private String accessTokenSecret;

	/**
	 * Text of status ('tweet') to post to Twitter.
	 * 
	 * @parameter expression="${message}" default-value="${project.artifactId}:${project.version} released"
	 */
	private String message;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// Validate required parameters
		if (null == project) {
			throw new MojoExecutionException("Expected POM reference is null");
		}
		if (StringUtils.isBlank(consumerKey)) {
			throw new MojoFailureException("Missing required parameter 'consumerKey'");
		} else if (StringUtils.isBlank(consumerSecret)) {
			throw new MojoFailureException("Missing required parameter 'consumerSecret'");
		}
		Twitter twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		try {
			// Set access token
			AccessToken oauthToken = acquireAccessToken(twitter);
			twitter.setOAuthAccessToken(oauthToken);
			getLog().info("Authenticated; posting tweet...");
			
			Status status = twitter.updateStatus(createStatus());
			getLog().info("Posted tweet. Details:");
			getLog().info("* Created at: " + formatDate(status.getCreatedAt()));
			getLog().info("* Text:       '" + status.getText() + "'");
		} catch (IOException ioe) {
			getLog().error("Failed to read PIN from input");
			throw new MojoFailureException("Failed to read PIN from input", ioe);
		} catch (TwitterException te) {
			getLog().error("Failed to post tweet. Details:");
			getLog().error("Message: " + te.getErrorMessage());
			getLog().error("Code: " + te.getExceptionCode());
			throw new MojoExecutionException("Failed to post tweet.", te);
		}
	}

	private AccessToken acquireAccessToken(Twitter twitter) throws IOException, TwitterException {
		AccessToken token = null;
		if (StringUtils.isNotBlank(accessToken) && StringUtils.isNotBlank(accessTokenSecret)) {
			token = new AccessToken(accessToken, accessTokenSecret);
		} else {
			RequestToken requestToken = twitter.getOAuthRequestToken();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (null == token) {
				getLog().info("Open the following URL and grant access to your account:");
				getLog().info(requestToken.getAuthorizationURL());
				System.out.print("Enter the PIN (if available) or just hit enter. [PIN]:");
				String pin = br.readLine();
				token = (0 < pin.length())
						 ? twitter.getOAuthAccessToken(requestToken, pin)
						 : twitter.getOAuthAccessToken();
			}
		}
		return token;
	}

	private String createStatus() {
		String status = (StringUtils.isNotBlank(message))
				         ? message
				         : new StringBuilder(project.getArtifactId())
		                       .append(":")
		                       .append(project.getVersion())
		                       .append(" released")
		                       .toString();
		return status;
	}

	private String formatDate(Date d) {
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return df.format(d);
	}

}
