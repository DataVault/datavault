package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DataCreator;
import org.datavaultplatform.common.model.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.List;

public class VaultsPureService {
    private static final Logger logger = LoggerFactory.getLogger(VaultsPureService.class);

    public Boolean hasProducedPureRecord(Vault vault) {
        Boolean retVal = false;
        if (vault.getSnapshot() != null && ! vault.getSnapshot().isEmpty()) {
            retVal = true;
        }
        return retVal;
    }

    public void produceXml(Vault vault) throws ParserConfigurationException, TransformerException {
        // Build the Pure XML record for the Vault
        // 1.Create a DocumentBuilder instance
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setExpandEntityReferences(false);
        DocumentBuilder dbuilder = dbFactory.newDocumentBuilder();

        // 2. Create a Document from the above DocumentBuilder.
        Document document = dbuilder.newDocument();

        // 3. Create the elements you want using the Element class and its appendChild method.

        // root element
        // <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        // <v1:datasets xmlns:v1="v1.dataset.pure.atira.dk" xmlns:v3="v3.commons.pure.atira.dk">
        // <v1:dataset id="datavault-1" type="dataset">
        // <v1.title xmlns="v1.dataset.pure.atira.dk">Test Title 2022</v1.title>
        // /v1:dataset>
        // </v1:datasets>
        //Element datasets = document.createElementNS("v1.dataset.pure.atira.dk", "v1:datasets");
        //datasets.setAttribute("xmlns:v1", "v1.dataset.pure.atira.dk");
        //datasets.setAttribute("xmlns:v3", "v3.commons.pure.atira.dk");
        //document.appendChild(datasets);
        //<?xml version="1.0" encoding="UTF-8" standalone="no"?>
        // <v1:datasets xmlns:v1="v1.dataset.pure.atira.dk" xmlns:v3="v3.commons.pure.atira.dk">
        // <v1:dataset id="datavault-1" type="dataset">
        // <v1:title>Test Title 2022</v1:title>
        // <v1:additionalDescriptions>
        // <v1:description type="abstract">Data supporting FOREST CANOPY NITROGEN UPTAKE CAN SUPPLY ENTIRE FOLIAR DEMAND (Ferarretto et al. 2022, Functional Ecology)
        // Contact person: Richard K F Nair, rnair@bgc-jena.mpg.de
        // These datasets contain ecohydrology N fluxes for a set of experiments investigating N processing by forest canopies at Griffin Forest, Scotland
        // These data were previously used in the PhD thesis Ferarretto 2020, &amp;apos;Nitrogen fluxes in forests from atmospheric deposition to soil: use of water flux monitoring and stable isotopes to close gaps in nitrogen transfers&amp;apos;: https://era.ed.ac.uk/handle/1842/38004
        // Data collection was funded by Elizabeth Sinclair Irvine Bequest and Centenary Agroforestry 89 Fund of the School of GeoSciences (University of Edinburgh), Forest Research UK and the UK Natural Environment Research Council (NERC) through grant NE/G00725X/1 and a Life Sciences Mass Spectrometry Facility (LSMSF) award.
        // This dataset consists of a readme file and three files corresponding to the three experiments described in the paper.
        // </v1:description>
        // <v1:description type="reason_for_dataset_access_restriction_and_conditions_for_release">Ferraretto, Daniele; Nair, Richard. (2022). Forest Canopy Nitrogen Uptake experiments data, 2012-2017 [dataset]. University of Edinburgh. School of GeoSciences. https://doi.org/10.7488/ds/3272.
        // </v1:description>
        // </v1:additionalDescriptions>
        // </v1:dataset>
        // </v1:datasets>


        Element datasets = this.addDatasetsTag(document);
        Element dataset = this.addDatasetTag(document);
        datasets.appendChild(dataset);
        Element title = this.addTitleTag(document, vault.getName());
        dataset.appendChild(title);
        Element descriptions = this.addDescriptionTags(document, vault.getDescription());
        dataset.appendChild(descriptions);
        Element publisher = this.addPublisher(document, "Edinburgh-Datavault-Publisher-uuid");
        dataset.appendChild(publisher);
        Element visibility = this.addVisibility(document, "Public");
        dataset.appendChild(visibility);
        if (vault.getDataCreators() != null && !vault.getDataCreators().isEmpty()) {
            Element persons = this.addPersons(document, vault.getDataCreators());
            dataset.appendChild(persons);
        }

        // write content into xml file

        // 4. Create a new Transformer instance and a new DOMSource instance.
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        //DocumentType docType = document.getImplementation().createDocumentType("docType", "-//Oberon//YOUR PUBLIC DOCTYPE//EN", "test");
        //transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
        //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "");
        //transformer.setOutputProperty("entities", "[apos  \"'\"]");
        //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "test");
        document.setXmlStandalone(true);
        DOMSource source = new DOMSource(document);
        // 5. Create a new StreamResult to the output stream you want to use.
        //StreamResult result = new StreamResult(new File("/Users/admin/Desktop/users.xml"));
        StreamResult result = new StreamResult(System.out); // to print on console

        // 6. Use transform method to write the DOM object to the output stream.
        transformer.transform(source, result);

        System.out.println("File created successfully");
    }

