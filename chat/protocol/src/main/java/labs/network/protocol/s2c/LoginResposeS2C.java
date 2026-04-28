package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serial;

public class LoginResposeS2C extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String session;

    public LoginResposeS2C(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element success = document.createElement("success");
        XMLUtils.appendTextElement(document, success, "session", session);
        return success;
    }

    public static LoginResposeS2C fromXml(Element success) {
        return new LoginResposeS2C(XMLUtils.getContent(success, "session"));
    }
}
