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
(minimally) conforming KML document;
2. CL2: as for CL1, plus tests that address recommended constraints that 
should be satisfied;
3. CL3: as for CL2, plus tests for optional constraints that are purely 
informative in nature.

Visit the [project documentation website](http://opengeospatial.github.io/ets-kml2/) 
for more information, including the API documentation.

### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/ets-kml2/issues) such as a defect or 
an enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/ets-kml2/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add new tests: Fork the repository, implement (and verify) the tests on a new topic branch, 
and create a pull request
