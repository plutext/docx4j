/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.docx4j.com.google.common.base;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.docx4j.com.google.common.annotations.GwtCompatible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods factored out so that they can be emulated differently in GWT.
 *
 * @author Jesse Wilson
 */
@GwtCompatible(emulated = true)
final class Platform {

	private static Logger log = LoggerFactory.getLogger(Platform.class);
	
  private Platform() {}

  /** Calls {@link System#nanoTime()}. */
  @SuppressWarnings("GoodTime") // reading system time without TimeSource
  static long systemNanoTime() {
    return System.nanoTime();
  }


  static String formatCompact4Digits(double value) {
    return String.format(Locale.ROOT, "%.4g", value);
  }

  static boolean stringIsNullOrEmpty(@Nullable String string) {
    return string == null || string.isEmpty();
  }

  static String nullToEmpty(@Nullable String string) {
    return (string == null) ? "" : string;
  }

  static String emptyToNull(@Nullable String string) {
    return stringIsNullOrEmpty(string) ? null : string;
  }


  private static void logPatternCompilerError(ServiceConfigurationError e) {
    log.warn("Error loading regex compiler, falling back to next option", e);
  }

}
