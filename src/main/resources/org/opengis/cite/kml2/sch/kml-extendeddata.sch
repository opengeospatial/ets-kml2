<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-extendeddata" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x ExtendedData Constraints (CL2)</iso:title>

  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="ExtendedDataPattern"/>
  </iso:phase>

  <iso:pattern id="ExtendedDataPattern">
    <iso:p>Defines rules that apply to custom data elements.</iso:p>
    <iso:rule context="kml:Data" id="Data" see="OGC-14-068r2.html#atc-208">
      <iso:assert test="string-length(@name) gt 0">Expected non-empty name attribute in kml:Data</iso:assert>
      <iso:assert test="string-length(kml:value) gt 0">Expected non-empty kml:value in kml:Data</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
