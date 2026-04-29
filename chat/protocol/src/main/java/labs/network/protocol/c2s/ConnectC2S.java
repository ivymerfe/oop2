package labs.network.protocol.c2s;

import labs.network.protocol.Message;
import labs.network.protocol.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serial;

public class ConnectC2S extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String clientType;
    private final String password;

    public ConnectC2S(String name, String clientType, String password) {
        this.name = name;
        this.clientType = clientType;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getClientType() {
        return clientType;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element command = document.createElement("command");
        command.setAttribute("name", "login");
        XMLUtils.appendTextElement(document, command, "name", name);
        XMLUtils.appendTextElement(document, command, "type", clientType);
        XMLUtils.appendTextElement(document, command, "password", password);
        return command;
    }

    public static ConnectC2S fromXml(Element command) {
        return new ConnectC2S(
                XMLUtils.getContent(command, "name"),
                XMLUtils.getContent(command, "type"),
                XMLUtils.getOrDefault(command, "password", "")
        );
    }
}
