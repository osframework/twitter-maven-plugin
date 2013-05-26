/*
 * File: AbstractTwitterMojo.java
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

import static org.osframework.maven.plugins.twitter.ConfigurationSettings.ENCODING;
import static org.osframework.maven.plugins.twitter.ConfigurationSettings.MESSAGE;
import static org.osframework.maven.plugins.twitter.ConfigurationSettings.OAUTH_ACCESS_TOKEN;
import static org.osframework.maven.plugins.twitter.ConfigurationSettings.OAUTH_ACCESS_TOKEN_SECRET;
import static org.osframework.maven.plugins.twitter.ConfigurationSettings.OAUTH_CONSUMER_KEY;
import static org.osframework.maven.plugins.twitter.ConfigurationSettings.OAUTH_CONSUMER_SECRET;
import static org.osframework.maven.plugins.twitter.ConfigurationSettings.WORK_DIRECTORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Abstract superclass of mojos that invoke the Twitter API.
 *
 * @since 1.0.0
 * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
 * @see <a href="https://dev.twitter.com/docs/api/1.1">Twitter - REST API v1.1 Resources</a>
 */
public abstract class AbstractTwitterMojo extends AbstractMojo {

	/**
	 * Minimum required Maven version.
	 */
	private static final String MAVEN_MIN_VERSION = "3.0";

	/**
	 * Maven project.
	 */
	@Component
	protected MavenProject project;

	/**
	 * Working directory for <tt>twitter-maven-plugin</tt> mojos. Subclasses
	 * access this value via {@link #getWorkDirectory()}. 
	 */
	@Parameter(property = WORK_DIRECTORY,
			   defaultValue = "${project.build.directory}/twitter")
	private File workDirectory;

	/**
	 * Character encoding of {@link #message}. Subclasses access this value via
	 * {@link #getEncoding()}.
	 */
	@Parameter(property = ENCODING,
			   defaultValue = "${project.build.sourceEncoding}")
	private String encoding;

	/**
	 * Text of message to be sent to Twitter.
	 */
	@Parameter(property = MESSAGE)
	private String message;

	/**
	 * Consumer key for this application, generated at time of registration.
	 */
	@Parameter(property = OAUTH_CONSUMER_KEY,
			   required = true)
	private String consumerKey;

	/**
	 * Consumer secret for this application, generated at time of registration.
	 */
	@Parameter(property = OAUTH_CONSUMER_SECRET,
			   required = true)
	private String consumerSecret;

	/**
	 * Access token for this application, generated at time of registration.
	 * Optional. If provided, Twitter API calls are made as the authorized user
	 * that registered this application.
	 * <p><u>Do not</u> share this value with anyone.</p>  
	 */
	@Parameter(property = OAUTH_ACCESS_TOKEN)
	private String accessToken;

	/**
	 * Access token secret for this application, generated at time of
	 * registration. Optional. If provided, Twitter API calls are made as the
	 * authorized user that registered this application.
	 * <p><u>Do not</u> share this value with anyone.</p>  
	 */
	@Parameter(property = OAUTH_ACCESS_TOKEN_SECRET)
	private String accessTokenSecret;

	/**
	 * Authorized OAuth access token.
	 */
	private AccessToken authToken;

	/**
	 * {@inheritDoc}
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!isMaven3OrGreater()) {
			throw new MojoExecutionException("Requires at least Maven version " + MAVEN_MIN_VERSION);
		}
		try {
			executeInTwitter(getAuthenticatedTwitter());
			storeAccessToken();
		} catch (TwitterException te) {
			throw new MojoFailureException("Could not create authorized Twitter session", te);
		}
	}

	/**
	 * Execute an action through the Twitter REST API. Concrete subclasses must
	 * implement this method.
	 * 
	 * @param twitter Authenticated, authorized Twitter API client object
	 * @throws MojoExecutionException if an unexpected problem occurs. Throwing
	 *         this exception causes a "BUILD ERROR" message to be displayed.
	 * @throws MojoFailureException if an expected problem (such as a
	 *         compilation failure) occurs. Throwing this exception causes a
	 *         "BUILD FAILURE" message to be displayed.
	 */
	protected abstract void executeInTwitter(final Twitter twitter)
		throws MojoExecutionException, MojoFailureException;

	/**
	 * Get character encoding of message. If character encoding is not
	 * specified in the project POM, a default encoding of <tt>UTF-8</tt> is
	 * used.
	 * 
	 * @return character encoding, never <code>null</code>
	 */
	protected String getEncoding() {
		return (StringUtils.isNotBlank(encoding)) ? encoding : ReaderFactory.UTF_8;
	}

