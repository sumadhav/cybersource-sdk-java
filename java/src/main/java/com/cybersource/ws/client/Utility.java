/*
 * Copyright 2003-2014 CyberSource Corporation
 *
 * THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
 * AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
 * DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
 * COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
 * EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
 * WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
 * DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
 * OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
 * ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
 * DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
 * DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
 * WITH ANY PARTICULAR OPERATING SYSTEM.
 */

package com.cybersource.ws.client;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Containing useful constants and methods.
 */
public class Utility {
    private Utility() {
    }

    /**
     * Version number of this release.
     */
    public static final String VERSION = "6.2.13";
    public static final String ORIGIN_TIMESTAMP = "v-c-client-iat";
    public static final String SDK_ELAPSED_TIMESTAMP = "v-c-client-computetime";
    public static final String RESPONSE_TIME_REPLY = "v-c-response-time";
    public static final String MERCHANT_TRANSACTION_IDENTIFIER = "merchantTransactionIdentifier";

    public static final String AUTH_SERVICE = "ccAuthService";
    public static final String AUTH_SERVICE_XML_RUN_ATT = "run";
    public static final String AUTH_SERVICE_NVP = "ccAuthService_run";
    public static final String ELEM_MERCHANT_ID = "merchantID";
    public static final String KEY_ALIAS = "keyAlias";
    public static final String ELEM_MERCHANT_REFERENCE_CODE = "merchantReferenceCode";
    public static final String ELEM_CLIENT_LIBRARY = "clientLibrary";
    public static final String ELEM_CLIENT_LIBRARY_VERSION = "clientLibraryVersion";
    public static final String ELEM_CLIENT_ENVIRONMENT = "clientEnvironment";
    /**
     * HTTP Status-Code 400: Bad Request.
     */
    public static final int HTTP_BAD_REQUEST = 400;
    public static final String MTI_FIELD_ERR_MSG = "merchantTransactionIdentifier field is mandatory if useHttpClientWithConnectionPool is set to true";

    /**
     * If in the Request map, a key called "_has_escapes" is present and is set
     * to "1", we will not escape the XML special characters.  Basically, the
     * merchant is saying that they have escaped the characters themselves.
     * This might prove useful for more advanced users of the Basic client.
     */
    private static final String HAS_ESCAPES = "_has_escapes";

    /**
     * NVP library information.
     */
    public static final String NVP_LIBRARY = new StringBuilder().append("Java NVP").append("/")
            .append(VERSION).toString();

    /**
     * XML library information.
     */
    public static final String XML_LIBRARY = new StringBuilder().append("Java XML").append("/")
            .append(VERSION).toString();

    /**
     * Environment information.
     */
    public static final String ENVIRONMENT
            = new StringBuilder().append(System.getProperty("os.name")).append("/")
            .append(System.getProperty("os.version"))
            .append("/").append(System.getProperty("java.vendor"))
            .append("/").append(System.getProperty("java.version"))
            .toString();


    /**
     * Returns the string representation of the given Node object.  Used for
     * logging or demo purposes only.  As it employs some formatting
     * parameters, parsing the string it returns may not result to a Node
     * object exactly similar to the one passed to it.
     *
     * @param node the Node object whose string representation is wanted.
     * @param type either PCI.REQUEST or PCI.REPLY.  Used for masking.
     * @return the string representation of the given Node object.
     */
    public static String nodeToString(Node node, int type) {
        node = node.cloneNode(true);
        maskXml(type, node, null);
        return nodeToString(node);
    }

    /**
     * Returns the string representation of the given Node object.  Used for
     * logging or demo purposes only.  As it employs some formatting
     * parameters, parsing the string it returns may not result to a Node
     * object exactly similar to the one passed to it.
     *
     * @param node the Node object whose string representation is wanted.
     * @return the string representation of the given Node object.
     */
    public static String nodeToString(Node node) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            transformer.transform(
                    new DOMSource(node), new StreamResult(stream));

            String str = stream.toString("UTF-8");
            stream.close();

