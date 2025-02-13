package org.opengis.cite.kml2.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jakarta.ws.rs.core.MediaType;

/**
 * Verifies the behavior of the HttpClientUtils class.
 */
public class VerifyHttpClientUtils {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void acceptableXmlMediaType() {
		MediaType xmlType = MediaType.APPLICATION_XML_TYPE;
		boolean result = ClientUtils.contentIsAcceptable(
				"application/atom+xml", xmlType);
		assertTrue(result);
	}

	@Test
	public void generalXmlMediaTypeIsUnacceptable() {
		MediaType atomMediaType = MediaType.APPLICATION_ATOM_XML_TYPE;
		// general type is not substitutable for specific subtype
		boolean result = ClientUtils.contentIsAcceptable("application/xml",
				atomMediaType);
		assertFalse("Expected invalid Link.", result);
	}

}
