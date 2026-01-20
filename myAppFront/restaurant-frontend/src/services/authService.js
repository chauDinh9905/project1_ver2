import axios from 'axios';

const API_BASE_URL = '/api';

const TOKEN_KEY = 'admin_token';
const USER_KEY = 'admin_user';

const authService = {
  login: async (username, password) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/auth/login`, {
        userName: username, 
        password: password
      });
      
      const { token, userName, role, id } = response.data;
      
      // Lưu token và user info
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify({ 
        id, 
        username: userName,  
        role 
      }));
      
      // Set default authorization header
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      return { token, user: { id, username: userName, role } };
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    delete axios.defaults.headers.common['Authorization'];
  },

  getToken: () => {
    return localStorage.getItem(TOKEN_KEY);
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated: () => {
    const token = localStorage.getItem(TOKEN_KEY);
    return !!token;
  },

  setupAxiosInterceptor: () => {
    const token = authService.getToken();
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }

    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          authService.logout();
          window.location.href = '/';
        }
        return Promise.reject(error);
      }
    );
  }
};

export default authService;