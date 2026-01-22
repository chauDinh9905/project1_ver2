import { useState, useEffect } from 'react';
import { tableApi } from '../../services/api';
import './TableManagement.css';

export default function TableManagement({ onBack }) {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingTable, setEditingTable] = useState(null);
  const [formData, setFormData] = useState({
    capacity: 4,
    status: 'AVAILABLE'
  });

  useEffect(() => {
    loadTables();
  }, []);

  const loadTables = async () => {
    try {
      setLoading(true);
      const response = await tableApi.getAllTables();
      console.log('📊 Tables loaded:', response.data);
      setTables(response.data);
    } catch (error) {
      console.error('Error loading tables:', error);
      alert('Không thể tải danh sách bàn');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: name === 'capacity' ? parseInt(value) : value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.capacity < 1 || formData.capacity > 20) {
      alert('Số chỗ ngồi phải từ 1 đến 20');
      return;
    }

    try {
      if (editingTable) {
        await tableApi.updateTable(editingTable.id, formData);
        alert('Cập nhật bàn thành công!');
      } else {
        await tableApi.createTable(formData);
        alert('Thêm bàn mới thành công!');
      }

      resetForm();
      loadTables();
    } catch (error) {
      console.error('Error saving table:', error);
      alert('Không thể lưu bàn. Vui lòng kiểm tra lại!');
    }
  };

  const handleEdit = (table) => {
    setEditingTable(table);
    setFormData({
      capacity: table.capacity,
      status: table.status
    });
    setShowForm(true);
  };

  const handleDelete = async (table) => {
    if (table.status === 'OCCUPIED') {
      alert('Không thể xóa bàn đang có khách!');
      return;
    }

    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa Bàn ${table.id}?\nSố chỗ: ${table.capacity}`
    );
    
    if (!confirmed) return;

    try {
      await tableApi.deleteTable(table.id);
      alert('Xóa bàn thành công!');
      loadTables();
    } catch (error) {
      console.error('Error deleting table:', error);
      alert('Không thể xóa bàn!');
    }
  };

  const resetForm = () => {
    setFormData({
      capacity: 4,
      status: 'AVAILABLE'
    });
    setEditingTable(null);
    setShowForm(false);
  };

  const getTableStats = () => {
    const total = tables.length;
    const occupied = tables.filter(t => t.status === 'OCCUPIED').length;
    const available = tables.filter(t => t.status === 'AVAILABLE').length;
    
    return { total, occupied, available };
  };

  const stats = getTableStats();

  if (loading) {
    return <div className="table-management"><h2>Đang tải...</h2></div>;
  }

  return (
    <div className="table-management">
      <div className="management-header">
        <div>
          <h1>🪑 Quản lý Bàn</h1>
          <p>Thêm, sửa, xóa bàn ăn</p>
        </div>
        <div className="header-actions">
          <button className="add-btn" onClick={() => setShowForm(!showForm)}>
            {showForm ? '✕ Hủy' : '➕ Thêm bàn mới'}
          </button>
          <button className="back-btn" onClick={onBack}>
            ← Quay lại Dashboard
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-row">
        <div className="stat-card">
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
            <div className="stat-label">Đang sử dụng</div>
          </div>
        </div>

        <div className="stat-card available">
          <div className="stat-icon">✅</div>
          <div className="stat-info">
            <div className="stat-value">{stats.available}</div>
            <div className="stat-label">Còn trống</div>
          </div>
        </div>
      </div>

      {/* Form thêm/sửa bàn */}
      {showForm && (
        <div className="table-form-container">
          <h2>{editingTable ? '✏️ Sửa bàn' : '➕ Thêm bàn mới'}</h2>
          <div className="table-form">
            <div className="form-row">
              <div className="form-group">
                <label>Số chỗ ngồi *</label>
                <input
                  name="capacity"
                  type="number"
                  min="1"
                  max="20"
                  value={formData.capacity}
                  onChange={handleInputChange}
                  placeholder="4"
                  required
                />
                <small>Từ 1 đến 20 chỗ</small>
              </div>

              <div className="form-group">
                <label>Trạng thái *</label>
                <select
                  name="status"
                  value={formData.status}
                  onChange={handleInputChange}
                >
                  <option value="AVAILABLE">✅ Trống</option>
                  <option value="OCCUPIED">🔴 Đang dùng</option>
                </select>
              </div>
            </div>

            <div className="form-actions">
              <button className="submit-btn" onClick={handleSubmit}>
                {editingTable ? '💾 Cập nhật' : '➕ Thêm bàn'}
              </button>
              <button className="cancel-form-btn" onClick={resetForm}>
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Danh sách bàn */}
      <div className="tables-list">
        <h2>Danh sách bàn ({tables.length})</h2>
        <div className="tables-grid">
          {tables.map(table => (
            <div 
              key={table.id} 
              className={`table-card ${table.status.toLowerCase()}`}
            >
              <div className="table-header">
                <h3>Bàn {table.id}</h3>
                <span className={`status-badge ${table.status.toLowerCase()}`}>
                  {table.status === 'AVAILABLE' ? '✅ Trống' : '🔴 Đang dùng'}
                </span>
              </div>

              <div className="table-info">
                <div className="info-item">
                  <span className="info-label">👥 Số chỗ:</span>
                  <span className="info-value">{table.capacity}</span>
                </div>
                
                {table.currentOrderId && (
                  <div className="info-item">
                    <span className="info-label">📋 Đơn:</span>
                    <span className="info-value">#{table.currentOrderId}</span>
                  </div>
                )}

                {table.totalPrice > 0 && (
                  <div className="info-item">
                    <span className="info-label">💰 Tổng:</span>
                    <span className="info-value price">
                      {table.totalPrice.toLocaleString()}đ
                    </span>
                  </div>
                )}
              </div>

              <div className="table-actions">
                <button 
                  className="edit-btn" 
                  onClick={() => handleEdit(table)}
                  disabled={table.status === 'OCCUPIED'}
                  title={table.status === 'OCCUPIED' ? 'Không thể sửa bàn đang có khách' : 'Sửa bàn'}
                >
                  ✏️ Sửa
                </button>
                <button 
                  className="delete-btn" 
                  onClick={() => handleDelete(table)}
                  disabled={table.status === 'OCCUPIED'}
                  title={table.status === 'OCCUPIED' ? 'Không thể xóa bàn đang có khách' : 'Xóa bàn'}
                >
                  🗑️ Xóa
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}