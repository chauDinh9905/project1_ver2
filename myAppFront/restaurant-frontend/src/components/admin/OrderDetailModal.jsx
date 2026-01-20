import { useState, useEffect } from 'react';
import { orderApi, tableApi } from '../../services/api';
import { useWebSocket } from '../../hooks/useWebSocket';
import './OrderDetailModal.css';

const STATUS_OPTIONS = [
  { value: 'PENDING', label: '⏳ Đang chờ', color: '#ffc107' },
  { value: 'PREPARING', label: '👨‍🍳 Đang chuẩn bị', color: '#17a2b8' },
  { value: 'SERVING', label: '🍽️ Đang phục vụ', color: '#007bff' },
  { value: 'COMPLETED', label: '✅ Hoàn thành', color: '#28a745' }
];

export default function OrderDetailModal({ table, onClose, onOrderUpdated }) {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const { subscribe, isConnected } = useWebSocket();

  useEffect(() => {
    console.log('\n=== OrderDetailModal Mounted ===');
    console.log('Table ID:', table.id);
    console.log('WebSocket Connected:', isConnected);
    loadOrder();
  }, [table.id]);
  
  // Subscribe to order updates
  useEffect(() => {
    if (!isConnected) {
      console.log('⏳ [MODAL] WebSocket not connected yet');
      return;
    }

    const topic = `/topic/orders/${table.id}`;
    console.log('🔌 [MODAL] Subscribing to:', topic);

    const subscription = subscribe(topic, (data) => {
      console.log('\n📨 [MODAL] Received WebSocket message:');
      console.log('Topic:', topic);
      console.log('Data:', data);
      console.log('Data type:', typeof data);
      console.log('Data keys:', Object.keys(data));
      
      if (data.status === 'NO_ACTIVE_ORDER') {
        console.log('⚠️ [MODAL] No active order');
        setOrder(null);
      } else {
        console.log('✅ [MODAL] Setting order:', data);
        
        // ⭐ KIỂM TRA FIELD NAME
        if (data.orderId) {
          console.log('Order ID field: orderId =', data.orderId);
          // Map orderId → id nếu cần
          setOrder({ ...data, id: data.orderId });
        } else if (data.id) {
          console.log('Order ID field: id =', data.id);
          setOrder(data);
        } else {
          console.error('❌ No order ID found in data!');
          console.log('Available fields:', Object.keys(data));
        }
      }
    });

    return () => {
      console.log('🔌 [MODAL] Unsubscribing from:', topic);
      subscription?.unsubscribe();
    };
  }, [subscribe, table.id, isConnected]);

  const loadOrder = async () => {
    console.log('\n🔄 [MODAL] Loading order for table', table.id);
    
    try {
      setLoading(true);
      const response = await orderApi.getCurrentOrder(table.id);
      
      console.log('Response status:', response.status);
      console.log('Response data:', response.data);
      
      if (response.status === 204) {
        console.log('⚠️ [MODAL] No content (204)');
        setOrder(null);
      } else {
        console.log('✅ [MODAL] Order loaded:', response.data);
        
        // ⭐ KIỂM TRA FIELD NAME
        const orderData = response.data;
        if (orderData.orderId) {
          console.log('Mapping orderId → id');
          setOrder({ ...orderData, id: orderData.orderId });
        } else {
          setOrder(orderData);
        }
      }
    } catch (error) {
      console.error('❌ [MODAL] Error loading order:', error);
      console.error('Error response:', error.response);
      
      if (error.response?.status !== 204) {
        alert('Không thể tải thông tin đơn hàng');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    if (!order) return;

    console.log('\n🔄 [MODAL] Updating status to:', newStatus);
    console.log('Order ID:', order.id || order.orderId);

    try {
      setUpdating(true);
      const orderId = order.id || order.orderId;
      await orderApi.updateOrderStatus(orderId, newStatus);
      
      console.log('✅ [MODAL] Status updated successfully');
      
      if (onOrderUpdated) {
        onOrderUpdated();
      }
    } catch (error) {
      console.error('❌ [MODAL] Error updating status:', error);
      alert('Không thể cập nhật trạng thái');
    } finally {
      setUpdating(false);
    }
  };

  const handleReleaseTable = async () => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn giải phóng bàn ${table.id}?\nĐơn hàng sẽ được đánh dấu hoàn thành.`
    );

    if (!confirmed) return;

    console.log('\n🚪 [MODAL] Releasing table:', table.id);

    try {
      setUpdating(true);
      await tableApi.releaseTable(table.id);
      
      console.log('✅ [MODAL] Table released successfully');
      alert('Đã giải phóng bàn thành công!');
      
      if (onOrderUpdated) {
        onOrderUpdated();
      }
      onClose();
    } catch (error) {
      console.error('❌ [MODAL] Error releasing table:', error);
      alert('Không thể giải phóng bàn');
    } finally {
      setUpdating(false);
    }
  };

  const getCurrentStatus = () => {
    return STATUS_OPTIONS.find(s => s.value === order?.status) || STATUS_OPTIONS[0];
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>🍽️ Chi tiết đơn hàng - Bàn {table.id}</h2>
          <button className="close-button" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          {loading ? (
            <div className="loading-state">Đang tải...</div>
          ) : !order ? (
            <div className="empty-state">
              <p>Không có đơn hàng</p>
            </div>
          ) : (
            <>
              {/* Status Control */}
              <div className="status-control">
                <label>Trạng thái đơn hàng:</label>
                <div className="status-buttons">
                  {STATUS_OPTIONS.map(status => (
                    <button
                      key={status.value}
                      className={`status-btn ${order.status === status.value ? 'active' : ''}`}
                      style={{
                        backgroundColor: order.status === status.value ? status.color : '#f0f0f0',
                        color: order.status === status.value ? 'white' : '#666'
                      }}
                      onClick={() => handleStatusChange(status.value)}
                      disabled={updating}
                    >
                      {status.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Order Items */}
              <div className="order-items-section">
                <h3>Món đã gọi</h3>
                <div className="items-list">
                  {order.items && order.items.length > 0 ? (
                    order.items.map((item, index) => (
                      <div key={index} className="item-row">
                        <div className="item-main">
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
                    ))
                  ) : (
                    <p>Không có món nào</p>
                  )}
                </div>
              </div>

              {/* Order Notes */}
              {order.notes && (
                <div className="order-notes-section">
                  <h3>Ghi chú</h3>
                  <p>{order.notes}</p>
                </div>
              )}

              {/* Total */}
              <div className="order-total-section">
                <span>Tổng cộng:</span>
                <span className="total-price">{order.totalPrice?.toLocaleString() || '0'}đ</span>
              </div>

              {/* Order Info */}
              <div className="order-info-section">
                <div className="info-row">
                  <span>Mã đơn:</span>
                  <span>#{order.id || order.orderId}</span>
                </div>
                <div className="info-row">
                  <span>Thời gian tạo:</span>
                  <span>{order.createAt ? new Date(order.createAt).toLocaleString('vi-VN') : 'N/A'}</span>
                </div>
                <div className="info-row">
                  <span>Cập nhật:</span>
                  <span>{order.updateAt ? new Date(order.updateAt).toLocaleString('vi-VN') : 'N/A'}</span>
                </div>
              </div>
            </>
          )}
        </div>

        <div className="modal-footer">
          <button 
            className="release-btn"
            onClick={handleReleaseTable}
            disabled={updating}
          >
            🚪 Giải phóng bàn
          </button>
          <button className="cancel-btn" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}