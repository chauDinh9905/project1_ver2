import { useState, useEffect } from 'react';
import { useWebSocket } from '../../hooks/useWebSocket';
import { tableApi } from '../../services/api';
import './TableSelection.css';

export default function TableSelection({ onTableSelected }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const { subscribe, isConnected } = useWebSocket();

  // Lấy danh sách bàn lần đầu
  useEffect(() => {
    loadTables();
  }, []);

  // Subscribe WebSocket để cập nhật real-time
  useEffect(() => {
    const subscription = subscribe('/topic/tables', (data) => {
      console.log('Table update received:', data);
      if (data.tables) {
        setTables(data.tables);
      }
    });

    return () => subscription?.unsubscribe();
  }, [subscribe]);

  const loadTables = async () => {
    try {
      setLoading(true);
      const response = await tableApi.getAllTables();
      setTables(response.data);
    } catch (error) {
      console.error('Error loading tables:', error);
      alert('Không thể tải danh sách bàn');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectTable = async (table) => {
    if (table.status !== 'AVAILABLE') {
      alert('Bàn này đang có người ngồi!');
      return;
    }

    try {
      await tableApi.occupyTable(table.id);
      localStorage.setItem('currentTableId', table.id);
      
      // Callback để chuyển sang trang menu
      if (onTableSelected) {
        onTableSelected(table.id);
      }
    } catch (error) {
      console.error('Error occupying table:', error);
      alert('Không thể chọn bàn này. Vui lòng thử lại!');
    }
  };

  if (loading) {
    return (
      <div className="table-selection-container">
        <h1>Đang tải...</h1>
      </div>
    );
  }

  return (
    <div className="table-selection-container">
      <div className="header">
        <h1>Chọn bàn của bạn</h1>
        <div className={`connection-status ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? 'Kết nối' : 'Mất kết nối'}
        </div>
      </div>

      <div className="legend">
        <div className="legend-item">
          <div className="legend-color available"></div>
          <span>Bàn trống</span>
        </div>
        <div className="legend-item">
          <div className="legend-color occupied"></div>
          <span>Đang sử dụng</span>
        </div>
      </div>

      <div className="tables-grid">
        {tables.map((table) => (
          <button
            key={table.id}
            className={`table-card ${table.status.toLowerCase()}`}
            onClick={() => handleSelectTable(table)}
            disabled={table.status !== 'AVAILABLE'}
          >
            <div className="table-number">Bàn {table.id}</div>
            <div className="table-capacity">
              👥 {table.capacity} người
            </div>
            <div className="table-status">
              {table.status === 'AVAILABLE' ? 'Trống' : 'Đang sử dụng'}
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}