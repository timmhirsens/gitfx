package de.br0tbox.gitfx.core.util;

public final class Preconditions {

	public static <T> void checkNotNull(T object, String parameterName) {
		if (object == null) {
			throw new IllegalArgumentException(String.valueOf(parameterName) + " must not be null");
		}
	}

}
