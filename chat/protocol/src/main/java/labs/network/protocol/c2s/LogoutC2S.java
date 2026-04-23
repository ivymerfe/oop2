package labs.network.protocol.c2s;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LogoutC2S extends Message {
    private static final long serialVersionUID = 1L;

    private final String session;

    public LogoutC2S(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element command = document.createElement("command");
        command.setAttribute("name", "logout");
        XMLUtils.appendTextElement(document, command, "session", session);
        return command;
    }

    public static LogoutC2S fromXml(Element command) {
        return new LogoutC2S(XMLUtils.getRequiredChildText(command, "session"));
    }
}
