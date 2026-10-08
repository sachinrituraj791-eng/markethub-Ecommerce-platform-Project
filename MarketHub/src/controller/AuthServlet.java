package controller;

import exception.AuthenticationException;
import exception.ValidationException;
import model.User;
import model.enums.UserRole;
import service.UserService;
import service.impl.UserServiceImpl;
import util.JSONUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * AuthServlet
 * Handles user registration, credential verification, session creation, and logout.
 * Endpoints:
 * POST /api/register
 * POST /api/login
 * POST /api/logout
 * GET  /api/auth/me
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/register", "/api/login", "/api/logout", "/api/auth/me"})
public class AuthServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        this.userService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getServletPath();

        if ("/api/auth/me".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                User user = (User) session.getAttribute("currentUser");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("Authenticated session active", userToJson(user)));
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(JSONUtils.errorResponse("Not logged in"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getServletPath();

        String body = JSONUtils.readBody(req.getReader());
        Map<String, String> payload = JSONUtils.parseSimpleJson(body);

        try {
            if ("/api/register".equals(path)) {
                String username = payload.get("username");
                String email = payload.get("email");
                String password = payload.get("password");
                String fullName = payload.get("fullName");
                String roleStr = payload.get("role");
                String phone = payload.get("phone");
                String address = payload.get("address");

                UserRole role = UserRole.fromString(roleStr);
                // Security rule: Buyers or Sellers can register; ADMIN cannot self-assign via registration
                if (role == UserRole.ADMIN) {
                    role = UserRole.BUYER;
                }

                User registered = userService.register(username, email, password, fullName, role, phone, address);
                
                // Establish session upon registration
                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", registered);

                resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                resp.getWriter().write(JSONUtils.successResponse("Registration successful", userToJson(registered)));

            } else if ("/api/login".equals(path)) {
                String emailOrUsername = payload.get("emailOrUsername");
                if (emailOrUsername == null || emailOrUsername.isEmpty()) {
                    emailOrUsername = payload.get("email");
                }
                String password = payload.get("password");

                User user = userService.authenticate(emailOrUsername, password);

                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", user);

                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                resp.getWriter().write(JSONUtils.successResponse("Login successful", userToJson(user)));

            } else if ("/api/logout".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JSONUtils.successResponse("Logged out successfully", "null"));
            }
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (AuthenticationException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            resp.getWriter().write(JSONUtils.errorResponse(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(JSONUtils.errorResponse("Internal server error: " + e.getMessage()));
        }
    }

    private String userToJson(User u) {
        return "{\"userId\":" + u.getUserId() +
                ",\"username\":\"" + JSONUtils.escape(u.getUsername()) + "\"" +
                ",\"email\":\"" + JSONUtils.escape(u.getEmail()) + "\"" +
                ",\"fullName\":\"" + JSONUtils.escape(u.getFullName()) + "\"" +
                ",\"role\":\"" + u.getRole().name() + "\"" +
                ",\"phone\":\"" + JSONUtils.escape(u.getPhone()) + "\"" +
                ",\"address\":\"" + JSONUtils.escape(u.getAddress()) + "\"" +
                ",\"isActive\":" + u.isActive() + "}";
    }
}
