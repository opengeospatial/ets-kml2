package org.opengis.cite.kml2;

/**
 * An enum type that specifies how the altitude components are interpreted in
 * kml:coordinates, kml:coord and kml:altitude elements. The allowed values are
 * declared by the schema component kml:AltitudeModeGroup; the default value is
 * "clampToGround".
 */
public enum AltitudeMode {
	CLAMP_TO_GROUND("clampToGround"), RELATIVE_TO_GROUND("relativeToGround"), ABSOLUTE(
			"absolute"), CLAMP_TO_SEAFLOOR("clampToSeaFloor"), RELATIVE_TO_SEAFLOOR(
			"relativeToSeaFloor");

	private final String enumValue;

	private AltitudeMode(String value) {
		this.enumValue = value;
	}

	/**
	 * Returns the AltitudeMode constant corresponding to the given string
	 * value.
	 * 
	 * @param value
	 *            A String specifying an altitude mode.
	 * @return The AltitudeMode constant corresponding to the given value, or
	 *         null if there is no match.
	 */
	public static AltitudeMode fromString(String value) {
		if (null == value || value.isEmpty()) {
			throw new IllegalArgumentException("No value supplied.");
		}
		AltitudeMode altitudeMode = null;
		for (AltitudeMode altMode : AltitudeMode.values()) {
			if (value.equals(altMode.enumValue)) {
				altitudeMode = altMode;
				break;
			}
		}
		return altitudeMode;
	}
}
