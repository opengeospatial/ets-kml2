## OGC KML 2.x Conformance Test Suite

### Scope

This test suite verifies that a KML 2.x resource conforms to the following OGC 
specifications:

* _OGC KML, Version 2.3_ ([OGC 12-007r1](https://portal.opengeospatial.org/files/?artifact_id=62042&version=1)) 
__Note__: this pre-release version is only available to OGC members
* _OGC KML, Version 2.2_ ([OGC 07-147r2](http://portal.opengeospatial.org/files/?artifact_id=27810))

The KML specification defines three conformance levels indicating the relative 
importance or priority of a particular set of constraints; each level successively 
increases the test coverage. The highest level (CL3) indicates full conformance, 
but a given application or user community may opt for a lower level of conformance. 
All three levels will be implemented by the test suite:

1. CL1: includes tests covering requirements that must be satisfied by every 
(minimally) conforming KML document.
2. CL2: as for CL1, plus tests that address recommended constraints that should 
be satisfied; non-conformance at this level may hinder the utility, portability, 
or interoperability of the resource.
3. CL3: as for CL2, plus tests for optional constraints that are purely 
informative in nature.

Visit the [project documentation website](http://opengeospatial.github.io/ets-kml2/) 
for more information, including the API documentation.

### Special dependencies

The KML 2.3 specification adopted [XML Schema 1.1](http://www.w3.org/TR/xmlschema11-1/) 
as the normative schema language. The [Xerces2 Java](http://xerces.apache.org/xerces2-j/) 
parser aspires to support the current W3C Recommendation, but it seems to be 
mired in an interminable beta development cycle (the last release was November 
2010!). The test suite uses a component built from the 
[`xml-schema-1.1-dev`](http://svn.apache.org/viewvc/xerces/java/branches/xml-schema-1.1-dev/) 
branch (revision: 1667115). This artifact is available from the central Maven 
repository:

    <dependency>
      <groupId>org.opengis.cite.xerces</groupId> 
      <artifactId>xercesImpl-xsd11</artifactId> 
      <version>2.12-beta-r1667115</version> 
    </dependency>

In order to enable assertion checking using XML Schema 1.1 grammars, the XPath 
2.0 processor bundled with the Eclipse Web Tools Platform (WTP) is required. 
Unfortunately the Xerces schema processor does not use an official release of 
this component; instead, it uses a build based on the 
[`R3_2_maintenance`](http://git.eclipse.org/c/sourceediting/webtools.sourceediting.xpath.git/?h=R3_2_maintenance) 
branch. This special dependency has also been published to the central repository:

    <dependency> 
      <groupId>org.opengis.cite.eclipse.webtools.sse</groupId> 
      <artifactId>org.eclipse.wst.xml.xpath2.processor</artifactId> 
      <version>1.1.5-738bb7b85d</version> 
    </dependency>

__Note__: WTP 3.2 included v1.1.4 of the XPath 2.0 processor; WTP 3.3 included 
v2.0.0. Neither version will work with Xerces.


### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/ets-kml2/issues) such as a defect or 
an enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/ets-kml2/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add new tests: Fork the repository, implement (and verify) the tests on a new topic branch, 
and create a pull request
