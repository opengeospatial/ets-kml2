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

  <iso:pattern id="ViewPointPattern">
    <iso:p>Defines rules that apply to kml:Camera and kml:LookAt elements</iso:p>
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
