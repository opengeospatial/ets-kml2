package org.opengis.cite.kml2;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.util.ClientUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * A supporting base class that sets up a common test fixture. These
 * configuration methods are invoked before those defined in a subclass.
 */
public class CommonFixture {

	/** Root test suite package (absolute path). */
	public static final String ROOT_PKG_PATH = "/org/opengis/cite/kml2/";
	/** HTTP client component (JAX-RS Client API). */
	protected Client client;
	/** An HTTP request message. */
	protected ClientRequest request;
	/** An HTTP response message. */
	protected ClientResponse response;
	/** A DOM Document representing the main KML document. */
	protected Document kmlDoc;
	/** The elements to which the tests apply. */
	protected NodeList targetElements;

	/**
	 * Facilitates unit testing.
	 * 
	 * @param targetElements
	 *            The collection of elements to which the tests apply.
	 */
	public void setTargetElements(NodeList targetElements) {
		this.targetElements = targetElements;
	}

	/**
	 * Finds KML elements (of type kml:AbstractObjectType) by name. Only
	 * elements that occur outside of an update context are sought; that is, the
	 * element does not have a <code>targetId</code> attribute.
	 * 
	 * @param localName
	 *            The local name of some KML element.
	 * 
	 * @see "OGC KML 2.3, 13.5: kml:Update"
	 */
	protected void findTargetElements(String localName) {
		try {
			String xpath = String.format("//kml:%s[not(@targetId)]", localName);
			this.targetElements = XMLUtils.evaluateXPath(this.kmlDoc, xpath,
					null);
		} catch (XPathExpressionException xpe) {
			throw new AssertionError(xpe);
		}
		if (this.targetElements.getLength() == 0) {
			throw new SkipException(String.format(
					"No kml:%s elements found outside of update context.",
					localName));
		}
	}

	/**
	 * Initializes the common test fixture with a client component for
	 * interacting with HTTP endpoints.
	 *
	 * @param testContext
	 *            The test context that contains all the information for a test
	 *            run, including suite attributes.
	 */
	@BeforeClass
	public void initCommonFixture(ITestContext testContext) {
		Object obj = testContext.getSuite().getAttribute(
				SuiteAttribute.CLIENT.getName());
		if (null != obj) {
			this.client = Client.class.cast(obj);
		}
		obj = testContext.getSuite().getAttribute(
				SuiteAttribute.TEST_SUBJECT.getName());
		if (null == obj) {
			throw new SkipException("Test subject not found in ITestContext.");
		}
		this.kmlDoc = Document.class.cast(obj);
	}

	@BeforeMethod
	public void clearMessages() {
		this.request = null;
		this.response = null;
	}

	/**
	 * Obtains the (XML) response entity as a DOM Document. This convenience
	 * method wraps a static method call to facilitate unit testing (Mockito
	 * workaround).
	 *
	 * @param response
	 *            A representation of an HTTP response message.
	 * @param targetURI
	 *            The target URI from which the entity was retrieved (may be
	 *            null).
	 * @return A Document representing the entity.
	 *
	 * @see ClientUtils#getResponseEntityAsDocument(com.sun.jersey.api.client.ClientResponse,
	 *      java.lang.String)
	 */
	public Document getResponseEntityAsDocument(ClientResponse response,
			String targetURI) {
		return ClientUtils.getResponseEntityAsDocument(response, targetURI);
	}

	/**
	 * Builds an HTTP request message that uses the GET method. This convenience
	 * method wraps a static method call to facilitate unit testing (Mockito
	 * workaround).
	 *
	 * @param endpoint
	 *            A URI indicating the target resource.
	 * @param qryParams
	 *            A Map containing query parameters (may be null);
	 * @param mediaTypes
	 *            A list of acceptable media types; if not specified, generic
	 *            XML ("application/xml") is preferred.
	 * @return A ClientRequest object.
	 *
	 * @see ClientUtils#buildGetRequest(java.net.URI, java.util.Map,
	 *      javax.ws.rs.core.MediaType...)
	 */
	public ClientRequest buildGetRequest(URI endpoint,
			Map<String, String> qryParams, MediaType... mediaTypes) {
		return ClientUtils.buildGetRequest(endpoint, qryParams, mediaTypes);
	}

}