	/**
	 * Get message to be sent through Twitter API. If message is not specified
	 * in the project POM, a default message is generated from project
	 * properties:
	 * <pre>
	 * ${project.artifactId}:${project.version} released!
	 * </pre>
	 * 
	 * @return message to be sent
	 */
	protected String getMessage() {
		if (StringUtils.isNotBlank(message)) {
			return message;
		} else {
			StringBuilder buf = new StringBuilder(project.getArtifactId())
			                        .append(':')
			                        .append(project.getVersion())
			                        .append(" released!");
			if (null != project.getUrl()) {
				buf.append(" ").append(project.getUrl());
			}
			return buf.toString();
		}
	}

	/**
	 * Get work directory for this mojo.
	 * 
	 * @return work directory, never <code>null</code>
	 */
	protected File getWorkDirectory() {
		if (null == workDirectory) {
			workDirectory = getDefaultWorkDirectory();
			getLog().info("Using default work directory: " + workDirectory.getPath());
		}
		if (!workDirectory.exists()) {
			workDirectory.mkdirs();
			getLog().info("Created work directory: " + workDirectory.getPath());
		}
		return workDirectory;
	}

	protected void loadAccessToken(final Twitter twitter) throws TwitterException {
		// Check for stored access token
		File tokenStore = new File(getWorkDirectory(), "auth");
		if (tokenStore.canRead()) {
			Properties p = new Properties();
			InputStream in = null;
			try {
				in = new FileInputStream(tokenStore);
				p.load(in);
			} catch (IOException ignore) {	
			} finally {
				IOUtil.close(in);
			}
			authToken = new AccessToken(p.getProperty(OAUTH_ACCESS_TOKEN), p.getProperty(OAUTH_ACCESS_TOKEN_SECRET));
		}
		// Get access token via user authorization
		else {
			RequestToken requestToken = twitter.getOAuthRequestToken();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (null == authToken) {
				getLog().info("Open the following URL and grant access to your account:");
				getLog().info(requestToken.getAuthorizationURL());
				System.out.print("Enter the PIN (if available) or just hit enter. [PIN]: ");
				try {
					String pin = br.readLine();
					authToken = (0 < pin.length()) ? twitter.getOAuthAccessToken(requestToken, pin) : twitter.getOAuthAccessToken();
				} catch (IOException ioe) {
					getLog().error("Could not read authorization PIN from input");
					throw new TwitterException(ioe);
				} catch (TwitterException te) {
					if (401 == te.getStatusCode()) {
						getLog().error("Could not acquire access token");
					}
					throw te;
				}
			}
		}
	}

	protected void storeAccessToken() {
		if (null == authToken) {
			return;
		}
		Properties p = new Properties();
		p.setProperty(OAUTH_ACCESS_TOKEN, authToken.getToken());
		p.setProperty(OAUTH_ACCESS_TOKEN_SECRET, authToken.getTokenSecret());
		File tokenStore = new File(getWorkDirectory(), "auth");
		OutputStream out = null;
		try {
			tokenStore.createNewFile();
			out = new FileOutputStream(tokenStore);
			p.store(out, "Twitter access token");
			getLog().info("Wrote access token to work directory");
		} catch (IOException ignore) {
		} finally {
			IOUtil.close(out);
		}
	}

	/**
	 * Determine if the version of Maven executing this mojo is greater than or
	 * equal to <tt>3.0</tt>.
	 * 
	 * @return <code>true</code> if Maven version is 3.0+,
	 *         <code>false</code> otherwise
	 */
	protected static boolean isMaven3OrGreater() {
		return (0 <= new ComparableVersion(getMavenVersion()).compareTo(new ComparableVersion("3.0")));
	}

	/**
	 * Get version of Maven executing this mojo. The Maven version is obtained
	 * from the POM properties of <tt>maven-core</tt>, which should always be
	 * loaded by the core ClassLoader.
	 * 
	 * @return Maven version
	 */
	protected static String getMavenVersion() {
		final Properties properties = new Properties();
		final InputStream in = MavenProject.class.getClassLoader().getResourceAsStream("META-INF/maven/org.apache.maven/maven-core/pom.properties");
		try {
			properties.load(in);
		} catch (IOException ioe) {
			return "";
		} finally {
			IOUtil.close(in);
		}
		return properties.getProperty("version").trim();
	}

	private File getDefaultWorkDirectory() {
		final StringBuilder buf = new StringBuilder(project.getBuild().getDirectory()).append(File.separatorChar).append("twitter");
		return new File(buf.toString());
	}

	private Twitter getAuthenticatedTwitter() throws TwitterException {
		if (StringUtils.isBlank(consumerKey) || StringUtils.isBlank(consumerSecret)) {
			throw new TwitterException("Missing required credentials");
		}
		Twitter twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		loadAccessToken(twitter);
		twitter.setOAuthAccessToken(authToken);
		return twitter;
	}

}
