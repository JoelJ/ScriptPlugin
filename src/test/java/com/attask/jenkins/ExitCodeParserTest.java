package com.attask.jenkins;

import hudson.model.Result;
import junit.framework.TestCase;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 12:08 PM
 */
public class ExitCodeParserTest extends TestCase {
	public void testFindResult() {
		Result result;
		result = ExitCodeParser.findResult(0, ErrorMode.NON_ZERO, "", ErrorMode.NON_ZERO, "");
		assertEquals("Success", Result.SUCCESS, result);

		result = ExitCodeParser.findResult(1, ErrorMode.EXACTLY, "1", ErrorMode.EXACTLY, "1");
		assertEquals("Failure should be set since it takes precedence over Unstable", Result.FAILURE, result);

		result = ExitCodeParser.findResult(1, ErrorMode.EXACTLY, "1", ErrorMode.EXACTLY, "2");
		assertEquals("Error should be set", Result.FAILURE, result);

		result = ExitCodeParser.findResult(2, ErrorMode.EXACTLY, "1", ErrorMode.EXACTLY, "2");
		assertEquals("Unstable should be set", Result.UNSTABLE, result);
	}

	public void testExitCodeMatches_CUSTOM_singleValue() {
		boolean result;
		result = ExitCodeParser.exitCodeMatches(1000, ErrorMode.CUSTOM, "1000");
		assertTrue("same", result);

		result = ExitCodeParser.exitCodeMatches(1000, ErrorMode.CUSTOM, "-1000");
		assertFalse("not same", result);
	}

	public void testExitCodeMatches_CUSTOM_multipleValues() {
		boolean result;
		result = ExitCodeParser.exitCodeMatches(1000, ErrorMode.CUSTOM, "1000,2000");
		assertTrue("same 1000", result);

		result = ExitCodeParser.exitCodeMatches(2000, ErrorMode.CUSTOM, "1000,2000");
		assertTrue("same 2000", result);

		result = ExitCodeParser.exitCodeMatches(1500, ErrorMode.CUSTOM, "1000,2000");
		assertFalse("not same", result);
	}

	public void testExitCodeMatches_CUSTOM_multipleRanges() {
		boolean result;
		result = ExitCodeParser.exitCodeMatches(-10, ErrorMode.CUSTOM, "-10>10,20>40");
		assertTrue("same -10", result);

		result = ExitCodeParser.exitCodeMatches(10, ErrorMode.CUSTOM, "-10>10,20>40");
		assertTrue("same 10", result);

		result = ExitCodeParser.exitCodeMatches(20, ErrorMode.CUSTOM, "-10>10,20>40");
		assertTrue("same 20", result);

		result = ExitCodeParser.exitCodeMatches(40, ErrorMode.CUSTOM, "-10>10,20>40");
		assertTrue("same 40", result);

		result = ExitCodeParser.exitCodeMatches(15, ErrorMode.CUSTOM, "-10>10,20>40");
		assertFalse("not same 15", result);

		result = ExitCodeParser.exitCodeMatches(-15, ErrorMode.CUSTOM, "-10>10,20>40");
		assertFalse("not same -15", result);

		result = ExitCodeParser.exitCodeMatches(41, ErrorMode.CUSTOM, "-10>10,20>40");
		assertFalse("not same 41", result);
	}

	public void testExitCodeMatches_CUSTOM_simpleRange() {
		boolean result;
		result = ExitCodeParser.exitCodeMatches(1000, ErrorMode.CUSTOM, "1000>2000");
		assertTrue("equal low end", result);

		result = ExitCodeParser.exitCodeMatches(1500, ErrorMode.CUSTOM, "1000>2000");
		assertTrue("equal middle", result);

		result = ExitCodeParser.exitCodeMatches(2000, ErrorMode.CUSTOM, "1000>2000");
		assertTrue("equal high end", result);

		result = ExitCodeParser.exitCodeMatches(2001, ErrorMode.CUSTOM, "1000>2000");
		assertFalse("not same greater", result);

		result = ExitCodeParser.exitCodeMatches(999, ErrorMode.CUSTOM, "1000>2000");
		assertFalse("not same lower", result);
	}

	public void testExitCodeMatches_CUSTOM_simpleRange_negative() {
		boolean result;
		result = ExitCodeParser.exitCodeMatches(-1000, ErrorMode.CUSTOM, "-1000>1000");
		assertTrue("equal low end", result);

		result = ExitCodeParser.exitCodeMatches(0, ErrorMode.CUSTOM, "-1000>1000");
		assertTrue("equal middle", result);

		result = ExitCodeParser.exitCodeMatches(1000, ErrorMode.CUSTOM, "-1000>1000");
		assertTrue("equal high end", result);

		result = ExitCodeParser.exitCodeMatches(1001, ErrorMode.CUSTOM, "-1000>1000");
		assertFalse("not same greater", result);

		result = ExitCodeParser.exitCodeMatches(-1001, ErrorMode.CUSTOM, "-1000>1000");
		assertFalse("not same lower", result);
	}

	public void testExitCodeMatches_NON_ZERO() {
		boolean result;

		result = ExitCodeParser.exitCodeMatches(0, ErrorMode.NON_ZERO, "asdfasdfasdf");
		assertFalse("zero", result);

		result = ExitCodeParser.exitCodeMatches(Integer.MAX_VALUE, ErrorMode.NON_ZERO, "asdfasdfasdf");
		assertTrue("positive", result);

		result = ExitCodeParser.exitCodeMatches(Integer.MIN_VALUE, ErrorMode.NON_ZERO, "asdfasdfasdf");
		assertTrue("negative", result);
	}

	public void testExitCodeMatches_EXACTLY() {
		boolean result;

		result = ExitCodeParser.exitCodeMatches(10, ErrorMode.EXACTLY, "-10");
		assertFalse("not-equal", result);

		result = ExitCodeParser.exitCodeMatches(-10, ErrorMode.EXACTLY, "-10");
		assertTrue("equal", result);
	}

	public void testExitCodeMatches_GREATER_THAN() {
		boolean result;

		result = ExitCodeParser.exitCodeMatches(0, ErrorMode.GREATER_THAN, "10");
		assertFalse("less than", result);

		result = ExitCodeParser.exitCodeMatches(10, ErrorMode.GREATER_THAN, "10");
		assertFalse("equal", result);

		result = ExitCodeParser.exitCodeMatches(100, ErrorMode.GREATER_THAN, "10");
		assertTrue("greater than", result);
	}

	public void testExitCodeMatches_LESS_THAN() {
		boolean result;

		result = ExitCodeParser.exitCodeMatches(0, ErrorMode.LESS_THAN, "10");
		assertTrue("less than", result);

		result = ExitCodeParser.exitCodeMatches(10, ErrorMode.LESS_THAN, "10");
		assertFalse("equal", result);

		result = ExitCodeParser.exitCodeMatches(100, ErrorMode.LESS_THAN, "10");
		assertFalse("greater than", result);
	}
}
