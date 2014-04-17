package com.praetoriandroid.cameraremote;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DeviceDescription {

    public static final String CAMERA = "camera";
    public static final String GUIDE = "guide";

    private Map<String, String> services;

    public static class Fetcher {

        private HttpClient httpClient = new HttpClient();

        public DeviceDescription fetch(String url) throws IOException, ParseException {
            InputStream dataStream = null;
            try {
                dataStream = httpClient.get(url);
                Map<String, String> services = parse(dataStream);
                dataStream.close();
                return new DeviceDescription(services);
            } finally {
                if (dataStream != null) {
                    dataStream.close();
                }
            }
        }

        public Fetcher setConnectionTimeout(int timeout) {
            httpClient.setConnectionTimeout(timeout);
            return this;
        }

        private Map<String, String> parse(InputStream dataStream) throws IOException, ParseException {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(dataStream);

                Map<String, String> services = new HashMap<String, String>();

            /*
             * This is simplified device description parsing: we assume that the XML document contains the only
             * <device> element with the only <av:X_ScalarWebAPI_DeviceInfo> child element with the only
             * <av:X_ScalarWebAPI_ServiceList> element where all <av:X_ScalarWebAPI_Service> are located.
             */
                NodeList serviceList = document.getElementsByTagName("av:X_ScalarWebAPI_Service");
                for (int i = 0; i < serviceList.getLength(); i++) {
                    Element service = (Element) serviceList.item(i);
                    Element serviceTypeElement = getTheOnlyChild(service, "av:X_ScalarWebAPI_ServiceType");
                    String serviceType = getElementContent(serviceTypeElement);
                    Element actionListUrlElement = getTheOnlyChild(service, "av:X_ScalarWebAPI_ActionList_URL");
                    String actionListUrl = getElementContent(actionListUrlElement);
                    services.put(serviceType, actionListUrl);
                }
                return services;
            } catch (ParserConfigurationException e) {
                throw new ParseException(e);
            } catch (SAXException e) {
                throw new InvalidDataFormatException(e);
            }
        }

        private Element getTheOnlyChild(Element parent, String elementName) throws InvalidDataFormatException {
            NodeList children = parent.getElementsByTagName(elementName);
            if (children.getLength() != 1) {
                throw new InvalidDataFormatException("Element <" + parent.getTagName() + "> should contain the only child <"
                        + elementName + "> element");
            }
            return (Element) children.item(0);
        }

        private String getElementContent(Element element) throws InvalidDataFormatException {
            Node child = element.getFirstChild();
            if (child.getNodeType() != Node.TEXT_NODE) {
                throw new InvalidDataFormatException("Element <" + element.getTagName()
                        + "> should contain the only text child node");
            }
            return child.getNodeValue();
        }

//    private void dumpElement(Element element, int padding) {
//        System.out.print(getPadding(padding));
//        System.out.println('<' + element.getTagName() + '>');
//        NodeList children = element.getChildNodes();
//        for (int i = 0; i < children.getLength(); i++) {
//            Node child = children.item(i);
//            switch (child.getNodeType()) {
//                case Node.ELEMENT_NODE:
//                    dumpElement((Element) child, padding + 2);
//                    break;
//                case Node.TEXT_NODE:
//                    String text = child.getNodeValue();
//                    if (!text.trim().isEmpty()) {
//                        System.out.print(getPadding(padding + 2));
//                        System.out.println(child.getNodeValue());
//                    }
//                    break;
//            }
//        }
//        System.out.print(getPadding(padding));
//        System.out.println("</" + element.getTagName() + '>');
//    }
//
//    private String getPadding(int size) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < size; i++) {
//            sb.append(' ');
//        }
//        return sb.toString();
//    }

    }

    private DeviceDescription(Map<String, String> services) throws IOException {
        this.services = services;
    }

    public String getServiceUrl(String serviceType) throws ServiceNotSupportedException {
        String url = services.get(serviceType);
        if (url == null) {
            throw new ServiceNotSupportedException(serviceType);
        }
        return url + '/' + serviceType;
    }

    @Override
    public String toString() {
        return services.toString();
    }

}
