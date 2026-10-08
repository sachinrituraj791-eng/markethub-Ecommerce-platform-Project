package filter;

import model.User;
import model.enums.UserRole;
import util.JSONUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthorizationFilter
 * Enforces Role-Based Access Control (RBAC):
 * - /api/admin/* requires ADMIN role
 * - /api/seller/* requires SELLER role
 * Part of Role-Based Authorization & Security.
 */
@WebFilter(filterName = "AuthorizationFilter", urlPatterns = {
        "/api/seller/*",
        "/api/admin/*"
})
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(JSONUtils.errorResponse("Unauthorized"));
            return;
        }

        String uri = httpRequest.getRequestURI();

        if (uri.contains("/api/admin/") && currentUser.getRole() != UserRole.ADMIN) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(JSONUtils.errorResponse("Forbidden: Administrator privileges required."));
            return;
        }

        if (uri.contains("/api/seller/") && currentUser.getRole() != UserRole.SELLER && currentUser.getRole() != UserRole.ADMIN) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(JSONUtils.errorResponse("Forbidden: Seller merchant privileges required."));
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
