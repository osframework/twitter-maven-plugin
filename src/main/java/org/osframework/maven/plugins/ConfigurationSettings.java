/*
 * File: ConfigurationSettings.java
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

import twitter4j.conf.PropertyConfiguration;

/**
 * ConfigurationSettings description here.
 *
 * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
 */
public final class ConfigurationSettings {

	public static final String PLUGIN_PREFIX = "twitter";
	private static final String PROPERTY_PREFIX = PLUGIN_PREFIX + ".";

	public static final String ENCODING = PROPERTY_PREFIX + "encoding";
	public static final String MESSAGE = PROPERTY_PREFIX + "message";
	public static final String RECIPIENT_SCREEN_NAME = PROPERTY_PREFIX + "recipientScreenName";

	public static final String OAUTH_CONSUMER_KEY = PROPERTY_PREFIX + PropertyConfiguration.OAUTH_CONSUMER_KEY;
	public static final String OAUTH_CONSUMER_SECRET = PROPERTY_PREFIX + PropertyConfiguration.OAUTH_CONSUMER_SECRET;
	public static final String OAUTH_ACCESS_TOKEN = PROPERTY_PREFIX + PropertyConfiguration.OAUTH_ACCESS_TOKEN;
	public static final String OAUTH_ACCESS_TOKEN_SECRET = PROPERTY_PREFIX + PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET;

	public static final String WORK_DIRECTORY = PROPERTY_PREFIX + "workDirectory";

	private ConfigurationSettings() {}

}
