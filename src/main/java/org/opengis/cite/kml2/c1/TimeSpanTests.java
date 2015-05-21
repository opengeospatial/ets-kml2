package org.opengis.cite.kml2.c1;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationError;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements tests that apply to kml:TimeSpan elements. The relevant test cases
 * from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-104: TimeSpan interval</li>
 * </ul>
 * 
 * @see "OGC 12-007, 15.2: kml:TimeSpan"
 * @see "OGC 14-068, Conformance Level 1"
 */
public class TimeSpanTests extends CommonFixture {

	DateTimeFormatter timeFormatter;

	public TimeSpanTests() {
		this.timeFormatter = ISODateTimeFormat.dateTimeParser();
	}

	/**
	 * Finds kml:TimeSpan elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findTimeSpanElements() {
		findTargetElements("TimeSpan");
	}

	/**
	 * [Test] Verifies that a kml:TimeSpan element includes at least one child
	 * element (kml:begin or kml:end). If it is a definite interval (both
	 * kml:begin and kml:end are present), then the end value must be later than
	 * the begin value.
	 * 
	 * <p>
	 * Temporal values of varying precision (from fractional second to year) are
	 * permitted: xsd:dateTime, xsd:date, xsd:gYearMonth, and xsd:gYear. A time
	 * zone indicator is allowed.
	 * </p>
	 */
	@Test(description = "ATC-104")
	public void validTimeSpan() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element span = (Element) targetElements.item(i);
			NodeList nodes = null;
			try {
				nodes = XMLUtils.evaluateXPath(span, "kml:begin | kml:end",
						null);
				Assert.assertFalse(nodes.getLength() == 0, ErrorMessage.format(
						ErrorMessageKeys.EMPTY_TIMESPAN,
						XMLUtils.buildXPointer(span)));
			} catch (XPathExpressionException e) { // won't happen
			}
			if ((nodes.getLength() == 2) && !isValidDefiniteTimeInterval(nodes)) {
				ValidationError err = new ValidationError(ErrorSeverity.ERROR,
						ErrorMessage.get(ErrorMessageKeys.TIMESPAN_INTERVAL),
						null, -1, -1, XMLUtils.buildXPointer(span));
				throw new AssertionError(err);
			}
		}
	}

	/**
	 * Checks that the given time instants specify a valid temporal interval.
	 * 
	 * @param timeInstants
	 *            A pair of nodes containing the beginning and ending instants
	 *            of the interval (kml:begin, kml:end).
	 * @return true if kml:end is later than kml:begin; false otherwise.
	 */
	boolean isValidDefiniteTimeInterval(NodeList timeInstants) {
		Map<String, DateTime> instants = new HashMap<>();
		for (int i = 0; i < timeInstants.getLength(); i++) {
			Node instant = timeInstants.item(i);
			try {
				DateTime dateTime = timeFormatter.parseDateTime(instant
						.getTextContent().trim());
				instants.put(instant.getLocalName(), dateTime);
			} catch (IllegalArgumentException x) {
				instants.put(instant.getLocalName(), null);
			}
		}
		return instants.get("end").isAfter(instants.get("begin"));
	}
}
