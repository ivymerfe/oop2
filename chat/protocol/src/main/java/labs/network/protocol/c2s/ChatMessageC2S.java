package labs.network.protocol.c2s;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChatMessageC2S extends Message {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final String session;

    public ChatMessageC2S(String message, String session) {
        this.message = message;
        this.session = session;
    }

    public String getMessage() {
        return message;
    }

    public String getSession() {
        return session;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element command = document.createElement("command");
        command.setAttribute("name", "message");
        XMLUtils.appendTextElement(document, command, "message", message);
        XMLUtils.appendTextElement(document, command, "session", session);
        return command;
    }

    public static ChatMessageC2S fromXml(Element command) {
        return new ChatMessageC2S(
                XMLUtils.getRequiredChildText(command, "message"),
                XMLUtils.getRequiredChildText(command, "session")
        );
    }
}
