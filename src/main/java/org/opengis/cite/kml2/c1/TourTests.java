package org.opengis.cite.kml2.c1;

import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml2.CommonFeatureTests;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.UpdateValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements tests that apply to the kml:Tour element.
 * 
 * @see "OGC 12-007r2: OGC KML 2.3, 9.2.3"
 * @see "OGC 14-068r2: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class TourTests extends CommonFeatureTests {

	private UpdateValidator updateValidator;

	public TourTests() {
		this.updateValidator = new UpdateValidator();
	}

	/**
	 * Finds kml:Tour elements in the KML resource that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findTourElements() {
		findTargetElements("Tour");
	}

	/**
	 * [Test] Checks various Schematron constraints that apply to Tour features:
	 * <ul>
	 * <li>ATC-141: Tour playlist is not empty</li>
	 * <li>ATC-142: FlyTo view</li>
	 * <li>ATC-143: TourControl not empty</li>
	 * </ul>
	 */
	@Test(description = "ATC-141, ATC-142, ATC-143")
	public void checkTourConstraints() {
		URL schRef = this.getClass().getResource(
				"/org/opengis/cite/kml2/sch/kml-tour.sch");
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element tour = (Element) targetElements.item(i);
			ETSAssert.assertSchematronValid(schRef, new DOMSource(tour),
					"MainPhase");
		}
	}

	/**
	 * [Test] Verifies that the kml:SoundCue elements in a tour refer to an
	 * audio resource (of media type <code>audio/*</code>). Some commonly used
	 * audio formats are listed below.
	 * 
	 * <ul>
	 * <li>MP3 (audio/mpeg)</li>
	 * <li>MP4 (audio/mp4)</li>
	 * </ul>
	 */
	@Test(description = "ATC-144")
	public void soundCue() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element tour = (Element) targetElements.item(i);
			NodeList soundCues = tour.getElementsByTagNameNS(KML2.NS_NAME,
					"SoundCue");
			for (int j = 0; j < soundCues.getLength(); j++) {
				Element soundCue = (Element) soundCues.item(j);
				Node href = soundCue.getElementsByTagNameNS(KML2.NS_NAME,
						"href").item(0);
				Assert.assertNotNull(href, ErrorMessage.format(
						ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:href",
						XMLUtils.buildXPointer(soundCue)));
				URI uri = URI.create(href.getTextContent().trim());
				if (!uri.isAbsolute()) {
					uri = uri.resolve(tour.getOwnerDocument().getBaseURI());
				}
				ETSAssert.assertReferentExists(uri,
						MediaType.valueOf("audio/*"));
			}
		}
	}

	/**
	 * [Test] Verifies that a kml:AnimatedUpdate element satisfies all
	 * applicable constraints. Specifically, the update targets must exist
	 * (possibly in the same document).
	 */
	@Test(description = "ATC-145")
	public void animatedUpdateTarget() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element tour = (Element) targetElements.item(i);
			NodeList updates = tour.getElementsByTagNameNS(KML2.NS_NAME,
					"Update");
			for (int j = 0; j < updates.getLength(); j++) {
				Assert.assertTrue(updateValidator.isValid(updates.item(j)),
						updateValidator.getErrorMessages());
			}
		}
	}
}
