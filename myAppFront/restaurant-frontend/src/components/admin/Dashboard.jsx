import { useState, useEffect } from 'react';
import { useWebSocket } from '../../hooks/useWebSocket';
import { tableApi, orderApi } from '../../services/api';
import authService from '../../services/authService';
import OrderDetailModal from './OrderDetailModal';
import './Dashboard.css';

export default function AdminDashboard({ onNavigate, onLogout }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTable, setSelectedTable] = useState(null);
  const [showOrderModal, setShowOrderModal] = useState(false);
  const { subscribe, isConnected } = useWebSocket();

  const currentUser = authService.getCurrentUser();

  useEffect(() => {
    loadTables();
  }, []);

  // ⭐ Subscribe WebSocket - FIX
  useEffect(() => {
    if (!isConnected) {
      console.log('⏳ Waiting for WebSocket connection...');
      return;
    }

    console.log('🔌 Admin WebSocket connected, subscribing...');

    // ⭐ SUBSCRIBE 1: Cập nhật trạng thái bàn
    const tableSub = subscribe('/topic/tables', (data) => {
      console.log('📊 [ADMIN] Table update received:', data);
      
      // Backend gửi TableStatusUpdate wrapper với property "tables"
      if (data.tables && Array.isArray(data.tables)) {
        console.log('✅ Updating tables from WebSocket:', data.tables.length, 'tables');
        setTables(data.tables);
      } else {
        console.warn('⚠️ Unexpected table update format, reloading...');
        loadTables();
      }
    });

    // ⭐ SUBSCRIBE 2: Nhận thông báo order mới/update - FIX TOPIC
    const orderSub = subscribe('/topic/admin/orders', (orderData) => {
      console.log('📋 [ADMIN] Order update received:', orderData);
      
      // Reload tables để cập nhật currentOrderId và totalPrice
      loadTables();
    });

    return () => {
      console.log('🔌 Admin unsubscribing from WebSocket topics');
      tableSub?.unsubscribe();
      orderSub?.unsubscribe();
    };
  }, [subscribe, isConnected]); // ⭐ Thêm isConnected vào dependency

  const loadTables = async () => {
    try {
      setLoading(true);
      const response = await tableApi.getAllTables();
      console.log('🔄 Loaded tables from API:', response.data);
      setTables(response.data);
    } catch (error) {
      console.error('❌ Error loading tables:', error);
      alert('Không thể tải danh sách bàn');
    } finally {
      setLoading(false);
    }
  };

  const handleTableClick = async (table) => {
    if (table.status === 'AVAILABLE') {
      console.log('ℹ️ Clicked on empty table, no action');
      return;
    }

    console.log('👆 Opening order modal for table:', table.id);
    setSelectedTable(table);
    setShowOrderModal(true);
  };

  const handleCloseModal = () => {
    console.log('❌ Closing order modal');
    setShowOrderModal(false);
    setSelectedTable(null);
  };

  const handleOrderUpdated = () => {
    console.log('🔄 Order updated, reloading tables...');
    loadTables();
  };

  const handleLogout = () => {
    console.log('🚪 Logging out admin...');
    authService.logout();
    if (onLogout) {
      onLogout();
    }
  };

  const getTableStats = () => {
    const total = tables.length;
    const occupied = tables.filter(t => t.status === 'OCCUPIED').length;
    const available = tables.filter(t => t.status === 'AVAILABLE').length;
    
    return { total, occupied, available };
  };

  const stats = getTableStats();

  if (loading) {
    return (
      <div className="admin-dashboard">
        <h1>Đang tải...</h1>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      {/* Header */}
      <div className="dashboard-header">
        <div className="header-left">
          <h1>📊 Admin Dashboard</h1>
          <p className="welcome-text">Xin chào, <strong>{currentUser?.username || 'Admin'}</strong></p>
        </div>
        
        <div className="header-right">
          <div className={`connection-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
            {isConnected ? '🟢 Live' : '🔴 Offline'}
          </div>
          
          <button className="nav-button" onClick={() => onNavigate('menu')}>
            🍽️ Quản lý Menu
          </button>
          
          <button className="logout-button" onClick={handleLogout}>
            🚪 Đăng xuất
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-container">
        <div className="stat-card total">
          <div className="stat-icon">🪑</div>
          <div className="stat-info">
            <div className="stat-value">{stats.total}</div>
            <div className="stat-label">Tổng số bàn</div>
          </div>
        </div>

        <div className="stat-card occupied">
          <div className="stat-icon">👥</div>
          <div className="stat-info">
            <div className="stat-value">{stats.occupied}</div>
            <div className="stat-label">Bàn đang sử dụng</div>
          </div>
        </div>

        <div className="stat-card available">
          <div className="stat-icon">✅</div>
          <div className="stat-info">
            <div className="stat-value">{stats.available}</div>
            <div className="stat-label">Bàn trống</div>
          </div>
        </div>
      </div>

      {/* Tables Grid */}
      <div className="tables-section">
        <h2>Danh sách bàn</h2>
        
        <div className="tables-grid">
          {tables.map(table => (
            <div
              key={table.id}
              className={`table-card-admin ${table.status.toLowerCase()}`}
              onClick={() => handleTableClick(table)}
            >
              <div className="table-header-admin">
                <span className="table-number">Bàn {table.id}</span>
                <span className="table-capacity">👥 {table.capacity}</span>
              </div>

              <div className="table-status-badge">
                {table.status === 'AVAILABLE' ? '✅ Trống' : '🔴 Đang dùng'}
              </div>

              {table.currentOrderId && (
                <div className="order-info">
                  <div className="order-id">Đơn #{table.currentOrderId}</div>
                  <div className="order-total">
                    {table.totalPrice?.toLocaleString() || '0'}đ
                  </div>
                </div>
              )}

              {table.status === 'OCCUPIED' && (
                <div className="click-hint">👆 Click để xem chi tiết</div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Order Detail Modal */}
      {showOrderModal && selectedTable && (
        <OrderDetailModal
          table={selectedTable}
          onClose={handleCloseModal}
          onOrderUpdated={handleOrderUpdated}
        />
      )}
    </div>
  );
}