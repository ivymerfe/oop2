package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MessageResponseS2C extends Message  {
    private static final long serialVersionUID = 1L;

    @Override
    public Element toXmlElement(Document document) {
        Element success = document.createElement("success");
        success.setAttribute("response", "message");
        return success;
    }

    public static MessageResponseS2C fromXml(Element success) {
        return new MessageResponseS2C();
    }
}
