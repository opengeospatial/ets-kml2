<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:tns="http://www.opengis.net/cite/kml2"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:tec="java:com.occamlab.te.TECore"
  xmlns:tng="java:org.opengis.cite.kml2.TestNGController">

  <ctl:function name="tns:run-ets-kml2">
    <ctl:param name="testRunArgs">A Document node containing test run arguments (as XML properties).</ctl:param>
    <ctl:param name="outputDir">The directory in which the test results will be written.</ctl:param>
    <ctl:return>The test results as a Source object (root node).</ctl:return>
    <ctl:description>Runs the kml2 ${version} test suite.</ctl:description>
    <ctl:code>
      <xsl:variable name="controller" select="tng:new($outputDir)" />
      <xsl:copy-of select="tng:doTestRun($controller, $testRunArgs)" />
    </ctl:code>
  </ctl:function>

  <ctl:suite name="tns:ets-kml2-${version}">
    <ctl:title>OGC KML 2.x Conformance Test Suite</ctl:title>
    <ctl:description>Verifies that a KML 2.x resource conforms to the following OGC 
specifications: OGC KML 2.3 (OGC 12-007r1), OGC KML 2.2 (OGC 07-147r2).</ctl:description>
    <ctl:starting-test>tns:Main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="tns:Main">
    <ctl:assertion>The KML resource satisfies all applicable constraints.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="form-data">
        <ctl:form method="POST" width="800" height="600" xmlns="http://www.w3.org/1999/xhtml">
          <h2>OGC KML 2.x Conformance Test Suite</h2>
          <div style="background:#F0F8FF" bgcolor="#F0F8FF">
            <p>The KML resource is checked against the following specifications:</p>
            <ul>
              <li><em>OGC KML, Version 2.3</em> (<a target="_blank" type="application/zip"
    href="https://portal.opengeospatial.org/files/?artifact_id=62042&amp;version=1">OGC 12-007r1</a>)
                <div>
                  <strong>Note:</strong> This pre-release version is only available to OGC members.
                </div>
              </li>
              <li><em>OGC KML, Version 2.2</em> (<a target="_blank" type="application/pdf"
    href="http://portal.opengeospatial.org/files/?artifact_id=27810">OGC 07-147r2</a>)</li>
            </ul>
            <p>Three conformance levels are defined, where each level successively 
            increases the test coverage:</p>
            <dl>
              <dt><strong>CL1</strong></dt>
              <dd>Contains tests for mandatory constraints.</dd>
              <dt><strong>CL2</strong></dt>
              <dd>As for CL1, plus tests for recommended constraints.</dd>
              <dt><strong>CL3</strong></dt>
              <dd>As for CL2, plus tests for optional constraints.</dd>
            </dl>
          </div>
          <fieldset style="background:#ccffff">
            <legend style="font-family: sans-serif; color: #000099; 
			                 background-color:#F0F8FF; border-style: solid; 
                       border-width: medium; padding:4px">KML resource</legend>
            <p>
              <label for="uri">
                <h4 style="margin-bottom: 0.5em">Resource location (absolute http: or file: URI)</h4>
              </label>
              <input id="uri" name="uri" size="128" type="text" value="https://developers.google.com/kml/documentation/KML_Samples.kml" />
            </p>
            <p>
              <label for="doc">
                <h4 style="margin-bottom: 0.5em">Upload KML resource</h4>
              </label>
              <input name="doc" id="doc" size="128" type="file" />
            </p>
            <p>
              <strong>Note</strong>: If both a URI reference and a file are submitted 
              the URI is ignored.</p>
            <p>
              <label for="level">Conformance level: </label>
              <input id="CL1" type="radio" name="level" value="1" checked="checked" />
              <label for="CL1"> CL1 | </label>
              <input id="CL2" type="radio" name="level" value="2" />
              <label class="form-label" for="CL2"> CL2 | </label>
              <input id="CL3" type="radio" name="level" value="3" />
              <label class="form-label" for="CL3"> CL3</label>
            </p>
          </fieldset>
          <p>
            <input class="form-button" type="submit" value="Start"/> | 
            <input class="form-button" type="reset" value="Clear"/>
          </p>
        </ctl:form>
      </xsl:variable>
      <xsl:variable name="iut-file" select="$form-data//value[@key='doc']/ctl:file-entry/@full-path" />
      <xsl:variable name="test-run-props">
        <properties version="1.0">
          <entry key="kml">
            <xsl:choose>
              <xsl:when test="empty($iut-file)">
                <xsl:value-of select="normalize-space($form-data/values/value[@key='uri'])"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="concat('file:///', $iut-file)" />
              </xsl:otherwise>
            </xsl:choose>
          </entry>
          <entry key="lvl"><xsl:value-of select="$form-data/values/value[@key='level']"/></entry>
        </properties>
      </xsl:variable>
      <xsl:variable name="testRunDir">
        <xsl:value-of select="tec:getTestRunDirectory($te:core)"/>
      </xsl:variable>
      <xsl:variable name="test-results">
        <ctl:call-function name="tns:run-ets-kml2">
          <ctl:with-param name="testRunArgs" select="$test-run-props"/>
          <ctl:with-param name="outputDir" select="$testRunDir" />
        </ctl:call-function>
      </xsl:variable>
      <xsl:call-template name="tns:testng-report">
        <xsl:with-param name="results" select="$test-results" />
        <xsl:with-param name="outputDir" select="$testRunDir" />
      </xsl:call-template>
      <xsl:variable name="summary-xsl" select="tec:findXMLResource($te:core, '/testng-summary.xsl')" />
      <ctl:message>
        <xsl:value-of select="saxon:transform(saxon:compile-stylesheet($summary-xsl), $test-results)"/>
See detailed test report in the TE_BASE/users/<xsl:value-of 
select="concat(substring-after($testRunDir, 'users/'), '/html/')" /> directory.
      </ctl:message>
      <xsl:if test="xs:integer($test-results/testng-results/@failed) gt 0">
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and not(@is-config='true')]">
          <ctl:message>
Test method <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
          </ctl:message>
        </xsl:for-each>
        <ctl:fail/>
      </xsl:if>
      <xsl:if test="xs:integer($test-results/testng-results/@skipped) eq xs:integer($test-results/testng-results/@total)">
        <ctl:message>All tests were skipped. One or more preconditions were not satisfied.</ctl:message>
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and @is-config='true']">
          <ctl:message>
            <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
          </ctl:message>
        </xsl:for-each>
        <ctl:skipped />
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <xsl:template name="tns:testng-report">
    <xsl:param name="results" />
    <xsl:param name="outputDir" />
    <xsl:variable name="stylesheet" select="tec:findXMLResource($te:core, '/testng-report.xsl')" />
    <xsl:variable name="reporter" select="saxon:compile-stylesheet($stylesheet)" />
    <xsl:variable name="report-params" as="node()*">
      <xsl:element name="testNgXslt.outputDir">
        <xsl:value-of select="concat($outputDir, '/html')" />
      </xsl:element>
    </xsl:variable>
    <xsl:copy-of select="saxon:transform($reporter, $results, $report-params)" />
  </xsl:template>
</ctl:package>
