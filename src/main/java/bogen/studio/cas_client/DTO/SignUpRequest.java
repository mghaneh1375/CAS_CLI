package bogen.studio.cas_client.DTO;

import bogen.studio.cas_client.Enum.AuthVia;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {

    private String value;
    private AuthVia via;
    private String callback;

}
