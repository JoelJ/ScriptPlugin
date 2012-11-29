package com.attask.jenkins;

import hudson.model.Result;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 10:35 AM
 */
public class ExitCodeParser {
	public static Result findResult(int exitCode, ErrorMode errorMode, String errorRange, ErrorMode unstableMode, String unstableRange) {
		if(exitCodeMatches(exitCode, errorMode, errorRange)) {
			return Result.FAILURE;
		}

		if(exitCodeMatches(exitCode, unstableMode, unstableRange)) {
			return Result.UNSTABLE;
		}

		return Result.SUCCESS;
	}

	static boolean exitCodeMatches(int exitCode, ErrorMode mode, String range) {
		range = range.replaceAll("\\s+", "");//Strip out the whitespace
		switch (mode) {
			case LESS_THAN:
			case GREATER_THAN:
			case EXACTLY:
				return mode.matches(exitCode, Integer.parseInt(range));
			case NON_ZERO:
				return mode.matches(exitCode);
			case CUSTOM:
				String[] commaSplit = range.split(",");
				for (String s : commaSplit) {
					String[] rangeSplit = s.split(">", 2);
					int left = Integer.parseInt(rangeSplit[0]);
					if(rangeSplit.length > 1) {
						int right = Integer.parseInt(rangeSplit[1]);
						if(exitCode >= left && exitCode <= right) {
							return true;
						}
					} else if(left == exitCode) {
						return true;
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("No case for " + mode); //Just to make sure we don't ever forget to add new cases.
		}
		return false;
	}
}
