<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-feature" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2"
  defaultPhase="MainPhase">

  <iso:title>OGC KML 2.x Feature Constraints</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="FeaturePattern"/>
  </iso:phase>
  
  <iso:phase id="CL2">
    <iso:p>Patterns that apply at CL2 (or higher)</iso:p>
    <iso:active pattern="FeaturePattern"/>
    <iso:active pattern="CL2-FeaturePattern"/>
  </iso:phase>

  <iso:pattern id="FeaturePattern">
    <iso:title>General rules that apply to any KML feature</iso:title>
    <iso:rule context="kml:StyleMap" id="StyleMap" see="OGC-14-068r2.html#atc-135">
      <iso:assert test="exists(kml:Pair)">Expected at least one kml:Pair element in kml:StyleMap</iso:assert>
      <iso:assert test="every $p in kml:Pair satisfies $p[kml:key]">Expected kml:key element in every kml:Pair</iso:assert>
      <iso:assert test="every $p in kml:Pair satisfies $p[kml:styleUrl or kml:Style]">Expected kml:styleURL or kml:Style element in every kml:Pair</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="CL2-FeaturePattern">
    <iso:title>Rules for KML features at CL2</iso:title>
    <iso:rule context="atom:link" id="atomLink" see="OGC-14-068r2.html#atc-232">
      <iso:assert test="@rel eq 'related'">Expected atom:link with @rel = 'related'</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
