import { useState, useEffect } from 'react';
import { useWebSocket } from '../../hooks/useWebSocket';
import { orderApi } from '../../services/api';
import './OrderStatus.css';

const STATUS_LABELS = {
  'PENDING': { text: 'Đang chờ xác nhận', color: '#ffc107', icon: '⏳' },
  'PREPARING': { text: 'Đang chuẩn bị', color: '#17a2b8', icon: '👨‍🍳' },
  'SERVING': { text: 'Đang phục vụ', color: '#007bff', icon: '🍽️' },
  'COMPLETED': { text: 'Hoàn thành', color: '#28a745', icon: '✅' }
};

export default function OrderStatus({ tableId, onAddMoreItems }) {  // ⭐ Thêm prop
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const { subscribe, isConnected } = useWebSocket();

  const currentTableId = tableId || localStorage.getItem('currentTableId');

  // Load order lần đầu
  useEffect(() => {
    if (currentTableId) {
      loadCurrentOrder();
    }
  }, [currentTableId]);

  // Subscribe WebSocket
  useEffect(() => {
    if (!currentTableId) return;

    const subscription = subscribe(`/topic/order/${currentTableId}`, (data) => {
      console.log('Order update received:', data);
      
      if (data.status === 'NO_ACTIVE_ORDER') {
        setOrder(null);
      } else {
        setOrder(data);
      }
    });

    return () => subscription?.unsubscribe();
  }, [subscribe, currentTableId]);

  const loadCurrentOrder = async () => {
    try {
      setLoading(true);
      const response = await orderApi.getCurrentOrder(currentTableId);
      
      if (response.status === 204) {
        setOrder(null);
      } else {
        setOrder(response.data);
      }
    } catch (error) {
      if (error.response?.status === 204) {
        setOrder(null);
      } else {
        console.error('Error loading order:', error);
      }
    } finally {
      setLoading(false);
    }
  };

  // ⭐ THÊM HANDLER
  const handleAddMoreItems = () => {
    if (onAddMoreItems) {
      onAddMoreItems();  // Gọi callback từ App.jsx
    }
  };

  if (loading) {
    return (
      <div className="order-status-container">
        <h2>Đang tải...</h2>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="order-status-container">
        <div className="no-order">
          <h2>📋 Chưa có đơn hàng</h2>
          <p>Bạn chưa đặt món nào</p>
        </div>
      </div>
    );
  }

  const statusInfo = STATUS_LABELS[order.status] || STATUS_LABELS.PENDING;

  return (
    <div className="order-status-container">
      <div className="status-header">
        <h1>Trạng thái đơn hàng</h1>
        <div className={`connection-badge ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? '🟢 Live' : '🔴 Offline'}
        </div>
      </div>

      <div className="status-card" style={{ borderColor: statusInfo.color }}>
        <div className="status-icon" style={{ backgroundColor: statusInfo.color }}>
          {statusInfo.icon}
        </div>
        <div className="status-text" style={{ color: statusInfo.color }}>
          {statusInfo.text}
        </div>
        <div className="status-time">
          Cập nhật: {new Date(order.updateAt).toLocaleTimeString('vi-VN')}
        </div>
      </div>

      <div className="order-details">
        <h2>Chi tiết đơn hàng</h2>
        
        {order.items && order.items.length > 0 ? (
          <div className="order-items-list">
            {order.items.map((item, index) => (
              <div key={index} className="order-item-row">
                <div className="item-info">
                  <span className="item-name">{item.name}</span>
                  <span className="item-quantity">x{item.quantity}</span>
                </div>
                {item.notes && (
                  <div className="item-notes">📝 {item.notes}</div>
                )}
                <div className="item-price">
                  {(item.price * item.quantity).toLocaleString()}đ
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p>Không có món nào</p>
        )}

        {order.notes && (
          <div className="order-notes-display">
            <strong>Ghi chú:</strong> {order.notes}
          </div>
        )}

        <div className="order-total">
          <span>Tổng cộng:</span>
          <span className="total-amount">{order.totalPrice.toLocaleString()}đ</span>
        </div>

        {/* ⭐ NÚT ĐẶT THÊM MÓN - CHỈ HIỆN KHI ORDER CHƯA COMPLETED */}
        {order.status !== 'COMPLETED' && (
          <button 
            className="add-more-btn"
            onClick={handleAddMoreItems}
          >
            ➕ Đặt thêm món
          </button>
        )}
      </div>

      <div className="order-timeline">
        <div className={`timeline-step ${['PENDING', 'PREPARING', 'SERVING', 'COMPLETED'].includes(order.status) ? 'completed' : ''}`}>
          ⏳ Chờ xác nhận
        </div>
        <div className={`timeline-step ${['PREPARING', 'SERVING', 'COMPLETED'].includes(order.status) ? 'completed' : ''}`}>
          👨‍🍳 Chuẩn bị
        </div>
        <div className={`timeline-step ${['SERVING', 'COMPLETED'].includes(order.status) ? 'completed' : ''}`}>
          🍽️ Phục vụ
        </div>
        <div className={`timeline-step ${order.status === 'COMPLETED' ? 'completed' : ''}`}>
          ✅ Hoàn thành
        </div>
      </div>
    </div>
  );
}