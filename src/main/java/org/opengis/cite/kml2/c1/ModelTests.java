package org.opengis.cite.kml2.c1;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.JTSGeometryBuilder;
import org.opengis.cite.kml2.util.URIUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.LinkValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
 * <li>ATC-129: Valid texture alias</li>
 * <li>ATC-131: Model orientation not empty</li>
 * <li>ATC-133: Model referents</li>
 * <li>ATC-214: Model location</li>
 * </ul>
 * 
 * @see "OGC 12-007r1: OGC KML 2.3, 10.9.2"
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class ModelTests extends CommonFixture {

	private LinkValidator modelLinkValidator;

	public ModelTests() {
		this.modelLinkValidator = new LinkValidator(
				MediaType.valueOf("model/*"));
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
			Assert.assertTrue(modelLinkValidator.isValid(link.item(0)),
					modelLinkValidator.getErrorMessages());
		}
	}

	/**
	 * [Test] Verifies that a kml:Orientation element contains at least one of
	 * the following elements: kml:heading, kml:tilt, or kml:roll.
	 */
	@Test(description = "ATC-131")
	public void orientation() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			Element orientation = (Element) model.getElementsByTagNameNS(
					KML2.NS_NAME, "Orientation").item(0);
			if (null == orientation) {
				continue;
			}
			Assert.assertTrue(
					orientation.getElementsByTagNameNS(KML2.NS_NAME, "*")
							.getLength() > 0, ErrorMessage.format(
							ErrorMessageKeys.CONSTRAINT_VIOLATION,
							"Model orientation is empty.",
							XMLUtils.buildXPointer(orientation)));
		}
	}

	/**
	 * [Test] Verifies that the kml:Alias elements appearing in a
	 * kml:ResourceMap element satisfy all of the following constraints:
	 * <ol>
	 * <li>the value of the child kml:targetHref element is a URI reference to
	 * an image (texture) resource;</li>
	 * <li>the value of the child kml:sourceHref element corresponds to a file
	 * reference that appears within the 3D object resource referenced by the
	 * sibling kml:Link element</li>
	 * </ol>
	 * 
	 * Note: The source file is expected to be a textual (including XML) digital
	 * asset resource such as a COLLADA file.
	 */
	@Test(description = "ATC-129")
	public void resourceMap() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			Element map = (Element) model.getElementsByTagNameNS(KML2.NS_NAME,
					"ResourceMap").item(0);
			if (null == map) {
				continue;
			}
			NodeList aliases = map
					.getElementsByTagNameNS(KML2.NS_NAME, "Alias");
			Node modelRef = null;
			try {
				modelRef = XMLUtils.evaluateXPath(model, "kml:Link/kml:href",
						null).item(0);
			} catch (XPathExpressionException e) {
			}
			URI modelURI = URI.create(modelRef.getTextContent().trim());
			if (!modelURI.isAbsolute()) {
				modelURI = URIUtils.resolveRelativeURI(model.getOwnerDocument()
						.getBaseURI(), modelURI.toString());
			}
			for (int j = 0; j < aliases.getLength(); j++) {
				assertValidAlias((Element) aliases.item(j), modelURI);
			}
		}
	}

	/**
	 * Asserts that the given kml:Alias element satisfies all applicable
	 * constraints.
	 * 
	 * @param alias
	 *            A kml:Alias element.
	 * @param modelURI
	 *            An absolute URI that refers to a 3D model (e.g. a COLLADA
	 *            file); this will be used to resolve relative source URIs if
	 *            necessary.
	 */
	void assertValidAlias(Element alias, URI modelURI) {
		Element sourceHrefElem = (Element) alias.getElementsByTagNameNS(
				KML2.NS_NAME, "sourceHref").item(0);
		Assert.assertNotNull(sourceHrefElem, ErrorMessage.format(
				ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:sourceHref",
				XMLUtils.buildXPointer(alias)));
		String sourceHref = sourceHrefElem.getTextContent().trim();
		URI sourceURI = URI.create(sourceHref);
		if (!sourceURI.isAbsolute()) {
			// resolve against URI of referring model resource
			sourceURI = modelURI.resolve(sourceURI);
		}
		ETSAssert.assertReferentExists(sourceURI, MediaType.valueOf("image/*"));
		// Verify sourceHref occurs in model content?
		Element targetHref = (Element) alias.getElementsByTagNameNS(
				KML2.NS_NAME, "targetHref").item(0);
		Assert.assertNotNull(targetHref, ErrorMessage.format(
				ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:targetHref",
				XMLUtils.buildXPointer(alias)));
		URI targetURI = URI.create(targetHref.getTextContent().trim());
		if (!targetURI.isAbsolute()) {
			// resolve against base URI of KML doc
			targetURI = URIUtils.resolveRelativeURI(alias.getOwnerDocument()
					.getBaseURI(), targetURI.toString());
		}
		ETSAssert.assertReferentExists(targetURI, MediaType.valueOf("image/*"));
	}
}
