package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EventMessageS2C extends Message  {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final String fromName;

    public EventMessageS2C(String message, String fromName) {
        this.message = message;
        this.fromName = fromName;
    }

    public String getMessage() {
        return message;
    }

    public String getFromName() {
        return fromName;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element event = document.createElement("event");
        event.setAttribute("name", "message");
        XMLUtils.appendTextElement(document, event, "message", message);
        XMLUtils.appendTextElement(document, event, "name", fromName);
        return event;
    }

    public static EventMessageS2C fromXml(Element event) {
        return new EventMessageS2C(
                XMLUtils.getRequiredChildText(event, "message"),
                XMLUtils.getRequiredChildText(event, "name")
        );
    }
}
