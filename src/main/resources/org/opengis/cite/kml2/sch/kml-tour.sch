<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="kml-overlay" 
  schemaVersion="2.3.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2"
  defaultPhase="MainPhase">

  <iso:title>OGC KML 2.x Tour Constraints</iso:title>

  <iso:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <iso:phase id="MainPhase">
    <iso:active pattern="TourPattern"/>
  </iso:phase>

  <iso:phase id="CL2">
    <iso:p>Patterns that apply at CL2 (or higher)</iso:p>
    <iso:active pattern="CL2-TourPattern"/>
  </iso:phase>

  <iso:pattern id="TourPattern">
    <iso:title>General rules for Tour features</iso:title>
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

  <iso:pattern id="CL2-TourPattern">
    <iso:title>Rules for Tour features at CL2</iso:title>
    <iso:rule context="kml:FlyTo" id="CL2.FlyTo" see="OGC-14-068r2.html#atc-228">
      <iso:assert test="xs:double(kml:duration) gt 0">Expected kml:duration &gt; 0</iso:assert>
      <iso:assert test="kml:flyToMode">Expected kml:flyToMode in kml:FlyTo</iso:assert>
    </iso:rule>
    <iso:rule context="kml:Wait" id="Wait" see="OGC-14-068r2.html#atc-229">
      <iso:assert test="xs:double(kml:duration) gt 0">Expected kml:duration &gt; 0</iso:assert>
    </iso:rule>
    <iso:rule context="kml:AnimatedUpdate" id="AnimatedUpdate" see="OGC-14-068r2.html#atc-230">
      <iso:assert test="xs:double(kml:duration) gt 0">Expected kml:duration &gt; 0</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.node.info" xml:lang="en">
    Node has [local name] = '<iso:value-of select="local-name()"/>' and [namespace name] = '<iso:value-of select="namespace-uri()"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
