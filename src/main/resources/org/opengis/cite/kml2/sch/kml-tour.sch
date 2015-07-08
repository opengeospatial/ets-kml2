<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-overlay" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>OGC KML 2.x Tour Constraints</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="TourPattern"/>
  </iso:phase>

  <iso:pattern id="TourPattern">
    <iso:p>Defines rules that apply to Tour features</iso:p>
    <iso:rule context="kml:Tour" id="Tour" see="OGC-14-068r2.html#atc-141">
      <iso:assert test="kml:Playlist/*">Expected non-empty Playlist in Tour</iso:assert>
    </iso:rule>
    <iso:rule context="kml:Playlist/kml:FlyTo" id="FlyTo" see="OGC-14-068r2.html#atc-142">
      <iso:assert test="kml:Camera or kml:LookAt">Expected kml:Camera or kml:LookAt in kml:FlyTo element</iso:assert>
    </iso:rule>
    <iso:rule context="kml:Playlist/kml:TourControl" id="TourControl" see="OGC-14-068r2.html#atc-143">
      <iso:assert test="kml:playMode">Expected kml:playMode in kml:TourControl element</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
