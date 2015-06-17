<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-deprecated" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>Deprecated elements in OGC KML 2.x</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="DeprecatedPattern"/>
  </iso:phase>

  <iso:pattern id="DeprecatedPattern">
    <iso:p>Deprecated elements in KML features</iso:p>
    <iso:rule context="/" id="Deprecated" see="OGC-14-068r2.html#level-3">
      <iso:report test="kml:Metadata">kml:Metadata is deprecated. Use kml:ExtendedData instead.</iso:report>
      <iso:report test="kml:Snippet">kml:Snippet is deprecated. Use kml:snippet instead.</iso:report>
    </iso:rule>
    <iso:rule context="kml:BalloonStyle" id="BalloonStyle" see="OGC-14-068r2.html#atc-303">
      <iso:report test="kml:color">kml:color is deprecated in kml:BalloonStyle. Use kml:bgColor instead.</iso:report>
    </iso:rule>
    <iso:rule context="kml:NetworkLink" id="NetworkLink" see="OGC-14-068r2.html#atc-309">
      <iso:report test="kml:Url">kml:Url is deprecated in kml:NetworkLink. Use kml:Link instead.</iso:report>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
