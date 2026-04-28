package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serial;

public class UserLoginEventS2C extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String clientType;

    public UserLoginEventS2C(String name, String clientType) {
        this.name = name;
        this.clientType = clientType;
    }

    public String getName() {
        return name;
    }

    public String getClientType() {
        return clientType;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element event = document.createElement("event");
        event.setAttribute("name", "userlogin");
        XMLUtils.appendTextElement(document, event, "name", name);
        XMLUtils.appendTextElement(document, event, "type", clientType);
        return event;
    }

    public static UserLoginEventS2C fromXml(Element event) {
        String name = XMLUtils.getContent(event, "name");
        String clientType = XMLUtils.getOrDefault(event, "type", "unknown");
        return new UserLoginEventS2C(name, clientType);
    }
}
