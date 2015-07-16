package org.opengis.cite.kml2;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.util.URIUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.ExtendedDataValidator;
import org.opengis.cite.kml2.validation.RegionValidator;
import org.opengis.cite.kml2.validation.ViewpointValidator;
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
	private ViewpointValidator viewValidator;

	public CommonFeatureTests() {
		this.regionValidator = new RegionValidator(this.conformanceLevel);
		this.viewValidator = new ViewpointValidator();
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
			Assert.assertTrue(regionValidator.isValid(region.item(0)),
					regionValidator.getErrorMessages());
		}

	}

	/**
	 * [Test] Checks that the content of a kml:Camera or kml:LookAt element
	 * satisfies all applicable constraints.
	 * 
	 * @see ViewpointValidator
	 */
	@Test(description = "ATC-213")
	public void validViewpoint() {
		if (null == this.targetElements || this.conformanceLevel == 1) {
			return;
		}
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			NodeList view = null;
			try {
				view = XMLUtils.evaluateXPath(kmlFeature,
						"kml:Camera | kml:LookAt", null);
			} catch (XPathExpressionException e) {
			}
			if (view.getLength() > 0) {
				Assert.assertTrue(viewValidator.isValid(view.item(0)),
						viewValidator.getErrorMessages());
			}
		}

	}

	/**
	 * [Test] Checks that the content of a kml:ExtendedData element satisfies
	 * all applicable constraints.
	 * 
	 * @see ExtendedDataValidator
	 */
	@Test(description = "ATC-127")
	public void validExtendedData() {
		if (null == this.targetElements) {
			return;
		}
		ExtendedDataValidator validator = new ExtendedDataValidator();
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			Node extData = kmlFeature.getElementsByTagNameNS(KML2.NS_NAME,
					"ExtendedData").item(0);
			if (null == extData) {
				continue;
			}
			Assert.assertTrue(validator.isValid(extData),
					validator.getErrorMessages());
		}
	}

	/**
	 * [Test] Checks that the value of the kml:phoneNumber element is a valid
	 * 'tel' URI that complies with RFC 3966, <em>The tel URI for Telephone
	 * Numbers</em>.
	 * 
	 * @see <a href="http://tools.ietf.org/html/rfc3966" target="_blank">RFC
	 *      3966</a>
	 */
	@Test(description = "ATC-124")
	public void phoneNumber() {
		if (null == this.targetElements) {
			return;
		}
		Pattern phoneNumPattern = Pattern
				.compile("tel:(\\+)?(\\d*([-.()])?)+(\\d{3}?[-.()])?(\\d{3}[-.()])?(\\d{4,10})?([;].*)?");
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			Node phoneNum = kmlFeature.getElementsByTagNameNS(KML2.NS_NAME,
					"phoneNumber").item(0);
			if (null == phoneNum) {
				continue;
			}
			Matcher matcher = phoneNumPattern
					.matcher(phoneNum.getTextContent());
			Assert.assertTrue(matcher.matches(), ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"Valid 'tel' URI (RFC 3966)",
					XMLUtils.buildXPointer(phoneNum)));
		}
	}

	/**
	 * [Test] Checks that an atom:author element satisfies all of the following
	 * assertions:
	 * <ol>
	 * <li>the content of the child atom:uri element is an IRI reference;</li>
	 * <li>the content of the child atom:email element conforms to the
	 * "addr-spec" production rule in RFC 5322 (<em>Internet Message Format</em>
	 * ).</li>
	 * </ol>
	 * 
	 * <p>
	 * The content model is specified by the <code>atomPersonConstruct</code>
	 * pattern in the RELAX NG schema. Note that within a container element
	 * authorship is inherited by all child feature members; it may be
	 * overridden on a per-feature basis.
	 * </p>
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc4287#section-3.2"
	 *      target="_blank">RFC 4287 - Person Constructs</a>
	 * @see <a href="http://tools.ietf.org/html/rfc5322#section-3.4.1"
	 *      target="_blank">RFC 5322 - Addr-Spec Specification</a>
	 */
	@Test(description = "ATC-130")
	public void atomAuthor() {
		if (null == this.targetElements) {
			return;
		}
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			try {
				Node uri = XMLUtils.evaluateXPath(kmlFeature,
						"atom:author/atom:uri", null).item(0);
				if (null != uri) {
					ETSAssert.assertValidIRI(uri.getTextContent().trim());
				}
				Node email = XMLUtils.evaluateXPath(kmlFeature,
						"atom:author/atom:email", null).item(0);
				if (null != email) {
					ETSAssert.assertValidEmailAddress(email.getTextContent()
							.trim());
				}
			} catch (XPathExpressionException e) {
			}
		}
	}

	/**
	 * [Test] Checks various Schematron constraints that apply to any KML
	 * feature:
	 * <ul>
	 * <li>ATC-135: Mode-specific feature style (StyleMap)</li>
	 * <li>ATC-232: Feature metadata - atom:link</li>
	 * </ul>
	 */
	@Test(description = "ATC-135, ATC-232")
	public void checkFeatureConstraints() {
		if (null == this.targetElements) {
			return;
		}
		URL schRef = this.getClass().getResource(
				"/org/opengis/cite/kml2/sch/kml-feature.sch");
		String activePhase = (this.conformanceLevel > 1) ? "CL2" : "MainPhase";
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element kmlFeature = (Element) targetElements.item(i);
			ETSAssert.assertSchematronValid(schRef, new DOMSource(kmlFeature),
					activePhase);
		}
	}

}
