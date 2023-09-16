package bogen.studio.cas_client.Repository;

import bogen.studio.cas_client.Model.CommonUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * Repository for the {@link CommonUser} JPA entity. Any custom methods, not
 * already defined in {@link MongoRepository}, are to be defined here
 */
public interface UserRepository extends MongoRepository<CommonUser, String> {

	@Query(value = "{'username' : ?0 }", count = true)
	int findByUsernameCount(String username);

	@Query(value = "{'mail' : ?0 }", count = true)
	int findByMailCount(String mail);

	@Query(value = "{'phone' : ?0 }", count = true)
	int findByPhoneCount(String phone);

	@Query(value = "{'phone' : ?0 }")
	Optional<CommonUser> findByPhone(String phone);

	@Query(value = "{'username' : ?0 }")
	Optional<CommonUser> findByUsername(String username);

	@Query(value = "{'mail' : ?0 }")
	Optional<CommonUser> findByEmail(String email);

}
