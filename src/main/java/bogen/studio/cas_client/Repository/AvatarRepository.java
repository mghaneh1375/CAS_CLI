package bogen.studio.cas_client.Repository;

import bogen.studio.cas_client.Model.Avatar;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Repository for the {@link bogen.studio.cas_client.Model.Avatar} JPA entity. Any custom methods, not
 * already defined in {@link MongoRepository}, are to be defined here
 */
public interface AvatarRepository extends MongoRepository<Avatar, ObjectId> {

    @Query(value = "{'is_default' : true }")
    Avatar findByDefault();

}
