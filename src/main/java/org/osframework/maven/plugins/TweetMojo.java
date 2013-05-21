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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Sends a message to Twitter (aka 'tweet') via the Twitter API v1.1.
 * 
 * @goal tweet
 * @phase deploy
 * @requiresProject true
 * @requiresOnline true
 * 
 * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
 */
public class TweetMojo extends AbstractMojo {

	/**
	 * Twitter account username.
	 * @parameter expression="${twitter.accountName}"
	 * @required
	 */
	private String accountName;

	/**
	 * Application ID registered on Twitter for this project.
	 * @parameter expression="${twitter.applicationId}"
	 * @required
	 */
	private String applicationId;

	/**
	 * Generated client token for this project.
	 * @parameter expression="${twitter.clientToken}"
	 * @required
	 */
	private String clientToken;

	/**
	 * Generated client secret for this project.
	 * @parameter expression="${twitter.clientSecret}"
	 * @required
	 */
	private String clientSecret;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// Validate required parameters
		if (StringUtils.isEmpty(accountName)) {
			throw new MojoFailureException("Missing required parameter 'accountName'");
		} else if (StringUtils.isEmpty(applicationId)) {
			throw new MojoFailureException("Missing required parameter 'applicationId'");
		} else if (StringUtils.isEmpty(clientToken)) {
			throw new MojoFailureException("Missing required parameter 'clientToken'");
		} else if (StringUtils.isEmpty(clientSecret)) {
			throw new MojoFailureException("Missing required parameter 'clientSecret'");
		}
		
		getLog().info("Tweeting as '" + accountName + "'");
		getLog().info("A new tweet would be posted now...");
	}

}
