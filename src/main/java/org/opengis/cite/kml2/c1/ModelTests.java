package org.opengis.cite.kml2.c1;

import javax.ws.rs.core.MediaType;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.JTSGeometryBuilder;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.LinkValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:Model elements. Outside of an update
 * context the following constraints apply:
 * <ul>
 * <li>contains a kml:Link element (specifies the location of a textured 3D
 * object resource)</li>
 * <li>contains a kml:Location element (specifies the position of the model's
 * origin)</li>
 * <li>if the target resource has texture files, contains a kml:ResourceMap
 * element that has a kml:Alias element for each texture file</li>
 * </ul>
 * 
 * The relevant test cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-131: Model orientation not empty</li>
 * <li>ATC-133: Model referents</li>
 * <li>ATC-214: Model location</li>
 * </ul>
 * 
 * @see "OGC 12-007r1: OGC KML 2.3, 10.9.2"
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class ModelTests extends CommonFixture {

	private LinkValidator linkValidator;

	public ModelTests() {
		this.linkValidator = new LinkValidator(MediaType.valueOf("model/*"));
	}

	/**
	 * Finds kml:Model elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findModelElements() {
		findTargetElements("Model");
	}

	/**
	 * [Test] Verifies that a kml:Model element has a valid location (that
	 * specifies the position of the model origin).
	 */
	@Test(description = "ATC-133, ATC-214")
	public void modelLocation() {
		JTSGeometryBuilder geomBuilder = new JTSGeometryBuilder();
		Polygon crsPolygon = geomBuilder.buildPolygon(new Envelope(-180, 180,
				-90, 90));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			NodeList location = model.getElementsByTagNameNS(KML2.NS_NAME,
					"Location");
			if (location.getLength() == 0) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:Location",
						XMLUtils.buildXPointer(model)));
			}
			Point jtsPoint = geomBuilder.buildPoint((Element) location.item(0));
			Assert.assertTrue(
					crsPolygon.covers(jtsPoint),
					ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
							jtsPoint.toText()));
		}
	}

	/**
	 * [Test] Verifies that a kml:Model element has a valid reference to a
	 * textured 3D object resource. The referent must exist, but the content is
	 * not prescribed by the KML specification. In practice this is usually a
	 * COLLADA file (model/vnd.collada+xml);
	 */
	@Test(description = "ATC-133")
	public void modelLink() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			NodeList link = model.getElementsByTagNameNS(KML2.NS_NAME, "Link");
			if (link.getLength() == 0) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:Link",
						XMLUtils.buildXPointer(model)));
			}
			Assert.assertTrue(linkValidator.isValid(link.item(0)),
					linkValidator.getErrorMessages());
		}
	}

}
