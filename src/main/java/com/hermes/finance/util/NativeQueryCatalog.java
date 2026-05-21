package com.hermes.finance.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class NativeQueryCatalog {

    private final Map<String, String> queries = new HashMap<>();

    public NativeQueryCatalog() {
        loadQueries();
    }

    public String get(String name) {
        String query = queries.get(name);
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query not found in named-native-query.xml: " + name);
        }
        return query;
    }

    private void loadQueries() {
        try {
            ClassPathResource resource = new ClassPathResource("named-native-query.xml");
            try (InputStream inputStream = resource.getInputStream()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

                Document document = factory.newDocumentBuilder().parse(inputStream);
                NodeList nodeList = document.getElementsByTagName("query");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element queryElement = (Element) nodeList.item(i);
                    String name = queryElement.getAttribute("name");
                    String sql = queryElement.getTextContent();
                    if (name != null && !name.isBlank() && sql != null && !sql.isBlank()) {
                        queries.put(name, sql.trim());
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load named-native-query.xml", ex);
        }
    }
}
