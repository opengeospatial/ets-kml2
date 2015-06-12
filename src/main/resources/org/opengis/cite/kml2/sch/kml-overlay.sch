<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-overlay" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x Constraints</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="OverlayPattern"/>
  </iso:phase>

  <iso:pattern id="OverlayPattern">
    <iso:p>Defines rules that apply to overlay features</iso:p>
    <iso:rule context="kml:PhotoOverlay" id="PhotoOverlay" see="OGC-14-068r2.html#atc-119">
      <iso:assert test="kml:ViewVolume/kml:leftFov">Expected kml:leftFov element in kml:ViewVolume</iso:assert>
      <iso:assert test="kml:ViewVolume/kml:rightFov">Expected kml:rightFov element in kml:ViewVolume</iso:assert>
      <iso:assert test="kml:ViewVolume/kml:bottomFov">Expected kml:bottomFov element in kml:ViewVolume</iso:assert>
      <iso:assert test="kml:ViewVolume/kml:topFov">Expected kml:topFov element in kml:ViewVolume</iso:assert>
      <iso:assert test="number(kml:ViewVolume/kml:near) ge 0">Expected kml:ViewVolume/kml:near to be non-negative</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
