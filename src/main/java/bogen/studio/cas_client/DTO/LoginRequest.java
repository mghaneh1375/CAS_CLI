package bogen.studio.cas_client.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String value;
    private String token;
    private Integer code;
    private String callback;
    private String redirectUrl;
}
