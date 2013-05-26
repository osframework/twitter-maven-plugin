/*
 * File: DirectMessageMojo.java
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

import static org.osframework.maven.plugins.twitter.ConfigurationSettings.RECIPIENT_SCREEN_NAME;

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
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Sends a direct message to a Twitter recipient.
 * 
 * @since 1.0.0
 * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
 * @see <a href="https://dev.twitter.com/docs/api/1.1/post/direct_messages/new">POST direct_messages/new | Twitter Developers</a>
 */
@Mojo(name = "dm",
      defaultPhase = LifecyclePhase.DEPLOY,
      requiresProject = true,
      requiresOnline = true)
public class DirectMessageMojo extends AbstractTwitterMojo {

	@Parameter(property = RECIPIENT_SCREEN_NAME,
			   required = true)
	private String recipientScreenName;

	protected void executeInTwitter(Twitter twitter) throws MojoExecutionException, MojoFailureException {
		try {
			DirectMessage dm = twitter.sendDirectMessage(recipientScreenName, getMessage());
			getLog().info("Sent direct message to @" + dm.getRecipientScreenName() + ": " + dm.getText());
			logDirectMessage(dm);
		} catch (TwitterException te) {
			throw new MojoFailureException("Could not send direct message", te);
		}
	}

	private String formatDate(Date d) {
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return df.format(d);
	}

	private void logDirectMessage(DirectMessage dm) {
		StringBuilder logMsg = new StringBuilder(formatDate(dm.getCreatedAt()))
		                           .append(" ")
		                           .append(dm.getRecipientScreenName())
		                           .append(" ")
		                           .append(dm.getText());
		File logFile = new File(getWorkDirectory(), "dm.log");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(logMsg.toString());
		} catch (IOException ioe) {
			getLog().warn("Could not write status to dm.log");
		} finally {
			IOUtil.close(writer);
		}
	}

}
