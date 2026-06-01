package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.User;

import java.util.Optional;


/**
 * Repository interface for performing database operations
 * on {@link User} entities.
 *
 * <p>
 * Provides CRUD functionality and custom
 * user lookup operations.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a user by full name.
     *
     * <p>
     * Optional prevents null pointer exceptions.
     * Spring automatically generates the query
     * from the method name.
     * </p>
     *
     * @param fullName user full name
     * @return optional containing the matching user if found
     */
    // Optional prevents null pointer exceptions
    // Spring generates the query from the method name
    Optional<User> findByFullName(String fullName);
}
