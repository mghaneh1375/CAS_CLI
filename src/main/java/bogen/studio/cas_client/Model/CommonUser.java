package bogen.studio.cas_client.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Document(collection="users")
@Getter
@Setter
public class CommonUser extends my.common.commonkoochita.Model.User {

    public Collection<? extends GrantedAuthority> getAllAuthorities() {
        return getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

}

