package com.attask.jenkins;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 10:31 AM
 */
public enum ErrorMode {
	NONE("None"),
	LESS_THAN("Less Than"),
	GREATER_THAN("Greater Than"),
	EXACTLY("Exactly"),
	NON_ZERO("Non-Zero"),
	CUSTOM("Custom");

	private final String humanReadable;

	private ErrorMode(String humanReadable) {
		this.humanReadable = humanReadable;
	}

	public String getHumanReadable() {
		return humanReadable;
	}

	public boolean matches(int toCheck, int value) {
		switch (this) {
			case LESS_THAN:
				return toCheck < value;
			case GREATER_THAN:
				return toCheck > value;
			case EXACTLY:
				return toCheck == value;
			case NON_ZERO:
				return toCheck != 0;
			default:
				throw new UnsupportedOperationException("Cannot call matches on a " + this + "ErrorMode");
		}

	}

	public boolean matches(int exitCode) {
		return matches(exitCode, 0);
	}
}
