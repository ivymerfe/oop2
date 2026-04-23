package labs.network.protocol;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract Element toXmlElement(Document document);
}
