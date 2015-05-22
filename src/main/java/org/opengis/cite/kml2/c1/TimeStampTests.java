package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Implements tests that apply to kml:TimeStamp elements. The relevant test
 * cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-105: TimeStamp value</li>
 * </ul>
 * 
 * @see "OGC 12-007, 15.3: kml:TimeStamp"
 * @see "OGC 14-068, Conformance Level 1"
 */
public class TimeStampTests extends CommonFixture {

	/**
	 * Finds kml:TimeStamp elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods are skipped.
	 */
	@BeforeClass
	public void findTimeStampElements() {
		findTargetElements("TimeStamp");
	}

	/**
	 * [Test] Verifies that a kml:TimeStamp element has a child kml:when
	 * element.
	 * <p>
	 * Time stamps of varying precision (from fractional second to year) are
	 * permitted: xsd:dateTime, xsd:date, xsd:gYearMonth, and xsd:gYear. A time
	 * zone indicator is allowed.
	 * </p>
	 */
	@Test(description = "ATC-105")
	public void validTimeStamp() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element time = (Element) targetElements.item(i);
			Assert.assertTrue(time.getElementsByTagNameNS(KML2.NS_NAME, "when")
					.getLength() == 1, ErrorMessage.format(
					ErrorMessageKeys.EMPTY_TIMESTAMP,
					XMLUtils.buildXPointer(time)));
		}
	}
}
