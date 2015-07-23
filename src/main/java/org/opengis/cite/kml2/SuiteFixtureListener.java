package org.opengis.cite.kml2;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.validation.Schema;

import org.opengis.cite.kml2.util.HttpClientUtils;
import org.opengis.cite.kml2.util.KMLUtils;
import org.opengis.cite.kml2.util.TestSuiteLogger;
import org.opengis.cite.kml2.util.URIUtils;
import org.opengis.cite.kml2.util.ValidationUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

/**
 * A listener that performs various tasks before and after a test suite is run,
 * usually concerned with maintaining a shared test suite fixture. Since this
 * listener is loaded using the ServiceLoader mechanism, its methods will be
 * called before those of other suite listeners listed in the test suite
 * definition and before any annotated configuration methods.
 *
 * Attributes set on an ISuite instance are not inherited by constituent test
 * group contexts (ITestContext). However, suite attributes are still accessible
 * from lower contexts.
 *
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		processSuiteParameters(suite);
		buildKMLSchemas(suite);
		registerHttpClient(suite);
	}

	/**
	 * Builds immutable {@link Schema Schema} objects suitable for validating
	 * the content of a KML 2.x document. The schemas are added to the suite
	 * fixture as the value of the attributes identified in the following table.
	 *
	 * <table border="1" style="border-collapse: collapse;">
	 * <caption>Application schemas</caption>
	 * <thead>
	 * <tr>
	 * <th>SuiteAttribute</th>
	 * <th>Schema</th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <td><code>SuiteAttribute.KML23_SCHEMA</code></td>
	 * <td>ogckml23_xsd11.xsd (OGC KML 2.3, OGC 12-007)</td>
	 * </tr>
	 * <tr>
	 * <td><code>SuiteAttribute.KML22_SCHEMA</code></td>
	 * <td>ogckml22.xsd (OGC KML 2.2, OGC 07-147)</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @param suite
	 *            The test suite to be run.
	 * 
	 * @see <a target="_blank" href="http://schemas.opengis.net/kml/">OGC schema
	 *      repository</a>
	 */
	void buildKMLSchemas(ISuite suite) {
		Schema kml22Schema = ValidationUtils.createKMLSchema("2.2");
		if (null != kml22Schema) {
			suite.setAttribute(SuiteAttribute.KML22_SCHEMA.getName(),
					kml22Schema);
		}
		Schema kml23Schema = ValidationUtils.createKMLSchema("2.3");
		if (null != kml23Schema) {
			suite.setAttribute(SuiteAttribute.KML23_SCHEMA.getName(),
					kml23Schema);
		}
	}

	@Override
	public void onFinish(ISuite suite) {
	}

	/**
	 * Processes test suite arguments and sets suite attributes accordingly. The
	 * entity referenced by the {@link TestRunArg#KML kml} argument is parsed
	 * (or unpacked first if it's a KMZ resource) and the resulting Document is
	 * set as the value of the "testSubject" attribute. The level of conformance
	 * assessment is determined by the value of the {@link TestRunArg#LVL lvl}
	 * argument (default value: 1).
	 * 
	 * @param suite
	 *            An ISuite object representing a TestNG test suite.
	 */
	void processSuiteParameters(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		TestSuiteLogger.log(Level.CONFIG,
				"Suite parameters\n" + params.toString());
		Integer level = new Integer(1);
		if (null != params.get(TestRunArg.LVL.toString())) {
			try {
				int lvlParam = Integer.parseInt(params.get(TestRunArg.LVL
						.toString()));
				if (lvlParam > 0 && lvlParam < 4) {
					level = Integer.valueOf(lvlParam);
				}
			} catch (NumberFormatException nfe) { // use default value instead
			}
		}
		suite.setAttribute(SuiteAttribute.LEVEL.getName(), level);
		String kmlParam = params.get(TestRunArg.KML.toString());
		if ((null == kmlParam) || kmlParam.isEmpty()) {
			throw new IllegalArgumentException(
					"Required test run parameter not found: "
							+ TestRunArg.KML.toString());
		}
		URI iutRef = URI.create(kmlParam.trim());
		File entityFile = null;
		try {
			entityFile = URIUtils.dereferenceURI(iutRef);
		} catch (IOException iox) {
			throw new RuntimeException(
					"Failed to dereference resource located at " + iutRef, iox);
		}
		Document kmlDoc;
		try {
			kmlDoc = KMLUtils.parseKMLDocument(entityFile);
		} catch (IOException | SAXException x) {
			throw new RuntimeException("Failed to parse KML resource at "
					+ iutRef, x);
		}
		suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), kmlDoc);
		String kmlVersion = kmlDoc.getDocumentElement().getAttribute(
				KML2.VER_ATTR);
		if (kmlVersion.isEmpty()) {
			kmlVersion = "2.2";
		}
		suite.setAttribute(SuiteAttribute.KML_VERSION.getName(), kmlVersion);
		if (TestSuiteLogger.isLoggable(Level.FINE)) {
			StringBuilder logMsg = new StringBuilder(
					"Parsed KML resource retrieved from ");
			logMsg.append(iutRef).append("\n");
			logMsg.append(XMLUtils.writeNodeToString(kmlDoc));
			TestSuiteLogger.log(Level.FINE, logMsg.toString());
		}
	}

	/**
	 * An JAX-RS Client component is added to the suite fixture as the value of
	 * the {@link SuiteAttribute#CLIENT} attribute; it may be subsequently
	 * accessed via the {@link org.testng.ITestContext#getSuite()} method.
	 *
	 * @param suite
	 *            The test suite instance.
	 */
	void registerHttpClient(ISuite suite) {
		Client client = HttpClientUtils.buildClient();
		if (null != client) {
			suite.setAttribute(SuiteAttribute.CLIENT.getName(), client);
		}
	}
}
