package service.impl;

import dao.UserDAO;
import dao.impl.UserDAOImpl;
import exception.AuthenticationException;
import exception.AuthorizationException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.User;
import model.enums.UserRole;
import service.UserService;
import util.PasswordHasher;
import util.ValidationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UserServiceImpl
 * Service implementation for user lifecycle management.
 * Enforces business validation, password hashing, and role policies.
 */
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User register(String username, String email, String password, String fullName, UserRole role, String phone, String address) {
        ValidationUtils.validateUsername(username);
        ValidationUtils.validateEmail(email);
        ValidationUtils.validatePassword(password);
        ValidationUtils.validateNotBlank(fullName, "Full Name");

        if (userDAO.findByEmail(email).isPresent()) {
            throw new ValidationException("An account with email '" + email + "' already exists.");
        }
        if (userDAO.findByUsername(username).isPresent()) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setFullName(fullName.trim());
        user.setRole(role != null ? role : UserRole.BUYER);
        user.setPhone(phone);
        user.setAddress(address);
        user.setActive(true);

        return userDAO.save(user);
    }

    @Override
    public User authenticate(String emailOrUsername, String password) {
        ValidationUtils.validateNotBlank(emailOrUsername, "Email or Username");
        ValidationUtils.validateNotBlank(password, "Password");

        Optional<User> userOpt = emailOrUsername.contains("@")
                ? userDAO.findByEmail(emailOrUsername.trim().toLowerCase())
                : userDAO.findByUsername(emailOrUsername.trim());

        if (!userOpt.isPresent()) {
            throw new AuthenticationException("Invalid email/username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            throw new AuthenticationException("Account has been deactivated. Please contact support.");
        }

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email/username or password.");
        }

        return user;
    }

    @Override
    public User getUserById(int userId) {
        return userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userDAO.findByRole(role);
    }

    @Override
    public boolean updateUserProfile(int userId, String fullName, String phone, String address) {
        User user = getUserById(userId);
        ValidationUtils.validateNotBlank(fullName, "Full Name");
        user.setFullName(fullName.trim());
        user.setPhone(phone);
        user.setAddress(address);
        return userDAO.update(user);
    }

    @Override
    public boolean setUserActiveStatus(int adminUserId, int targetUserId, boolean isActive) {
        User admin = getUserById(adminUserId);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new AuthorizationException("Only platform administrators can change user account status.");
        }
        if (adminUserId == targetUserId) {
            throw new ValidationException("Administrator cannot deactivate their own account.");
        }
        return userDAO.updateStatus(targetUserId, isActive);
    }

    @Override
    public Map<String, Object> getAdminUserMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", userDAO.countUsers());
        metrics.put("totalBuyers", userDAO.countByRole(UserRole.BUYER));
        metrics.put("totalSellers", userDAO.countByRole(UserRole.SELLER));
        metrics.put("totalAdmins", userDAO.countByRole(UserRole.ADMIN));
        return metrics;
    }
}
