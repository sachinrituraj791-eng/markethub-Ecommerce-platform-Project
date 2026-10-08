package dao;

import model.User;
import model.enums.UserRole;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO Interface
 * Defines database contracts for user authentication, profiles, and administration.
 */
public interface UserDAO extends GenericDAO<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByRole(UserRole role);

    boolean updateStatus(int userId, boolean isActive);

    int countUsers();

    int countByRole(UserRole role);
}
