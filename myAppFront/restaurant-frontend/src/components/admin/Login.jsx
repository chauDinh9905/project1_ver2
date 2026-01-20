import { useState } from 'react';
import authService from '../../services/authService';
import './Login.css';

export default function AdminLogin({ onLoginSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.login(username, password);
      
      if (onLoginSuccess) {
        onLoginSuccess();
      }
    } catch (err) {
      console.error('Login failed:', err);
      
      if (err.response?.status === 401) {
        setError('Tên đăng nhập hoặc mật khẩu không đúng');
      } else if (err.response?.status === 403) {
        setError('Tài khoản không có quyền truy cập');
      } else {
        setError('Đăng nhập thất bại. Vui lòng thử lại!');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>🔐 Đăng nhập Admin</h1>
          <p>Nhà hàng ABC - Quản lý hệ thống</p>
        </div>

        <div className="login-form">
          {error && (
            <div className="error-message">
              ⚠️ {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">Tên đăng nhập</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Nhập tên đăng nhập"
              autoFocus
              disabled={loading}
              onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mật khẩu</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Nhập mật khẩu"
              disabled={loading}
              onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
            />
          </div>

          <button 
            onClick={handleSubmit} 
            className="login-button"
            disabled={loading}
          >
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </div>

        <div className="login-footer">
          <p>💡 Chỉ dành cho quản trị viên</p>
        </div>
      </div>
    </div>
  );
}