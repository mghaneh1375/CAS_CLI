package bogen.studio.cas_client.Repository;

import bogen.studio.cas_client.Model.Activation;
import my.common.commonkoochita.Model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * Repository for the {@link Activation} JPA entity. Any custom methods, not
 * already defined in {@link MongoRepository}, are to be defined here
 */
public interface ActivationRepository extends MongoRepository<Activation, ObjectId> {
	@Query(value = "{'value' : ?0, 'created_at': {$gt: ?1} }")
	Activation findByValueAndCreatedAt(String value, long createdAt);

	@Query(value = "{'value' : ?0, 'created_at': {$lt: ?1} }")
	Activation deleteByValueAndCreatedAt(String value, long createdAt);

	@Query(value = "{'value' : ?0, 'token': ?1, 'code': ?2 }")
	Activation findByValueAndTokenAndCode(String value, String token, Integer code);
}
