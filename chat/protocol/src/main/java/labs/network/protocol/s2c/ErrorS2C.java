package labs.network.protocol.s2c;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serial;

public class ErrorS2C extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;

    public ErrorS2C(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element error = document.createElement("error");
        XMLUtils.appendTextElement(document, error, "message", message);
        return error;
    }

    public static ErrorS2C fromXml(Element error) {
        return new ErrorS2C(XMLUtils.getContent(error, "message"));
    }
}
