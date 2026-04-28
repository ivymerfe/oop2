package labs.network.protocol;

import java.io.Serializable;

public record UserInfo(String name, String clientType) implements Serializable {

}

