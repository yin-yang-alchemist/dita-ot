<!-- (C) Copyright Shawn McKenzie, 2007. All Rights Reserved.
  *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="*[contains(@class, ' map/topicref ')]">
    <xsl:param name="parent"/>

    <xsl:variable name="apos">'</xsl:variable>
    <xsl:variable name="jsapos">\'</xsl:variable>
    <xsl:variable name="comma">,</xsl:variable>
    <xsl:variable name="empty_string" select="''"/>
    <xsl:variable name="self" select="generate-id()"/>

    <xsl:if test="not(@toc='no')">
      <xsl:if test="@href">
        <!-- Send an error to the commandline if the navtitle is empty -->

        <xsl:if test="@navtitle = $empty_string">
          <xsl:message>WARNING: Topic <xsl:value-of select="@href"/>, has an empty
          navtitle!</xsl:message>
        </xsl:if>

        <xsl:text>var </xsl:text>
        <xsl:value-of select="concat('obj', $self)"/>
        <xsl:text> = { label: "</xsl:text>

        <!-- I think this next if/navtitle bit is wrong. I think there will always be a navtitle -->
        <xsl:if test="@navtitle">
          <xsl:value-of select="@navtitle"/>
        </xsl:if>

        <xsl:if test="not(@navtitle)">
          <xsl:value-of select="parent::*[@navtitle]"/>
        </xsl:if>



        <xsl:text>", href:"</xsl:text>
        <xsl:call-template name="gethref">
          <xsl:with-param name="ditahref" select="@href"/>
        </xsl:call-template>
        <xsl:text>", target:"contentwin" };
    </xsl:text>

        <xsl:text>var </xsl:text>
        <xsl:value-of select="$self"/>
        <xsl:text> = new YAHOO.widget.TextNode(</xsl:text>
        <xsl:value-of select="concat('obj', $self)"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$parent"/>
        <xsl:text>, false);</xsl:text>

        <xsl:apply-templates>
          <xsl:with-param name="parent" select="$self"/>
        </xsl:apply-templates>
      </xsl:if>

      <!-- this next bit deals with topicref that have no href (like a topichead) -->

      <xsl:if test="not(@href)">
        <!-- if there is no navtitle, it is a topicgroup used only for links, no TOC impact -->
        <xsl:if test="not(@navtitle)">
          <xsl:apply-templates>
            <xsl:with-param name="parent" select="$parent"/>
          </xsl:apply-templates>
        </xsl:if>

        <xsl:if test="@navtitle">
          <xsl:text>var </xsl:text>
          <xsl:value-of select="$self"/>
          <xsl:text> = new YAHOO.widget.TextNode("</xsl:text>
          <xsl:value-of select="@navtitle"/>
          <xsl:text>", </xsl:text>
          <xsl:value-of select="$parent"/>
          <xsl:text>, false);</xsl:text>

          <xsl:apply-templates>
            <xsl:with-param name="parent" select="$self"/>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:if>

      <!-- Need to deal with topicgroup elements that are used for navigation purposes -->
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
