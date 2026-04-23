package labs.network.protocol;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XMLUtils {
    private XMLUtils() {
    }

    public static void appendTextElement(Document document, Element parent, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    public static String getRequiredChildText(Element parent, String childTag) {
        Element child = firstChildElement(parent, childTag);
        if (child == null) {
            throw new SerializationException("Missing element: " + childTag);
        }
        String text = child.getTextContent();
        if (text == null) {
            throw new SerializationException("Empty element: " + childTag);
        }
        return text;
    }

    public static Element firstChildElement(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element element && tagName.equals(element.getTagName())) {
                return element;
            }
        }
        return null;
    }
}
