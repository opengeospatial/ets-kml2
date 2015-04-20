package org.opengis.cite.kml2.c2;

import org.opengis.cite.kml2.SuiteAttribute;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;

/**
 * Verifies that the preconditions for running CL2 tests are satisfied. If not,
 * they are all skipped.
 */
public class CL2Preconditions {

	/**
	 * Checks the value of the {@link SuiteAttribute#LEVEL} attribute; its
	 * (Integer) value must be 2 or greater.
	 * 
	 * @param testContext
	 *            Information about the current test run.
	 */
	@BeforeTest
	public void checkConformanceLevel(ITestContext testContext) {
		Integer level = (Integer) testContext.getSuite().getAttribute(
				SuiteAttribute.LEVEL.getName());
		Assert.assertTrue(level > 1,
				String.format("Skipping CL2 tests: lvl !> 1."));
	}

}