            return (str);
        } catch (TransformerConfigurationException e) {
            return (e.getMessage());
        } catch (TransformerException e) {
            return (e.getMessage());
        } catch (UnsupportedEncodingException e) {
            return (e.getMessage());
        } catch (IOException e) {
            return (e.getMessage());
        }
    }

    /**
     * Reads the properties from a file.  If no filename was specified in the
     * command-line, it will look for "cybs.properties" in the current
     * directory.
     *
     * @param commandLineArgs the command-line arguments.
     * @return Properties object containing the run-time properties required by
     * the clients.
     */
    public static Properties readProperties(String[] commandLineArgs) {
        Properties props = new Properties();

        try {
            String filename = (commandLineArgs.length > 0)
                    ? commandLineArgs[0] : "cybs.properties";

            FileInputStream fis = new FileInputStream(filename);
            props.load(fis);
            fis.close();

            return (props);
        } catch (IOException ioe) {
            // do nothing.  An empty Properties object will be returned.
        }

        return (props);
    }

    /**
     * Reads the content of the given file into a byte array.
     *
     * @param filename name of the file to read.
     * @return content of the file.
     * @throws IOException if there was an error reading the file.
     */
    public static byte[] read(String filename)
            throws IOException {
        BufferedInputStream stream
                = new BufferedInputStream(
                new FileInputStream(filename));

        byte[] content = read(stream);
        stream.close();

        return (content);
    }

    /**
     * Reads the content of the given file into a byte array.
     *
     * @param file File object to read.
     * @return content of the file
     * @throws IOException if there was an error reading the file.
     */
    public static byte[] read(File file)
            throws IOException {
        BufferedInputStream stream
                = new BufferedInputStream(
                new FileInputStream(file));

        byte[] content = read(stream);
        stream.close();

        return (content);
    }

    /**
     * Reads the content of the given file into a byte array.
     *
     * @param in InputStream object to read.
     * @return content of the file
     * @throws IOException if there was an error reading the file.
     */
    public static byte[] read(InputStream in)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean more = true;
        int totalBytes = 0;
        while (more) {
            int avail = in.available();
            if (avail <= 0) {
                avail = 1;
            }

            byte[] buf = new byte[avail];
            int numRead = in.read(buf, 0, avail);
            if (numRead != -1) {
                baos.write(buf, 0, numRead);
                totalBytes += numRead;
            } else {
                more = false;
            }
        }

        if (totalBytes == 0) {
            return new byte[0];
        }

        byte[] ba = baos.toByteArray();
        baos.close();

        return (ba);
    }

    /**
     * Returns a DocumentBuilder object.
     *
     * @return a DocumentBuilder object.
     * @throws ParserConfigurationException if no suitable parser
     *                                      implementation is found.
     */
    public static DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        //to prevent XXE is always to disable DTDs (External Entities) completely
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return (dbf.newDocumentBuilder());
    }

    /**
     * Returns the Element object corresponding to the given element name.
     *
     * @param owner       Document object to search.
     * @param elementName local name to search for.
     * @param nsURI       namespaceURI to used (may be null).
     * @return the Element object corresponding to the given element name or
     * <code>null</code> if none is found.
     */
    public static Element getElement(
            Document owner, String elementName, String nsURI) {
        NodeList nodes
                = nsURI != null
                ? owner.getElementsByTagNameNS(nsURI, elementName)
                : owner.getElementsByTagName(elementName);

        if (nodes != null && nodes.getLength() > 0) {
            return ((Element) nodes.item(0));
        }

        return (null);
    }

    /**
     * Returns the text value of the given element name in the CyberSource
     * namespace.
     *
     * @param owner       Document object to search.
     * @param elementName local name to search for.
     * @param nsURI       namespaceURI to used (may be null).
     * @return the text value of the given element name in the CyberSource
     * namespace or <code>null</code> if none is found.
     */
    public static String getElementText(
            Document owner, String elementName, String nsURI) {
        Element elem = getElement(owner, elementName, nsURI);
        if (elem != null) {
            return (elem.getFirstChild().getNodeValue());
        }

        return (null);
    }


    /**
     * Returns the stack trace of the supplied Exception object.
     *
     * @param e Exception object.
     * @return the stack trace of the Exception object.
     */
    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        pw.close();
        return (stackTrace);
    }

    private static final String CYBS_ROOT_FIELDS = "requestMessage replyMessage nvpRequest nvpReply";

    // parentName must be null.  This is a recursive method.
    // The recursive calls will pass non-NULL strings to said
    // parameter.
    private static void maskXml(int type, Node node, String parentName) {
        if (node == null) return;

        short nodeType = node.getNodeType();
        if (nodeType == Node.TEXT_NODE) {
            if (!PCI.isSafe(type, parentName)) {
                String origVal = node.getNodeValue();
                if (origVal != null) origVal = origVal.trim();
                if (origVal != null && origVal.length() > 0) {
                    node.setNodeValue(PCI.mask(parentName, origVal));
                }
            }
        } else if (nodeType == Node.ELEMENT_NODE ||
                nodeType == Node.DOCUMENT_NODE) {
            if (!node.hasChildNodes()) return;

            String localName = node.getLocalName();
            if (localName == null) localName = "";

            String fieldFullName = null;
            if (parentName == null) {
                // we have not encountered any of the fields in
                // CYBS_ROOT_FIELDS, in which case, we check if
                // the current node's local name is one of them.
                // If so, then we set fieldFullName to "".
                // Otherwise, fieldFullName remains null.
                if (localName.length() > 0 &&
                        CYBS_ROOT_FIELDS.indexOf(localName) != -1) {
                    fieldFullName = "";
                }
            } else if (parentName.length() == 0) {
                // the immediate parent of this node is one of
                // those in CYBS_ROOT_FIELDS, in which case, we
                // use its local name as the field name so far.
                fieldFullName = localName;
            } else {
                // this is a node that is at least two levels
                // down from one of the CYBS_ROOT_FIELDS, in which
                // case, we append its local name to the parent's name.
                fieldFullName = parentName + "_" + localName;
            }

            // call this method recursively on each of the child nodes
            NodeList children = node.getChildNodes();
            int numChildren = children.getLength();
            for (int i = 0; i < numChildren; ++i) {
                maskXml(type, children.item(i), fieldFullName);
            }
        }
    }

    /**
     * Converts a name-value pair string into a Map object.
     *
     * @param src String containing name-value pairs.
     * @return resulting Map object; will be empty if the string was null or
     * empty.
     */
    public static HashMap<String, String> stringToMap(String src) {
        HashMap<String, String> dest = new HashMap<String, String>();

        if (src == null) {
            return (dest);
        }

        String line, key, val;
        int equalsPos, newLinePos, startPos = 0, len = src.length();
        while (startPos < len) {
            newLinePos = src.indexOf('\n', startPos);

            // if the last line does not end with a newline character,
            // assume an imaginary newline character at the end of the string
            if (newLinePos == -1) {
                newLinePos = len;
            }

            line = src.substring(startPos, newLinePos);

            equalsPos = line.indexOf('=');
            if (equalsPos != -1) {
                key = line.substring(0, equalsPos);
                val = line.substring(equalsPos + 1);
            } else {
                key = line;
                val = null;
            }

            dest.put(key, val);

            startPos = newLinePos + 1;
        }

        return (dest);
    }

    /**
     * Converts the contents of a Map object into a string, one name-value pair
     * to a line and the name and value are separated by an equal sign.
     *
     * @param src  Map object whose contents are being converted.
     * @param mask Flag whether or not to mask "unsafe" fields.
     * @param type Relevant only when mask is true, indicates whether this
     *             is the request or the reply map.  Pass either
     *             PCI.REQUEST or PCI.REPLY.
     * @return resulting string; will be empty if the Map object was null or
     * empty.
     */
    public static String mapToString(Map src, boolean mask, int type) {
        StringBuilder dest = new StringBuilder();

        if (src != null && !src.isEmpty()) {
            Iterator iter = src.keySet().iterator();
            String key, val;
            while (iter.hasNext()) {
                key = (String) iter.next();
                val = mask ? PCI.maskIfNotSafe(type, key, (String) src.get(key))
                        : (String) src.get(key);
                dest.append(key).append("=").append(val).append("\n");
            }
        } else {
            return dest.toString();
        }

        String hasEscapes = (String) src.get(HAS_ESCAPES);

        // no need to escape the string if merchant had already escaped it
        // themselves.
        return (("1".equals(hasEscapes) ||
                "true".equalsIgnoreCase(hasEscapes))
                ? dest.toString() : StringEscapeUtils.escapeXml11((dest.toString())));
    }


    /**
     * Read the request xml file
     *
     * @param props    Properties object to lookup properties in
     * @param filename Filename of file containing XML request
     * @return Document Request from filename read as document
     */
    public static Document readRequest(Properties props, String filename) {
        return readRequest(props, filename, null);
    }

    /**
     * Read the request xml file
     *
     * @param props      Properties object to lookup properties in
     * @param filename   Filename of file containing XML request
     * @param merchantId merchantId
     * @return Document Request from filename read as document
     */
    public static Document readRequest(Properties props, String filename, String merchantId) {
        Document doc = null;

        try {
            // read in the XML file
            byte[] xmlBytes = Utility.read(filename);

            // replace _NSURI_ (if any) with effective namespace URI.
            String xmlString = new String(xmlBytes, "UTF-8");
            int pos = xmlString.indexOf("_NSURI_");
            if (pos != -1) {
                StringBuilder sb = new StringBuilder(xmlString);
                sb.replace(
                        pos, pos + 7,
                        XMLClient.getEffectiveNamespaceURI(props, merchantId));
                xmlBytes = sb.toString().getBytes("UTF-8");
            }

            // load the byte array into a Document object.
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            //to prevent XXE is always to disable DTDs (External Entities) completely
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.parse(bais);
            bais.close();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (doc);
    }

    /**
     * Creates an Element object in the CyberSource namespace.
     *
     * @param owner       Document object to own the Element object.
     * @param nsURI       Namespace URI to use.
     * @param elementName local name of Element object to create.
     * @param textValue   text value of the new Element object.
     * @return the newly created Element object.
     */
    public static Element createElement(
            Document owner, String nsURI, String elementName, String textValue) {
        Attr attr
                = owner.createAttributeNS(
                "http://www.w3.org/2000/xmlns/", "xmlns");
        attr.setValue(nsURI);
        Element elem
                = owner.createElementNS(nsURI, elementName);
        elem.setAttributeNodeNS(attr);

        elem.appendChild(owner.createTextNode(textValue));
        return (elem);
    }

    public static long getResponseIssuedAtTime(String responseTime) {
        return parseLong(responseTime, 0L);
    }

    /**
     * This is the response transit time in seconds.
     *
     * @param resIssuedAtTime
     * @return long
     */
    public static long getResponseTransitTime(long resIssuedAtTime) {
        if (resIssuedAtTime > 0) {
            if(checkIfEpochTimeInSecs(resIssuedAtTime)) {
                return (System.currentTimeMillis() / 1000) - resIssuedAtTime;
            }else{
                return System.currentTimeMillis() - resIssuedAtTime;
            }
        }
        return 0;
    }

    private static boolean checkIfEpochTimeInSecs(long resIssuedAtTime) {
        return String.valueOf(resIssuedAtTime).length() == 10;
    }

    private static long parseLong(String val, long defaultValue) {
        long result = defaultValue;
        if (val != null) {
            try {
                result = Long.parseLong(val);
            } catch (NumberFormatException e) {
                //ignored
            }
        }
        return result;
    }

    /**
     * Checks if the request documents contains merchantTransactionIdentifier element.
     *
     * @param request       Document object to own the Element object.
     * @param nsURI       Namespace URI to use.
     * @return merchantTransactionIdentifier element value else null if not exist.
     */
    public static String checkIfMTIFiledExist(Document request, String nsURI) {
        String mtiFieldValue = Utility.getElementText(request, MERCHANT_TRANSACTION_IDENTIFIER, nsURI);
        if(StringUtils.isBlank(mtiFieldValue)){
            mtiFieldValue = Utility.getElementText(request, MERCHANT_TRANSACTION_IDENTIFIER, null);
        }
        return mtiFieldValue;
    }

    /**
     * Checks if the request documents contains ccAuthService element.
     *
     * @param request     Document object to own the Element object.
     * @param nsURI       Namespace URI to use.
     * @return ccAuthService element value else null if not exist.
     */
    public static String checkIfAuthServiceFieldExist(Document request, String nsURI) {
        Element authServiceElement = Utility.getElement(request, AUTH_SERVICE, nsURI);
        if(authServiceElement == null){
            authServiceElement = Utility.getElement(request, AUTH_SERVICE, null);
        }
        String isAuthService = null;
        if(authServiceElement != null){
            isAuthService = authServiceElement.getAttribute(AUTH_SERVICE_XML_RUN_ATT);
        }
        return isAuthService;
    }
}	
