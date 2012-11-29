package com.attask.jenkins;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 3:54 PM
 */
public class FailedScriptException extends RuntimeException {
	public FailedScriptException(String message) {
		super(message);
	}
}
