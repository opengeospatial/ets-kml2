package org.opengis.cite.kml2.c2;

import java.net.URL;

import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Checks CL2 constraints that apply to a kml:Tour element.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class CL2TourTests extends CommonFixture {

	/**
	 * Finds kml:Tour elements do not appear in an update context. If none are
	 * found, all test methods defined in the class are skipped.
	 */
	@BeforeClass
	public void findTourElements() {
		findTargetElements("Tour");
	}

	/**
	 * [Test] Verifies that all Tour constraints that apply at CL2 are
	 * satisfied.
	 */
	@Test(description = "ATC-228,-229,-230")
	public void checkTourConstraints() {
		URL schRef = this.getClass().getResource(
				"/org/opengis/cite/kml2/sch/kml-tour.sch");
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element tour = (Element) targetElements.item(i);
			ETSAssert.assertSchematronValid(schRef, new DOMSource(tour), "CL2");
		}
	}
}
