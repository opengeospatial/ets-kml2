package org.opengis.cite.kml2.c1;

import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.CoordinatesValidator;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Node;

/**
 * Implements ATC-103: Valid geometry coordinates.
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class ATC103 extends CommonFixture {

	private CoordinatesValidator validator;

	public ATC103() {
		this.validator = new CoordinatesValidator();
	}

	/**
	 * Finds target elements (kml:coordinates) in the KML document. If none are
	 * found, all tests are skipped.
	 */
	@BeforeClass
	public void findTargetElements() {
		try {
			this.targetElements = XMLUtils.evaluateXPath(this.kmlDoc,
					"//kml:coordinates", null);
		} catch (XPathExpressionException xpe) {
			throw new AssertionError(xpe);
		}
		if (this.targetElements.getLength() == 0) {
			throw new SkipException("No kml:coordinates elements found.");
		}
	}

	/**
	 * [Test] Verify that a kml:coordinates element contains a list of white
	 * space-separated 2D or 3D tuples that contain comma-separated decimal
	 * values (lon,lat[,hgt]). The relevant schema components are shown below.
	 * 
	 * <pre>
	 * {@literal
	 * <xsd:element name="coordinates" type="kml:coordinatesType"/>
	 * <xsd:simpleType name="coordinatesType">
	 *   <xsd:list itemType="string"/>
	 * </xsd:simpleType>
	 * }
	 * </pre>
	 */
	@Test(description = "ATC-103")
	public void validCoordinates() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Node coordinates = targetElements.item(i);
			Assert.assertTrue(validator.isValid(coordinates),
					validator.getErrors());
		}
	}

}
