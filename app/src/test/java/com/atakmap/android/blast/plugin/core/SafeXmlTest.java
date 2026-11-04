package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SafeXmlTest {

    @Test
    public void parsesPlainXmlAndRejectsDoctype() throws Exception {
        assertEquals("kml", SafeXml.parse("<kml><a/></kml>", false)
                .getDocumentElement().getNodeName());
        try {
            SafeXml.parse("<!DOCTYPE x [<!ENTITY e \"y\">]><x>&e;</x>", false);
            fail("DOCTYPE accepted");
        } catch (org.xml.sax.SAXException expected) {
            // DTDs are refused.
        }
    }
}
