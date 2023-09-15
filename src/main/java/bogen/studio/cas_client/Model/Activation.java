package bogen.studio.cas_client.Model;

import bogen.studio.cas_client.Enum.AuthVia;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "activation")
public class Activation {

    @Id
    @MongoId
    @Field("_id")
    private ObjectId id;

    private String token;

    @Field("created_at")
    private Long createdAt;

    private Integer code;
    private AuthVia authVia;
    private String value;
    private String password;
    private boolean used = false;

}
