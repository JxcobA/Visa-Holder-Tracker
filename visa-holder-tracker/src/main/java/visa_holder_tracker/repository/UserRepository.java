package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository {
    // Optional prevents null pointer exceptions
    // Spring generates the query from the method name
    Optional<User> findByUsername(String username);
}
