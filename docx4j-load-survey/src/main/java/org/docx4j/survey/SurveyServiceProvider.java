/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.survey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * The survey's SLF4J binding: every logger hands its events to {@link LogCapture}. DEBUG and TRACE
 * are off, so docx4j builds no message for them; INFO is on for the one line the survey counts.
 */
public final class SurveyServiceProvider implements SLF4JServiceProvider {

	private ILoggerFactory loggerFactory;
	private IMarkerFactory markerFactory;
	private MDCAdapter mdcAdapter;

	@Override
	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	@Override
	public IMarkerFactory getMarkerFactory() {
		return markerFactory;
	}

	@Override
	public MDCAdapter getMDCAdapter() {
		return mdcAdapter;
	}

	@Override
	public String getRequestedApiVersion() {
		return "2.0.99";
	}

	@Override
	public void initialize() {
		loggerFactory = new Factory();
		markerFactory = new BasicMarkerFactory();
		mdcAdapter = new NOPMDCAdapter();
	}

	private static final class Factory implements ILoggerFactory {
		private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();

		@Override
		public Logger getLogger(String name) {
			return loggers.computeIfAbsent(name, SurveyLogger::new);
		}
	}

	private static final class SurveyLogger extends LegacyAbstractLogger {
		private static final long serialVersionUID = 1L;

		SurveyLogger(String name) {
			this.name = name;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public boolean isErrorEnabled() {
			return true;
		}

		@Override
		protected String getFullyQualifiedCallerName() {
			return null;
		}

		@Override
		protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern,
				Object[] arguments, Throwable throwable) {
			LogCapture.log(level, name, messagePattern, arguments, throwable);
		}
	}
}
