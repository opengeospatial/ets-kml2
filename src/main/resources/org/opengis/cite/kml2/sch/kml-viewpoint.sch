<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-viewpoint" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x Viewpoint Constraints</iso:title>

  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="ViewPointPattern"/>
  </iso:phase>
  <iso:phase id="CL2">
    <iso:p>Patterns that apply at CL2 (or higher)</iso:p>
    <iso:active pattern="ViewPointPattern"/>
    <iso:active pattern="CL2-ViewPointPattern"/>
  </iso:phase>

  <iso:pattern id="ViewPointPattern">
    <iso:title>General rules for kml:Camera and kml:LookAt elements</iso:title>
    <iso:rule context="kml:LookAt" id="LookAt" see="OGC-14-068r2.html#atc-137">
      <iso:assert test="kml:latitude">Expected kml:latitude in LookAt</iso:assert>
      <iso:assert test="kml:longitude">Expected kml:longitude in LookAt</iso:assert>
      <iso:assert test="kml:range">Expected kml:range in LookAt</iso:assert>
      <iso:assert test="not(kml:tilt) or (xs:double(kml:tilt) ge 0 and xs:double(kml:tilt) le 90)">Expected kml:tilt in range 0-90</iso:assert>
      <iso:assert test="not(kml:altitudeMode) or (kml:altitudeMode eq 'clampToGround') or kml:altitude">Expected kml:altitude if altitudeMode != 'clampToGround'</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="CL2-ViewPointPattern">
    <iso:title>Rules for viewpoints at CL2</iso:title>
    <iso:rule context="kml:Camera" id="Camera" see="OGC-14-068r2.html#atc-213">
      <iso:assert test="kml:latitude">Expected kml:latitude in Camera</iso:assert>
      <iso:assert test="kml:longitude">Expected kml:longitude in Camera</iso:assert>
      <iso:assert test="kml:altitude">Expected kml:altitude in Camera</iso:assert>
      <iso:assert test="kml:altitudeMode and (kml:altitudeMode ne 'clampToGround')">Expected altitudeMode ne 'clampToGround'</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
