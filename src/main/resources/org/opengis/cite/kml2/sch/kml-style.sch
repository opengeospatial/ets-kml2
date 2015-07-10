<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-style" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x Style Constraints</iso:title>

  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="StyleSelectorPattern"/>
  </iso:phase>

  <iso:pattern id="StyleSelectorPattern">
    <iso:p>Defines rules that apply to style definitions (CL2)</iso:p>
    <iso:rule context="kml:Style" id="Style">
      <iso:assert test="exists(kml:*)">ATC-224: Expected non-empty kml:Style element</iso:assert>
    </iso:rule>
    <iso:rule context="kml:PolyStyle" id="PolyStyle">
      <iso:assert test="kml:color or kml:colorMode or kml:fill or kml:outline">ATC-201: Expected PolyStyle with [color or colorMode or fill or outline]</iso:assert>
    </iso:rule>
    <iso:rule context="kml:ListStyle" id="ListStyle">
      <iso:assert test="kml:listItemType or kml:bgColor or kml:ItemIcon">ATC-223: Expected ListStyle with [listItemType or bgColor or ItemIcon]</iso:assert>
    </iso:rule>
    <iso:rule context="kml:LabelStyle" id="LabelStyle">
      <iso:assert test="kml:color or kml:colorMode or kml:scale">ATC-222: Expected LabelStyle with [color or colorMode or scale]</iso:assert>
    </iso:rule>
    <iso:rule context="kml:IconStyle" id="IconStyle">
      <iso:assert test="exists(kml:*)">ATC-220: Expected non-empty kml:IconStyle</iso:assert>
    </iso:rule>
    <iso:rule context="kml:BalloonStyle" id="BalloonStyle">
      <iso:assert test="exists(kml:*)">ATC-217: Expected non-empty BalloonStyle element</iso:assert>
    </iso:rule>
    <iso:rule context="kml:StyleMap" id="StyleMap">
      <iso:assert test="count(distinct-values(kml:Pair/kml:key)) = count(kml:Pair)">ATC-227: kml:Pair/kml:key values must be distinct</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
