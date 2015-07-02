package org.opengis.cite.kml2;

/**
 * Defines keys used to access localized messages for assertion errors. The
 * messages are stored in Properties files that are encoded in ISO-8859-1
 * (Latin-1). For some languages the {@code native2ascii} tool must be used to
 * process the files and produce escaped Unicode characters.
 */
public class ErrorMessageKeys {

	public static final String NOT_SCHEMA_VALID = "NotSchemaValid";
	public static final String EMPTY_STRING = "EmptyString";
	public static final String XPATH_RESULT = "XPathResult";
	public static final String NAMESPACE_NAME = "NamespaceName";
	public static final String LOCAL_NAME = "LocalName";
	public static final String UNEXPECTED_QNAME = "UnexpectedQName";
	public static final String XML_ERROR = "XMLError";
	public static final String XPATH_ERROR = "XPathError";
	public static final String MISSING_INFOSET_ITEM = "MissingInfosetItem";
	public static final String UNEXPECTED_STATUS = "UnexpectedStatus";
	public static final String UNEXPECTED_MEDIA_TYPE = "UnexpectedMediaType";
	public static final String UNACCEPTABLE_MEDIA_TYPE = "UnacceptableMediaType";
	public static final String MISSING_ENTITY = "MissingEntity";
	public static final String UNSUPPORTED_VERSION = "UnsupportedVersion";
	public static final String POINT_COORDS = "PointCoords";
	public static final String LINE_COORDS = "LineCoords";
	public static final String RING_COORDS = "RingCoords";
	public static final String QUAD_COORDS = "QuadCoords";
	public static final String MISSING_COORDS = "MissingCoords";
	public static final String OPEN_RING = "OpenRing";
	public static final String COORD_DIM = "CoordDim";
	public static final String NAN = "NaN";
	public static final String OUTSIDE_CRS = "OutsideCRS";
	public static final String POLYGON_BOUNDARY = "PolygonBoundary";
	public static final String QUAD_NOT_CONVEX = "QuadNotConvex";
	public static final String RING_NOT_CCW = "RingNotCCW";
	public static final String URI_NOT_ACCESSIBLE = "URINotAccessible";
	public static final String NOT_FOUND = "NotFound";
	public static final String EMPTY_TIMESPAN = "EmptyTimeSpan";
	public static final String TIMESPAN_INTERVAL = "TimeSpanInterval";
	public static final String EMPTY_TIMESTAMP = "EmptyTimeStamp";
	public static final String SHARED_STYLE_NOT_FOUND = "SharedStyleNotFound";
	public static final String CONSTRAINT_VIOLATION = "ConstraintViolation";
	public static final String NO_CONTENT = "NoContent";
	public static final String EXT_BOUNDARY_ORIENT = "ExternalBoundaryOrientation";
	public static final String RING_NOT_SIMPLE = "SimpleRing";
	public static final String UOM_NOT_DEFN = "UnitOfMeasureNotDefined";
	public static final String INVALID_DATATYPE = "InvalidDatatype";
	public static final String DUPLICATE_DATA = "DuplicateData";
}
