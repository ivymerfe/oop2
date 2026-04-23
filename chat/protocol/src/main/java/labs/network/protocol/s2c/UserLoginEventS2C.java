package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UserLoginEventS2C extends Message  {
    private static final long serialVersionUID = 1L;

    private final String name;

    public UserLoginEventS2C(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element event = document.createElement("event");
        event.setAttribute("name", "userlogin");
        XMLUtils.appendTextElement(document, event, "name", name);
        return event;
    }

    public static UserLoginEventS2C fromXml(Element event) {
        return new UserLoginEventS2C(XMLUtils.getRequiredChildText(event, "name"));
    }
}
