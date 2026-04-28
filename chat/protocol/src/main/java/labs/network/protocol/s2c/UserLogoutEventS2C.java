package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serial;

public class UserLogoutEventS2C extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;

    public UserLogoutEventS2C(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element event = document.createElement("event");
        event.setAttribute("name", "userlogout");
        XMLUtils.appendTextElement(document, event, "name", name);
        return event;
    }

    public static UserLogoutEventS2C fromXml(Element event) {
        return new UserLogoutEventS2C(XMLUtils.getContent(event, "name"));
    }
}
