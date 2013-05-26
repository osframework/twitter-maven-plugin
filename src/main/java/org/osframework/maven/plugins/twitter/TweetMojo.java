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
package org.osframework.maven.plugins.twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.IOUtil;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Sends a status update to Twitter (aka 'tweet') via Twitter API.
 * 
 * @since 1.0.0
 * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
 * @see <a href="https://dev.twitter.com/docs/api/1.1/post/statuses/update">POST statuses/update | Twitter Developers</a>
 */
@Mojo(name = "tweet",
      defaultPhase = LifecyclePhase.DEPLOY,
      requiresProject = true,
      requiresOnline = true)
public class TweetMojo extends AbstractTwitterMojo {

	protected void executeInTwitter(Twitter twitter) throws MojoExecutionException, MojoFailureException {
		try {
			Status status = twitter.updateStatus(getMessage());
			getLog().info("Sent tweet: " + status.getText());
			logStatus(status);
		} catch (TwitterException te) {
			throw new MojoFailureException("Could not send tweet", te);
		}
	}

	private String formatDate(Date d) {
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return df.format(d);
	}

	private void logStatus(Status status) {
		StringBuilder logMsg = new StringBuilder(formatDate(status.getCreatedAt()))
		                           .append(" ")
		                           .append(status.getText());
		File logFile = new File(getWorkDirectory(), "tweet.log");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(logMsg.toString());
		} catch (IOException ioe) {
			getLog().warn("Could not write status to tweet.log");
		} finally {
			IOUtil.close(writer);
		}
	}

}
