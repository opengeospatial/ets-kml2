<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-update" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x Constraints - Update</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="UpdatePattern"/>
  </iso:phase>

  <iso:pattern id="UpdatePattern">
    <iso:p>Defines rules that apply to update contexts</iso:p>
    <iso:rule abstract="true" id="UpdateRule" see="OGC-14-068r2.html#atc-123">
      <iso:assert test="@targetId" diagnostics="msg.node.info">KML object must have @targetId in update context.</iso:assert>
      <iso:report test="@id" diagnostics="msg.node.info">KML object must not have @id in update context.</iso:report>
    </iso:rule>
    <iso:rule context="kml:Create/kml:*">
      <iso:extends rule="UpdateRule"/>
    </iso:rule>
    <iso:rule context="kml:Delete/kml:*">
      <iso:extends rule="UpdateRule"/>
    </iso:rule>
    <iso:rule context="kml:Change/kml:*">
      <iso:extends rule="UpdateRule"/>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
