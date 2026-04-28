package labs.network.protocol.s2c;

import labs.network.protocol.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class ListUsersS2C extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<UserInfo> users;

    public ListUsersS2C(List<UserInfo> users) {
        this.users = new ArrayList<>(users);
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    @Override
    public Element toXmlElement(Document document) {
        Element success = document.createElement("success");
        Element listUsers = document.createElement("listusers");
        for (UserInfo user : users) {
            Element userElement = document.createElement("user");
            XMLUtils.appendTextElement(document, userElement, "name", user.name());
            XMLUtils.appendTextElement(document, userElement, "type", user.clientType());
            listUsers.appendChild(userElement);
        }
        success.appendChild(listUsers);
        return success;
    }

    public static ListUsersS2C fromXml(Element success) {
        Element listUsers = XMLUtils.findChild(success, "listusers");
        if (listUsers == null) {
            throw new SerializationException("Missing element: listusers");
        }
        List<UserInfo> users = new ArrayList<>();
        NodeList children = listUsers.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element element) || !"user".equals(element.getTagName())) {
                continue;
            }
            users.add(new UserInfo(
                    XMLUtils.getContent(element, "name"),
                    XMLUtils.getContent(element, "type")
            ));
        }
        return new ListUsersS2C(users);
    }
}
