package labs.network.protocol;

import labs.network.protocol.c2s.ChatMessageC2S;
import labs.network.protocol.c2s.ConnectC2S;
import labs.network.protocol.c2s.ListUsersC2S;
import labs.network.protocol.c2s.LogoutC2S;
import labs.network.protocol.s2c.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class XMLSerializer implements Serializer {
    @Override
    public byte[] serialize(Message message) {
        try {
            Document document = newDocument();
            Element root = message.toXmlElement(document);
            document.appendChild(root);
            return toBytes(document);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize XML message", e);
        }
    }

    @Override
    public Message deserialize(byte[] data) {
        try {
            DocumentBuilder builder = newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(data));
            Element root = document.getDocumentElement();
            if (root == null) {
                throw new SerializationException("XML document has no root element");
            }
            return fromElement(root);
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize XML message", e);
        }
    }

    private Message fromElement(Element root) {
        String tag = root.getTagName();
        if ("command".equals(tag)) {
            String name = root.getAttribute("name");
            return parseCommand(root, name);
        }
        if ("error".equals(tag)) {
            return ErrorS2C.fromXml(root);
        }
        if ("success".equals(tag)) {
            Element listUsers = XMLUtils.findChild(root, "listusers");
            if (listUsers != null) {
                return ListUsersS2C.fromXml(root);
            }
            Element session = XMLUtils.findChild(root, "session");
            if (session != null) {
                return LoginResposeS2C.fromXml(root);
            }

            String response = root.getAttribute("response");
            if ("logout".equals(response)) {
                return LogoutResponseS2C.fromXml(root);
            }
            if ("message".equals(response) || response.isEmpty()) {
                return MessageResponseS2C.fromXml(root);
            }

            throw new SerializationException("Unsupported success response type: " + response);
        }
        if ("event".equals(tag)) {
            String name = root.getAttribute("name");
            return parseEvent(root, name);
        }
        throw new SerializationException("Unsupported XML root tag: " + tag);
    }

    private Message parseCommand(Element command, String name) {
        if ("login".equals(name)) {
            return ConnectC2S.fromXml(command);
        }
        if ("list".equals(name)) {
            return ListUsersC2S.fromXml(command);
        }
        if ("message".equals(name)) {
            return ChatMessageC2S.fromXml(command);
        }
        if ("logout".equals(name)) {
            return LogoutC2S.fromXml(command);
        }
        throw new SerializationException("Unsupported command name: " + name);
    }

    private Message parseEvent(Element event, String name) {
        if ("message".equals(name)) {
            return EventMessageS2C.fromXml(event);
        }
        if ("userlogin".equals(name)) {
            return UserLoginEventS2C.fromXml(event);
        }
        if ("userlogout".equals(name)) {
            return UserLogoutEventS2C.fromXml(event);
        }
        throw new SerializationException("Unsupported event name: " + name);
    }

    private Document newDocument() throws Exception {
        return newDocumentBuilder().newDocument();
    }

    private DocumentBuilder newDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private byte[] toBytes(Document document) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(output));
        return output.toByteArray();
    }
}
