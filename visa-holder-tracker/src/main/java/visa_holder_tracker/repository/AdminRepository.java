package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.Admin;

import java.util.Optional;



/**
 * Repository interface for performing database operations
 * on {@link Admin} entities.
 *
 * <p>
 * Provides CRUD functionality through {@link JpaRepository}
 * and custom query methods for administrator lookup.
 * </p>
 */
// Uses Long for the id, as Admin doesn't use passport number
// If findByFullName in UserRepository fails it falls back to this
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Finds an administrator by full name.
     *
     * @param fullName administrator full name
     * @return optional containing the matching admin if found
     */
    Optional<Admin> findByFullName(String fullName);
}
