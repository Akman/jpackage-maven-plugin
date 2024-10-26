/*
  Copyright (C) 2020 - 2024 Alexander Kapitman

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package ru.akman.commons;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils.
 */
public final class CommonUtils {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

  /**
   * Default logger context.
   */
  private static final LoggerContext LOG_CONTEXT =
      (LoggerContext) LoggerFactory.getILoggerFactory();

  /**
   * Default logger name.
   */
  private static final String ROOT_LOG_NAME = "ROOT";

  /**
   * Default logger level.
   */
  private static final Level DEFAULT_LOG_LEVEL = Level.ERROR;

  private CommonUtils() {
    // not called
    if (LOG.isErrorEnabled()) {
      LOG.error("Call private constructor");
    }
    throw new UnsupportedOperationException();
  }

  /**
   * Set root logger level.
   * The <code>OFF</code> is used to turn off logging.
   * The <code>ERROR</code> level designates error events which may or not
   * be fatal to the application.
   * The <code>WARN</code> level designates potentially harmful situations.
   * The <code>INFO</code> level designates informational messages
   * highlighting overall progress of the application.
   * The <code>DEBUG</code> level designates informational events of lower
   * importance.
   * The <code>TRACE</code> level designates informational events of very low
   * importance.
   * The <code>ALL</code> is used to turn on all logging.
   * If passing level is invalid then used default level <code>ERROR</code>.
   *
   * @param level root logger level
   */
  public static void setLoggerLevel(final String level) {
    setLoggerLevel(level, ROOT_LOG_NAME);
  }

  /**
   * Set logger level.
   * The <code>OFF</code> is used to turn off logging.
   * The <code>ERROR</code> level designates error events which may or not
   * be fatal to the application.
   * The <code>WARN</code> level designates potentially harmful situations.
   * The <code>INFO</code> level designates informational messages
   * highlighting overall progress of the application.
   * The <code>DEBUG</code> level designates informational events of lower
   * importance.
   * The <code>TRACE</code> level designates informational events of very low
   * importance.
   * The <code>ALL</code> is used to turn on all logging.
   * If passing level is invalid then used default level <code>ERROR</code>.
   *
   * @param level logger level
   * @param name logger name (class name or <code>"ROOT"</code>)
   */
  public static void setLoggerLevel(final String level, final String name) {
    if (LOG_CONTEXT != null) {
      LOG_CONTEXT.getLogger(name).setLevel(
          Level.toLevel(level, DEFAULT_LOG_LEVEL));
    }
  }

  /**
   * Setup system output and error streams.
   */
  public static void setupSystemStreams() {
    System.setOut(new PrintStream(System.out, true, Charset.defaultCharset()));
    System.setErr(new PrintStream(System.err, true, Charset.defaultCharset()));
  }

  /**
   * Load resource by name and charset.
   *
   * @param resourceName resource name (path)
   * @param resourceCharset resource charset
   * @return properties object
   */
  public static Properties loadResource(final String resourceName,
      final Charset resourceCharset) {
    final URL resourceUrl = CommonUtils.class.getResource(resourceName);
    if (resourceUrl == null) {
      throw new MissingResourceException(
          "Resource not found", CommonUtils.class.getName(), resourceName);
    }
    final Properties properties = new Properties();
    try (InputStreamReader isr = new InputStreamReader(
        resourceUrl.openStream(), resourceCharset)) {
      properties.load(isr);
    } catch (IOException ex) {
      throw (MissingResourceException) new MissingResourceException(
          "Can't load resource", CommonUtils.class.getName(), resourceName)
          .initCause(ex);
    }
    return properties;
  }

  /**
   * Dump properties.
   *
   * @param properties properties object
   */
  public static void dumpProperties(final Properties properties) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("locale = {}", Locale.getDefault());
      LOG.debug("charset = {}", Charset.defaultCharset());
      LOG.debug("file.encoding = {}", System.getProperty("file.encoding"));
      properties.stringPropertyNames().forEach(name -> {
        LOG.debug("{} = {}", name, properties.getProperty(name));
      });
    }
  }

}
