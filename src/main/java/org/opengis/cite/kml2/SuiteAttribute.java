package org.opengis.cite.kml2;

import java.util.Set;

import javax.xml.validation.Schema;

import com.sun.jersey.api.client.Client;

import org.w3c.dom.Document;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a
 * shared test fixture.
 */
@SuppressWarnings("rawtypes")
public enum SuiteAttribute {

	/**
	 * A client component for interacting with HTTP endpoints.
	 */
	CLIENT("httpClient", Client.class),
	/**
	 * An immutable Schema object representing the complete KML 2.2 schema.
	 */
	KML22_SCHEMA("kml22Schema", Schema.class),
	/**
	 * An immutable Schema object for KML 2.3 (XML Schema 1.1 grammmar).
	 */
	KML23_SCHEMA("kml23Schema", Schema.class),
	/**
	 * The version of the KML document.
	 */
	KML_VERSION("kmlVersion", String.class),
	/**
	 * A DOM Document representation of the test subject or metadata about it.
	 */
	TEST_SUBJECT("testSubject", Document.class),
	/**
	 * An integer (1-3) indicating the level of conformance assessment.
	 */
	LEVEL("level", Integer.class),
	/**
	 * A {@code Set<String>} containing shared style identifiers.
	 */
	SHARED_STYLES("sharedStyles", Set.class),
	/**
	 * A {@code Set<String>} containing custom schema (kml:Schema) identifiers.
	 */
	CUSTOM_SCHEMAS("customSchemas", Set.class);

	private final Class attrType;
	private final String attrName;

	private SuiteAttribute(String attrName, Class attrType) {
		this.attrName = attrName;
		this.attrType = attrType;
	}

	public Class getType() {
		return attrType;
	}

	public String getName() {
		return attrName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(attrName);
		sb.append('(').append(attrType.getName()).append(')');
		return sb.toString();
	}
}
