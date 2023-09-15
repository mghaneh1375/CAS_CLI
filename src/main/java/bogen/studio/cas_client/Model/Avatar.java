package bogen.studio.cas_client.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "avatar")
public class Avatar {

    @Id
    @MongoId
    @Field("_id")
    private ObjectId id;

    private String name;

    @Field("is_default")
    private Boolean isDefault;

}
