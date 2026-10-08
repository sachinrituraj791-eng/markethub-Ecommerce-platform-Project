package service;

import model.User;
import model.enums.UserRole;
import java.util.List;
import java.util.Map;

/**
 * UserService Interface
 * Core Java Service Contract for user operations.
 */
public interface UserService {

    User register(String username, String email, String password, String fullName, UserRole role, String phone, String address);

    User authenticate(String emailOrUsername, String password);

    User getUserById(int userId);

    List<User> getAllUsers();

    List<User> getUsersByRole(UserRole role);

    boolean updateUserProfile(int userId, String fullName, String phone, String address);

    boolean setUserActiveStatus(int adminUserId, int targetUserId, boolean isActive);

    Map<String, Object> getAdminUserMetrics();
}
