import { useState, useEffect } from 'react';
import { menuApi } from '../../services/api';
import './MenuManagement.css';

const CATEGORIES = [
  { id: 1, name: 'Khai vị' },
  { id: 2, name: 'Món chính' },
  { id: 3, name: 'Tráng miệng' },
  { id: 4, name: 'Đồ uống' }
];

export default function MenuManagement({ onBack }) {
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    image: '',
    available: true,
    category: { id: 1 }
  });

  useEffect(() => {
    loadMenuItems();
  }, []);

  const loadMenuItems = async () => {
    try {
      setLoading(true);
      const response = await menuApi.getAllMenu();
      setMenuItems(response.data);
    } catch (error) {
      console.error('Error loading menu:', error);
      alert('Không thể tải menu');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    if (name === 'categoryId') {
      setFormData({
        ...formData,
        category: { id: parseInt(value) }
      });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? checked : value
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const submitData = {
      ...formData,
      price: parseFloat(formData.price)
    };

    try {
      if (editingItem) {
        await menuApi.updateMenuItem(editingItem.id, submitData);
        alert('Cập nhật món thành công!');
      } else {
        await menuApi.createMenuItem(submitData);
        alert('Thêm món mới thành công!');
      }

      resetForm();
      loadMenuItems();
    } catch (error) {
      console.error('Error saving menu item:', error);
      alert('Không thể lưu món ăn. Vui lòng kiểm tra lại thông tin!');
    }
  };

  const handleEdit = (item) => {
    setEditingItem(item);
    setFormData({
      name: item.name,
      description: item.description || '',
      price: item.price.toString(),
      image: item.image || '',
      available: item.available,
      category: { id: item.category.id }
    });
    setShowForm(true);
  };

  const handleDelete = async (item) => {
    const confirmed = window.confirm(`Bạn có chắc muốn xóa món "${item.name}"?`);
    if (!confirmed) return;

    try {
      await menuApi.deleteMenuItem(item.id);
      alert('Xóa món thành công!');
      loadMenuItems();
    } catch (error) {
      console.error('Error deleting item:', error);
      alert('Không thể xóa món ăn!');
    }
  };

  const handleToggleAvailable = async (item) => {
    try {
      await menuApi.toggleAvailable(item.id);
      loadMenuItems();
    } catch (error) {
      console.error('Error toggling available:', error);
      alert('Không thể thay đổi trạng thái!');
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      price: '',
      image: '',
      available: true,
      category: { id: 1 }
    });
    setEditingItem(null);
    setShowForm(false);
  };

  const groupedItems = menuItems.reduce((acc, item) => {
    const catId = item.category.id;
    if (!acc[catId]) acc[catId] = [];
    acc[catId].push(item);
    return acc;
  }, {});

  if (loading) {
    return <div className="menu-management"><h2>Đang tải...</h2></div>;
  }

  return (
    <div className="menu-management">
      <div className="management-header">
        <div>
          <h1>🍽️ Quản lý Menu</h1>
          <p>Thêm, sửa, xóa món ăn</p>
        </div>
        <div className="header-actions">
          <button className="add-btn" onClick={() => setShowForm(!showForm)}>
            {showForm ? '✕ Hủy' : '➕ Thêm món mới'}
          </button>
          <button className="back-btn" onClick={onBack}>
            ← Quay lại Dashboard
          </button>
        </div>
      </div>

      {showForm && (
        <div className="menu-form-container">
          <h2>{editingItem ? '✏️ Sửa món' : '➕ Thêm món mới'}</h2>
          <div className="menu-form">
            <div className="form-row">
              <div className="form-group">
                <label>Tên món *</label>
                <input
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="Vd: Phở Bò"
                  required
                />
              </div>

              <div className="form-group">
                <label>Giá (VNĐ) *</label>
                <input
                  name="price"
                  type="number"
                  value={formData.price}
                  onChange={handleInputChange}
                  placeholder="50000"
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Danh mục *</label>
                <select
                  name="categoryId"
                  value={formData.category.id}
                  onChange={handleInputChange}
                >
                  {CATEGORIES.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>URL Hình ảnh</label>
                <input
                  name="image"
                  value={formData.image}
                  onChange={handleInputChange}
                  placeholder="https://..."
                />
              </div>
            </div>

            <div className="form-group">
              <label>Mô tả</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="Mô tả về món ăn..."
                rows="3"
              />
            </div>

            <div className="form-group checkbox-group">
              <label>
                <input
                  type="checkbox"
                  name="available"
                  checked={formData.available}
                  onChange={handleInputChange}
                />
                <span>Đang bán</span>
              </label>
            </div>

            <div className="form-actions">
              <button className="submit-btn" onClick={handleSubmit}>
                {editingItem ? '💾 Cập nhật' : '➕ Thêm món'}
              </button>
              <button className="cancel-form-btn" onClick={resetForm}>
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="menu-list">
        {CATEGORIES.map(category => {
          const items = groupedItems[category.id] || [];
          if (items.length === 0) return null;

          return (
            <div key={category.id} className="category-group">
              <h2>{category.name} ({items.length})</h2>
              <div className="items-grid">
                {items.map(item => (
                  <div key={item.id} className="menu-item-card-admin">
                    <div className="item-header">
                      <h3>{item.name}</h3>
                      <span className={`status-badge ${item.available ? 'available' : 'unavailable'}`}>
                        {item.available ? '✅ Đang bán' : '❌ Hết'}
                      </span>
                    </div>

                    {item.description && (
                      <p className="item-description">{item.description}</p>
                    )}

                    <div className="item-price-tag">{item.price.toLocaleString()}đ</div>

                    <div className="item-actions">
                      <button
                        className="toggle-btn"
                        onClick={() => handleToggleAvailable(item)}
                      >
                        {item.available ? '🚫' : '✅'}
                      </button>
                      <button className="edit-btn" onClick={() => handleEdit(item)}>
                        ✏️ Sửa
                      </button>
                      <button className="delete-btn" onClick={() => handleDelete(item)}>
                        🗑️ Xóa
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}