package org.datavaultplatform.common.metadata.impl;

import org.datavaultplatform.common.metadata.Dataset;
import org.datavaultplatform.common.metadata.Provider;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

public class PureProvider implements Provider {
    
    private String endpoint;
    
    public PureProvider(String endpoint) {
        this.endpoint = endpoint;
    }
    
    @Override
    public List<Dataset> getDatasetsForUser(String userID) {
        try {
            String response = query(endpoint);
            return parse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public Dataset getDataset(String id) {
        return null;
    }
    
    // Query the endpoint and retrieve the XML response
    // TODO: Support basic auth
    private String query(String url) throws Exception {
       
        URL queryURL = new URL(url);
        URLConnection conn = queryURL.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
       
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
       
        return sb.toString();
    }
    
    // Handle dataset XML namespace mapping
    static class PureNamespaceContent implements NamespaceContext {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals("dataset")) {
                return "http://atira.dk/schemas/pure4/wsdl/template/dataset/stable";
            } else if (prefix.equals("core")) {
                return "http://atira.dk/schemas/pure4/model/core/stable";
            } else if (prefix.equals("stab")) {
                return "http://atira.dk/schemas/pure4/model/template/dataset/stable";
            }
            return XMLConstants.NULL_NS_URI;
        }
        @Override
        public String getPrefix(String namespaceURI) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Iterator getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException();
        }
    }
    
    // Parse XML to build a list of Dataset objects
    private static List<Dataset> parse(String input) {
        
        ArrayList<Dataset> datasets = new ArrayList<>();
        
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
            Document dom = db.parse(bais);
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new PureNamespaceContent());
            XPathExpression contentExpr = xpath.compile("//core:content");
            XPathExpression titleExpr = xpath.compile("stab:title/core:localizedString");
            
            // Get a list of datasets
            NodeList contents = (NodeList)contentExpr.evaluate(dom, XPathConstants.NODESET);
            
            for (int i=0; i<contents.getLength(); i++) {

                Dataset dataset = new Dataset();
                
                // Get the uuid of this dataset
                Node content = (Node)contents.item(i);
                dataset.setID(content.getAttributes().getNamedItem("uuid").getTextContent());
                
                // Get the title of this dataset
                Node title = (Node)titleExpr.evaluate(content, XPathConstants.NODE);
                if (title != null) {
                    dataset.setName(title.getTextContent());
                }
                
                datasets.add(dataset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return datasets;
    }
}
