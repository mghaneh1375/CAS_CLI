package bogen.studio.cas_client.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetUsernameRequest {

    private String username;
    private String value;
    private String token;
    private Integer code;
}
