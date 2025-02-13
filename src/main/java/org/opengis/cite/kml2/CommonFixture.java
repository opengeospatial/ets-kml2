package org.opengis.cite.kml2;

import java.util.Arrays;
import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;

import org.glassfish.jersey.client.ClientRequest;
import org.opengis.cite.kml2.util.ClientUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;

/**
 * A supporting base class that sets up a common test fixture. These configuration methods
 * are invoked before those defined in a subclass.
 */
public class CommonFixture {

	/** Root test suite package (absolute path). */
	public static final String ROOT_PKG_PATH = "/org/opengis/cite/kml2/";

	/** HTTP client component (JAX-RS Client API). */
	protected Client client;

	/** An HTTP request message. */
	protected ClientRequest request;

	/** An HTTP response message. */
	protected Response response;

	/** A DOM Document representing the main KML document. */
	protected Document kmlDoc;

	/** The elements to which the tests apply. */
	protected NodeList targetElements;

	/** The conformance level. */
	protected int conformanceLevel = 1;

	/**
	 * Facilitates unit testing.
	 * @param targetElements The collection of elements to which the tests apply.
	 */
	public void setTargetElements(NodeList targetElements) {
		this.targetElements = targetElements;
	}

	/**
	 * Finds KML elements (of type kml:AbstractObjectType) by (local) name. Only elements
	 * that occur outside of an update context are sought; that is, the element does not
	 * have a <code>targetId</code> attribute.
	 * @param localNames A list of KML element names.
	 * @see "OGC KML 2.3, 13.5: kml:Update"
	 */
	protected void findTargetElements(String... localNames) {
		StringBuilder xpath = new StringBuilder();
		String[] elemNames = localNames;
		for (int i = 0; i < elemNames.length; i++) {
			xpath.append(String.format("//kml:%s[not(@targetId)]", elemNames[i]));
			if (i < elemNames.length - 1) {
				xpath.append(" | ");
			}
		}
		try {
			this.targetElements = XMLUtils.evaluateXPath(this.kmlDoc, xpath.toString(), null);
		}
		catch (XPathExpressionException xpe) {
			throw new AssertionError(xpe);
		}
		if (this.targetElements.getLength() == 0) {
			throw new SkipException(
					String.format("No KML elements (%s) found outside of update context.", Arrays.toString(elemNames)));
		}
	}

	/**
	 * Initializes the common test fixture with a client component for interacting with
	 * HTTP endpoints, the KML document to be tested, and the effective conformance level.
	 * @param testContext The test context that contains all the information for a test
	 * run, including suite attributes.
	 */
	@BeforeClass
	public void initCommonFixture(ITestContext testContext) {
		Object obj = testContext.getSuite().getAttribute(SuiteAttribute.CLIENT.getName());
		if (null != obj) {
			this.client = Client.class.cast(obj);
		}
		obj = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJECT.getName());
		if (null == obj) {
			throw new SkipException("Test subject not found in ITestContext.");
		}
		this.kmlDoc = Document.class.cast(obj);
		obj = testContext.getSuite().getAttribute(SuiteAttribute.LEVEL.getName());
		if (null != obj) {
			this.conformanceLevel = Integer.class.cast(obj);
		}
	}

	/**
	 * <p>
	 * clearMessages.
	 * </p>
	 */
	@BeforeMethod
	public void clearMessages() {
		this.request = null;
		this.response = null;
	}

	/**
	 * Obtains the (XML) response entity as a DOM Document. This convenience method wraps
	 * a static method call to facilitate unit testing (Mockito workaround).
	 * @param response A representation of an HTTP response message.
	 * @param targetURI The target URI from which the entity was retrieved (may be null).
	 * @return A Document representing the entity.
	 * @see ClientUtils#getResponseEntityAsDocument(com.sun.jersey.api.client.ClientResponse,
	 * java.lang.String)
	 */
	public Document getResponseEntityAsDocument(Response response, String targetURI) {
		return ClientUtils.getResponseEntityAsDocument(response, targetURI);
	}

	/**
	 * <p>
	 * targetElementsProvider.
	 * </p>
	 * @return a {@link java.util.Iterator} object
	 */
	@DataProvider(name = "targetElementsProvider")
	protected Iterator<Object> targetElementsProvider() {
		return new TargetElementsIterator();
	}

	/**
	 * An Iterator suitable for use by a "lazy" data provider. It iterates over the target
	 * elements.
	 */
	protected class TargetElementsIterator implements Iterator<Object> {

		private Iterator<Node> nodeItr;

		/**
		 * 
		 */
		public TargetElementsIterator() {
			this.nodeItr = XMLUtils.asList(targetElements).iterator();
		}

		@Override
		public boolean hasNext() {
			return nodeItr.hasNext();
		}

		@Override
		public Object next() {
			return new Object[] { nodeItr.next() };
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
