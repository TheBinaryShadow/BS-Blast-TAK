package com.atakmap.android.blast.plugin.core;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/** Test-only XML parser with DTDs and external entities disabled. */
final class SafeXml {

    private SafeXml() {
    }

    static Document parse(String xml, boolean namespaces) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
                true);
        f.setFeature("http://xml.org/sax/features/external-general-entities",
                false);
        f.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);
        f.setNamespaceAware(namespaces);
        return f.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }
}
