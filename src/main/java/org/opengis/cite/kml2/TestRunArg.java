package org.opengis.cite.kml2;

/**
 * An enumerated type defining all recognized test run arguments.
 */
public enum TestRunArg {

	/**
	 * An absolute URI that refers to a representation of a KML resource.
	 */
	KML,
	/**
	 * An integer value (1-3) indicating the level of conformance assessment.
	 */
	LVL;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
