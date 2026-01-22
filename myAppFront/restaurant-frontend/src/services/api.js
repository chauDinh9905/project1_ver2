import axios from 'axios';

// Cấu hình base URL
const API_BASE_URL = '/api';

// Tạo axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ========== TABLE APIs ==========
export const tableApi = {
  //  SỬA: /tables → /table
  getAllTables: () => api.get('/table'),
  
  getAvailableTables: () => api.get('/table/available'),
  
  //  SỬA: /tables/{id}/occupy → /table/{id}/occupy
  occupyTable: (tableId) => api.post(`/table/${tableId}/occupy`),
  
  //  SỬA: /tables/{id}/release → /table/{id}/release
  releaseTable: (tableId) => api.post(`/table/${tableId}/release`),

  createTable: (data) => api.post('/table', data),
  updateTable: (id, data) => api.put(`/table/${id}`, data),
  deleteTable: (id) => api.delete(`/table/${id}`),
};

// ========== MENU APIs ==========
export const menuApi = {
  // ✅ SỬA: /menu → /menu-item
  getAvailableMenu: () => api.get('/menu-item/available'),
  
  getMenuByCategory: (categoryId) => api.get(`/menu-item/all/${categoryId}`),
  
  getAllMenu: () => api.get('/menu-item/all'),
  
  createMenuItem: (data) => api.post('/menu-item', data),
  
  updateMenuItem: (id, data) => api.put(`/menu-item/${id}`, data),
  
  toggleAvailable: (id) => api.patch(`/menu-item/${id}/toggle`),
  
  deleteMenuItem: (id) => api.delete(`/menu-item/${id}`),
};

// ========== ORDER APIs ==========
export const orderApi = {
  // ✅ SỬA: /orders → /order
  createOrder: (data) => api.post('/order', data),
  
  // ✅ SỬA: /orders/table/{id} → /order/table/{id}
  getCurrentOrder: (tableId) => api.get(`/order/table/${tableId}`),
  
  getOrderHistory: (tableId) => api.get(`/order/table/${tableId}/all`),
  
  getOrderById: (orderId) => api.get(`/order/${orderId}`),
  
  // ✅ SỬA: /orders/status/{status} → /order/all/{status}
  getOrdersByStatus: (status) => api.get(`/order/all/${status}`),
  
  // ✅ SỬA: /orders/{id}/status → /order/{id}/status
  updateOrderStatus: (orderId, status) => 
    api.put(`/order/${orderId}/status`, { status }),
  
  deleteOrder: (orderId) => api.delete(`/order/${orderId}`),
};

api.interceptors.request.use(
  (config) => {
    // Thay 'token' bằng key bạn dùng để lưu token sau login (thường là 'token', 'accessToken', hoặc 'jwt')
    const token = localStorage.getItem('admin_token');  
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('✅ Token attached:', token.substring(0, 20) + '...');
    }else {
      console.warn('⚠️ No token found in localStorage');
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.error('❌ 401 Unauthorized - Token invalid or expired');
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;