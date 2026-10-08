/**
 * MarketHub Centralized API Client Layer
 * Handles modern Fetch API requests to Java Servlets, JSON headers,
 * status code triage, and unified error handling.
 */
const API = {
  baseUrl: '',

  async request(endpoint, options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    };

    try {
      const response = await fetch(this.baseUrl + endpoint, config);
      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        if (response.status === 401) {
          // Session expired or unauthenticated
          console.warn('Session expired or login required.');
        }
        const errorMsg = data.message || `HTTP Error ${response.status}: ${response.statusText}`;
        const error = new Error(errorMsg);
        error.status = response.status;
        error.data = data;
        throw error;
      }

      return data;
    } catch (err) {
      console.error(`[API Error] ${endpoint}:`, err);
      throw err;
    }
  },

  // Auth endpoints
  auth: {
    login: (credentials) => API.request('/api/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    }),
    register: (userData) => API.request('/api/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    }),
    logout: () => API.request('/api/logout', {
      method: 'POST'
    }),
    me: () => API.request('/api/auth/me')
  },

  // Products
  products: {
    list: (params = {}) => {
      const query = new URLSearchParams(params).toString();
      return API.request(`/api/products${query ? '?' + query : ''}`);
    },
    get: (id) => API.request(`/api/products/${id}`),
    create: (data) => API.request('/api/products', {
      method: 'POST',
      body: JSON.stringify(data)
    }),
    update: (id, data) => API.request(`/api/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    }),
    delete: (id) => API.request(`/api/products/${id}`, {
      method: 'DELETE'
    })
  },

  // Cart
  cart: {
    get: () => API.request('/api/cart'),
    add: (productId, quantity = 1) => API.request('/api/cart', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity })
    }),
    update: (productId, quantity) => API.request('/api/cart', {
      method: 'PUT',
      body: JSON.stringify({ productId, quantity })
    }),
    remove: (productId) => API.request(`/api/cart?productId=${productId}`, {
      method: 'DELETE'
    }),
    clear: () => API.request('/api/cart', {
      method: 'DELETE'
    })
  },

  // Orders
  orders: {
    list: () => API.request('/api/orders'),
    get: (id) => API.request(`/api/orders/${id}`),
    place: (orderData) => API.request('/api/orders', {
      method: 'POST',
      body: JSON.stringify(orderData)
    })
  },

  // Wishlist
  wishlist: {
    list: () => API.request('/api/wishlist'),
    add: (productId) => API.request('/api/wishlist', {
      method: 'POST',
      body: JSON.stringify({ productId })
    }),
    remove: (productId) => API.request(`/api/wishlist/${productId}`, {
      method: 'DELETE'
    })
  },

  // Reviews
  reviews: {
    getByProduct: (productId) => API.request(`/api/reviews/${productId}`),
    add: (reviewData) => API.request('/api/reviews', {
      method: 'POST',
      body: JSON.stringify(reviewData)
    })
  },

  // Seller Portal
  seller: {
    getProducts: () => API.request('/api/seller/products'),
    getOrders: () => API.request('/api/seller/orders'),
    updateOrderStatus: (orderId, status) => API.request(`/api/seller/orders/${orderId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    }),
    getDashboard: () => API.request('/api/seller/dashboard')
  },

  // Admin Portal
  admin: {
    getUsers: () => API.request('/api/admin/users'),
    toggleUserStatus: (userId, isActive) => API.request(`/api/admin/users/${userId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ isActive })
    }),
    getProducts: () => API.request('/api/admin/products'),
    getOrders: () => API.request('/api/admin/orders'),
    getDashboard: () => API.request('/api/admin/dashboard')
  }
};

// UI Notification Toast Helper
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerText = message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}
