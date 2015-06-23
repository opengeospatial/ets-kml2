package org.opengis.cite.kml2;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.cite.kml2.util.URIUtils;
import org.opengis.cite.kml2.validation.ExtendedDataValidator;
import org.opengis.cite.kml2.validation.RegionValidator;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Checks constraints that apply to any KML feature (an element that substitutes
 * directly or indirectly for kml:AbstractFeatureGroup).
 * <ul>
 * <li>ATC-106</li>
 * <li>ATC-108</li>
 * <li>ATC-111</li>
 * </ul>
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 1"
 */
public class CommonFeatureTests extends CommonFixture {

	private Set<String> sharedStyles;
	private RegionValidator regionValidator;

	public CommonFeatureTests() {
		this.regionValidator = new RegionValidator();
	}

	/**
	 * Get the identifiers of all shared styles in the instance document.
	 * 
	 * @param testContext
	 *            The test context.
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public void getSharedStyles(ITestContext testContext) {
		Object obj = testContext.getSuite().getAttribute(
				SuiteAttribute.SHARED_STYLES.getName());
		if (null != obj) {
			this.sharedStyles = (Set<String>) obj;
		}
	}

	/**
	 * [Test] Checks that a kml:styleUrl element satisfies all of the following
	 * constraints:
	 * <ol>
	 * <li>its value is a valid relative or absolute URL that refers to a shared
	 * style definition (any element that substitutes for
	 * kml:AbstractStyleSelectorGroup)</li>
	 * <li>if the reference is an absolute URI, it conforms to the 'http' or
	 * 'file' URI schemes</li>
	 * <li>it includes a fragment identifier that conforms to the shorthand
	 * pointer syntax as defined in the W3C XPointer framework</li>
	 * </ol>
	 */
	@Test(description = "ATC-106")
	public void validStyleReference() {
		if (null == this.targetElements) {
			return;
		}
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			NodeList styleUrlElem = kmlFeature.getElementsByTagNameNS(
					KML2.NS_NAME, "styleUrl");
			if (styleUrlElem.getLength() == 0) {
				continue;
			}
			URI styleUrl = URI.create(styleUrlElem.item(0).getTextContent()
					.trim());
			if (styleUrl.getPath().isEmpty()) {
				// same-document style reference
				String id = styleUrl.getFragment();
				Assert.assertTrue(this.sharedStyles.contains(id), ErrorMessage
						.format(ErrorMessageKeys.SHARED_STYLE_NOT_FOUND, id));
				continue;
			}
			styleUrl = URIUtils.resolveRelativeURI(
					this.kmlDoc.getDocumentURI(), styleUrl.toString());
			Node referent;
			try {
				referent = URIUtils.parseURI(styleUrl);
			} catch (SAXException | IOException e) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.XML_ERROR, styleUrl));
			}
			Assert.assertNotNull(referent, ErrorMessage.format(
					ErrorMessageKeys.SHARED_STYLE_NOT_FOUND, styleUrl));
			ETSAssert.assertQualifiedName(referent, new QName[] {
					new QName(KML2.NS_NAME, "Style"),
					new QName(KML2.NS_NAME, "StyleMap") });
		}
	}

	/**
	 * [Test] Checks that the content of a kml:Region element satisfies all
	 * applicable constraints.
	 * 
	 * @see RegionValidator
	 */
	@Test(description = "ATC-108, ATC-111")
	public void validRegion() {
		if (null == this.targetElements) {
			return;
		}
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			NodeList region = kmlFeature.getElementsByTagNameNS(KML2.NS_NAME,
					"Region");
			if (region.getLength() == 0) {
				continue;
			}
			Assert.assertTrue(
					regionValidator.validateRegionExtent(region.item(0)),
					regionValidator.getErrors());
		}

	}

	/**
	 * [Test] Checks that the content of a kml:ExtendedData element satisfies
	 * all applicable constraints.
	 * 
	 * @see RegionValidator
	 */
	@Test(description = "ATC-ijk")
	public void validExtendedData() {
		if (null == this.targetElements) {
			return;
		}
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			Node extData = kmlFeature.getElementsByTagNameNS(KML2.NS_NAME,
					"ExtendedData").item(0);
			if (null == extData) {
				continue;
			}
			ExtendedDataValidator validator = new ExtendedDataValidator();
			Assert.assertTrue(validator.isValid(extData),
					validator.getErrorMessages());
		}
	}

}
