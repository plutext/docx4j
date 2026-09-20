/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
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
package org.docx4j.fop.wordlayout;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import org.docx4j.convert.out.fo.FopCapabilities;
import org.docx4j.convert.out.fo.FopCapabilities.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The docx4j FO renderer's hooks, as method handles: for each FOP member the Word layout
 * reads, the fork's public accessor when the renderer advertises the hook, or null, in
 * which case the caller uses the field it read by reflection before the fork existed.
 * So on Apache FOP nothing changes, and on the fork nothing needs {@code setAccessible}.
 *
 * <p>Compiled against Apache FOP (the default dependency), this module cannot name a
 * fork-only method in source; it looks it up by name, once, through the public lookup,
 * which is what makes the fork's accessors a contract rather than a private detail.
 * A hook the renderer advertises but whose member is missing is a broken contract: it
 * is logged once, and the field path is used.</p>
 *
 * <p>CR-020 phase 1. @since 17.2.0</p>
 */
final class FopHooks {

	private static final Logger log = LoggerFactory.getLogger(FopHooks.class);

	static final boolean PAIR_TABLE = FopCapabilities.has(Capability.PAIR_TABLE);
	static final boolean LEADER_PLACEMENT = FopCapabilities.has(Capability.LEADER_PLACEMENT);
	static final boolean INLINE_ACCESS = FopCapabilities.has(Capability.INLINE_ACCESS);

	private FopHooks() {}

	/**
	 * The handle of a public method of the renderer, or null when {@code hook} is off or
	 * the method is not there.
	 */
	static MethodHandle method(boolean hook, Class<?> owner, String name, Class<?>... params) {
		if (!hook) return null;
		try {
			return MethodHandles.publicLookup().unreflect(owner.getMethod(name, params));
		} catch (ReflectiveOperationException | RuntimeException e) {
			log.warn("FO renderer advertises a hook but " + owner.getSimpleName() + "." + name
					+ " is not public there; reading the field instead (" + e + ")");
			return null;
		}
	}

	/** The handle of a public constructor of the renderer, or null (as {@link #method}). */
	static MethodHandle constructor(boolean hook, Class<?> owner, Class<?>... params) {
		if (!hook) return null;
		try {
			return MethodHandles.publicLookup().unreflectConstructor(owner.getConstructor(params));
		} catch (ReflectiveOperationException | RuntimeException e) {
			log.warn("FO renderer advertises a hook but " + owner.getSimpleName()
					+ "'s constructor is not public there; using reflection instead (" + e + ")");
			return null;
		}
	}

	/**
	 * A field made accessible, for the fallback path; null rather than an exception when
	 * {@code covered} says a hook stands in for it, so that a renderer which has the hook
	 * and not the field still works.
	 * @throws IllegalStateException when neither the hook nor the field is there
	 */
	static Field field(Class<?> owner, String name, boolean covered) {
		try {
			Field f = owner.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		} catch (ReflectiveOperationException | RuntimeException e) {
			if (covered) return null;
			throw new IllegalStateException("FOP's " + owner.getSimpleName() + " has changed (no " + name
					+ "); org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	/** Invokes a handle, turning a checked failure into the unchecked one the callers use. */
	static Object call(MethodHandle h, Object... args) {
		try {
			return h.invokeWithArguments(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable t) {
			throw new IllegalStateException(t);
		}
	}
}
