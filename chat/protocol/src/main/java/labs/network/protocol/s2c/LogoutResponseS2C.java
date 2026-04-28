package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serial;

public class LogoutResponseS2C extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Element toXmlElement(Document document) {
        Element success = document.createElement("success");
        success.setAttribute("response", "logout");
        return success;
    }

    public static LogoutResponseS2C fromXml(Element success) {
        return new LogoutResponseS2C();
    }
}