    private Element addDatasetsTag(Document document) {
        Element datasets = document.createElementNS("v1.dataset.pure.atira.dk", "v1:datasets");
        datasets.setAttribute("xmlns:v1", "v1.dataset.pure.atira.dk");
        datasets.setAttribute("xmlns:v3", "v3.commons.pure.atira.dk");

        document.appendChild(datasets);

        return datasets;
    }

    private Element addDatasetTag(Document document) {
        Element dataset = document.createElementNS("v1.dataset.pure.atira.dk", "v1:dataset");

        // Attribute of child element
        dataset.setAttribute("id", "datavault-1");
        dataset.setAttribute("type", "dataset");

        return dataset;
    }

    private Element addTitleTag(Document document, String title) {
        Element titleElement = document.createElementNS("v1.dataset.pure.atira.dk", "v1:title");
        titleElement.appendChild(document.createTextNode("Datavault: " + title));
        return titleElement;
    }

    private Element addDescriptionTags(Document document, String description) {
        Element descriptions = document.createElementNS("v1.dataset.pure.atira.dk", "v1:additionalDescriptions");

        Element descriptionAbstract = document.createElementNS("v1.dataset.pure.atira.dk", "v1:description");
        descriptionAbstract.setAttribute("type", "abstract");
        descriptionAbstract.appendChild(document.createTextNode(description));
        descriptions.appendChild(descriptionAbstract);

        //Element descriptionReasonFor = document.createElementNS("v1.dataset.pure.atira.dk", "v1:description");
        //descriptionReasonFor.setAttribute("type", "reason_for_dataset_access_restriction_and_conditions_for_release");
        //descriptionReasonFor.appendChild(document.createTextNode("Ferraretto, Daniele; Nair, Richard. (2022). Forest Canopy Nitrogen Uptake experiments data, 2012-2017 [dataset]. University of Edinburgh. School of GeoSciences. https://doi.org/10.7488/ds/3272."));
        //descriptions.appendChild(descriptionReasonFor);

        return descriptions;
    }

    private Element addPersons(Document document, List<DataCreator> dataCreators) {
        Element persons = document.createElementNS("v1.dataset.pure.atira.dk", "v1:persons");

        for (DataCreator dc : dataCreators) {
            Element person = document.createElementNS("v1.dataset.pure.atira.dk", "v1:person");
            person.setAttribute("id", "get_id_from_pure");
            person.setAttribute("contactPerson", "get_value_from_dv?");
            Element subPerson = document.createElementNS("v1.dataset.pure.atira.dk", "v1:person");
            subPerson.setAttribute("lookupId", "get_id_from_pure");
            Element organisations = document.createElementNS("v1.dataset.pure.atira.dk", "v1:organisations");
            Element organisation = document.createElementNS("v1.dataset.pure.atira.dk", "v1:organisation");
            organisation.setAttribute("lookupId", "get_id_from_pure");
            organisations.appendChild(organisation);
            Element role = document.createElementNS("v1.dataset.pure.atira.dk", "v1:role");
            role.appendChild(document.createTextNode("creator"));
            Element startDate = document.createElementNS("v1.dataset.pure.atira.dk", "v1:associationStartDate");
            startDate.appendChild(document.createTextNode("get_from_pure?"));

            subPerson.appendChild(organisations);
            subPerson.appendChild(role);
            subPerson.appendChild(startDate);
            person.appendChild(subPerson);
            persons.appendChild(person);
        }

        return persons;
    }

    private Element addPublisher(Document document, String publisher_uuid) {
        Element publisherElement = document.createElementNS("v1.dataset.pure.atira.dk", "v1:publisher");
        publisherElement.setAttribute("lookupId", publisher_uuid);
        return publisherElement;
    }

    private Element addVisibility(Document document, String visibility) {
        Element visibilityElement = document.createElementNS("v1.dataset.pure.atira.dk", "v1:visibility");
        visibilityElement.appendChild(document.createTextNode(visibility));
        return visibilityElement;
    }

}
