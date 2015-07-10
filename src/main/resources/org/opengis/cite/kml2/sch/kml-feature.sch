<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-feature" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x Feature Constraints</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="FeaturePattern"/>
  </iso:phase>

  <iso:pattern id="FeaturePattern">
    <iso:p>Defines rules that apply to any KML feature</iso:p>
    <iso:rule context="kml:StyleMap" id="StyleMap" see="OGC-14-068r2.html#atc-135">
      <iso:assert test="exists(kml:Pair)">Expected at least one kml:Pair element in kml:StyleMap</iso:assert>
      <iso:assert test="every $p in kml:Pair satisfies $p[kml:key]">Expected kml:key element in every kml:Pair</iso:assert>
      <iso:assert test="every $p in kml:Pair satisfies $p[kml:styleUrl or kml:Style]">Expected kml:styleURL or kml:Style element in every kml:Pair</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
