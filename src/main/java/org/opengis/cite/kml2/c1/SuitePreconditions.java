package org.opengis.cite.kml2.c1;

import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.opengis.cite.kml2.ETSAssert;
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
	 * Verifies that the document (root) element has the following infoset
	 * properties:
	 * <ul>
	 * <li>[namespace name] = {@value org.opengis.cite.kml2.KML2#NS_NAME}</li>
	 * <li>[local name] = {@value org.opengis.cite.kml2.KML2#DOC_ELEMENT}</li>
	 * </ul>
	 *
	 * @param testContext
	 *            Information about the (pending) test run.
	 */
	@BeforeSuite(description = "ATC-101")
	public void isKMLDocument(ITestContext testContext) {
		Object sut = testContext.getSuite().getAttribute(
				SuiteAttribute.TEST_SUBJECT.getName());
		if (null != sut && Document.class.isInstance(sut)) {
			Document doc = Document.class.cast(sut);
			ETSAssert.assertQualifiedName(doc.getDocumentElement(), new QName(
					KML2.NS_NAME, KML2.DOC_ELEMENT));
		} else {
			String msg = String
					.format("Value of test suite attribute %s is missing or does not refer to an XML resource.",
							SuiteAttribute.TEST_SUBJECT.getName());
			TestSuiteLogger.log(Level.SEVERE, msg);
			throw new AssertionError(msg);
		}
	}

	/**
	 * Verifies that the version is supported (default value is "2.2:).
	 *
	 * @param testContext
	 *            Information about the (pending) test run.
	 */
	@BeforeSuite
	public void checkVersion(ITestContext testContext) {
		Object ver = testContext.getSuite().getAttribute(
				SuiteAttribute.KML_VERSION.getName());
		if (null != ver) {
			String version = ver.toString();
			Assert.assertTrue(
					version.startsWith("2.2") || version.startsWith("2.3"),
					ErrorMessage.format(ErrorMessageKeys.UNSUPPORTED_VERSION,
							version));
		} else {
			String msg = String.format(
					"Value of test suite attribute %s is missing.",
					SuiteAttribute.KML_VERSION.getName());
			TestSuiteLogger.log(Level.SEVERE, msg);
			throw new AssertionError(msg);
		}
	}

}
