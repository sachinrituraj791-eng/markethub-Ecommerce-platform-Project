package filter;

import model.User;
import util.JSONUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthenticationFilter
 * Intercepts protected API endpoints and ensures an active authenticated session exists.
 * Part of Servlets & HTTP Integration (7 Marks).
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {
        "/api/cart/*",
        "/api/orders/*",
        "/api/wishlist/*",
        "/api/seller/*",
        "/api/admin/*"
})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow CORS pre-flight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(JSONUtils.errorResponse("Authentication required. Please log in to proceed."));
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
