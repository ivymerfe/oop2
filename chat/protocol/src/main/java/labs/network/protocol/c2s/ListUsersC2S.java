package labs.network.protocol.c2s;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ListUsersC2S extends Message  {
    private static final long serialVersionUID = 1L;

    private final String session;

    public ListUsersC2S(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element command = document.createElement("command");
        command.setAttribute("name", "list");
        XMLUtils.appendTextElement(document, command, "session", session);
        return command;
    }

    public static ListUsersC2S fromXml(Element command) {
        return new ListUsersC2S(XMLUtils.getRequiredChildText(command, "session"));
    }
}
