package bogen.studio.cas_client.DTO;

import bogen.studio.cas_client.Validator.ValidatedLoginRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidatedLoginRequest
public class LoginRequest {
    private String value;
    private String token;
    private Integer code;
    private String callback;
    private String redirectUrl;
}
