package org.opengis.cite.kml2.c1;

import java.util.logging.Level;

import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.SuiteAttribute;
import org.opengis.cite.kml2.util.TestSuiteLogger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.w3c.dom.Document;

/**
 * Checks that various preconditions are satisfied before the test suite is run.
 * If any of these (BeforeSuite) methods fail, all tests are skipped.
 */
public class SuitePreconditions {

	/**
	 * Verifies that a KML document was obtained as a result of parsing the URI
	 * given as the value of the required 'kml' argument.
	 *
	 * @param testContext
	 *            Information about the (pending) test run.
	 */
	@BeforeSuite
	public void verifyKMLDocument(ITestContext testContext) {
		Object sut = testContext.getSuite().getAttribute(
				SuiteAttribute.TEST_SUBJECT.getName());
		if (null != sut && Document.class.isInstance(sut)) {
			Document doc = Document.class.cast(sut);
			Assert.assertEquals(doc.getDocumentElement().getNamespaceURI(),
					KML2.NS_NAME,
					ErrorMessage.get(ErrorMessageKeys.NAMESPACE_NAME));
		} else {
			String msg = String
					.format("Value of test suite attribute %s is missing or is not an XML document.",
							SuiteAttribute.TEST_SUBJECT.getName());
			TestSuiteLogger.log(Level.SEVERE, msg);
			throw new AssertionError(msg);
		}
	}
}
