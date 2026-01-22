import { useState, useEffect } from 'react';
import { menuApi, orderApi } from '../../services/api';
import './Menu.css';

const CATEGORIES = {
  1: { name: 'Khai vị', icon: '🥗' },
  2: { name: 'Món chính', icon: '🍜' },
  3: { name: 'Tráng miệng', icon: '🍰' },
  4: { name: 'Đồ uống', icon: '🥤' }
};

export default function Menu({ tableId, onOrderCreated }) {
  const [menuItems, setMenuItems] = useState([]);
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [orderNotes, setOrderNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [activeCategory, setActiveCategory] = useState(1);

  useEffect(() => {
    loadMenu();
  }, []);

  const loadMenu = async () => {
    try {
      setLoading(true);
      const response = await menuApi.getAvailableMenu();
      console.log('🔍 Menu API Response:', response.data);
      console.log('🔍 Total items:', response.data.length);

      const availableItems = response.data.filter(item => item.available === true);
      console.log('✅ Available items after filter:', availableItems.length);
      
      availableItems.forEach(item => {
      console.log(`Item: ${item.name}, Available: ${item.available}, Category:`, item.category);
    });
      
      setMenuItems(availableItems);
    } catch (error) {
      console.error('Error loading menu:', error);
      alert('Không thể tải menu');
    } finally {
      setLoading(false);
    }
  };

  const addToCart = (item) => {
    const existingItem = cart.find(cartItem => cartItem.menuItemId === item.id);
    
    if (existingItem) {
      setCart(cart.map(cartItem => 
        cartItem.menuItemId === item.id 
          ? { ...cartItem, quantity: cartItem.quantity + 1 }
          : cartItem
      ));
    } else {
      setCart([...cart, {
        menuItemId: item.id,
        name: item.name,
        price: item.price,
        quantity: 1,
        notes: ''
      }]);
    }
  };

  const updateQuantity = (menuItemId, newQuantity) => {
    if (newQuantity === 0) {
      setCart(cart.filter(item => item.menuItemId !== menuItemId));
    } else {
      setCart(cart.map(item => 
        item.menuItemId === menuItemId 
          ? { ...item, quantity: newQuantity }
          : item
      ));
    }
  };

  const updateItemNotes = (menuItemId, notes) => {
    setCart(cart.map(item => 
      item.menuItemId === menuItemId 
        ? { ...item, notes }
        : item
    ));
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const handleSubmitOrder = async () => {
    if (cart.length === 0) {
      alert('Giỏ hàng trống!');
      return;
    }

    try {
      setSubmitting(true);
      
      const orderData = {
        tableId: tableId || parseInt(localStorage.getItem('currentTableId')),
        items: cart.map(item => ({
          menuItemId: item.menuItemId,
          quantity: item.quantity,
          notes: item.notes || null
        })),
        notes: orderNotes || null
      };

      console.log('🔍 Submitting order:', orderData);
      
      const response = await orderApi.createOrder(orderData);
      console.log('✅ Order created:', response.data);
      
      alert('Đặt món thành công! 🎉');
      setCart([]);
      setOrderNotes('');
      
      if (onOrderCreated) {
        onOrderCreated(response.data);
      }
    } catch (error) {
      console.error('❌ Error creating order:', error);
      console.error('Response:', error.response?.data);
      alert(`Không thể đặt món: ${error.response?.data?.message || error.message}`);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="menu-container"><h2>Đang tải menu...</h2></div>;
  }

  console.log('🔍 RENDER - menuItems:', menuItems);
  console.log('🔍 RENDER - menuItems.length:', menuItems.length);

  return (
    <div className="menu-container">
      <h1>🍽️ Thực đơn</h1>
      
      <div className="category-tabs">
        {Object.entries(CATEGORIES).map(([id, category]) => (
          <button
            key={id}
            className={`category-tab ${activeCategory === parseInt(id) ? 'active' : ''}`}
            onClick={() => setActiveCategory(parseInt(id))}
          >
            {category.icon} {category.name}
          </button>
        ))}
      </div>
      
      <div className="menu-content">
        <div className="menu-section">
          {menuItems.length === 0 ? (
            <div className="empty-category">
              <p>Không có món nào</p>
            </div>
          ) : (
            <div className="category-section">
              <div className="menu-grid">
                {menuItems
                  .filter(item => item.category?.id === activeCategory)
                  .map(item => (
                    <div key={item.id} className="menu-item-card">
                      {item.image && (
                        <div className="menu-item-image">
                          <img 
                            src={item.image} 
                            alt={item.name}
                            onError={(e) => {
                              console.error('Failed to load image:', item.image);
                              e.target.style.display = 'none';
                            }}
                          />
                        </div>
                      )}
                      <div className="item-info">
                        <h3>{item.name}</h3>
                        <p className="item-description">{item.description}</p>
                        <p className="item-price">{item.price.toLocaleString()}đ</p>
                      </div>
                      <button 
                        className="add-btn"
                        onClick={() => addToCart(item)}
                      >
                        + Thêm
                      </button>
                    </div>
                  ))}
              </div>
              {menuItems.filter(item => item.category?.id === activeCategory).length === 0 && (
                <div className="empty-category">
                  <p>Chưa có món nào trong danh mục này</p>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="cart-section">
          <h2>🛒 Giỏ hàng</h2>
          
          {cart.length === 0 ? (
            <p className="empty-cart">Giỏ hàng trống</p>
          ) : (
            <>
              <div className="cart-items">
                {cart.map(item => (
                  <div key={item.menuItemId} className="cart-item">
                    <div className="item-name">{item.name}</div>
                    <div className="item-price">{item.price.toLocaleString()}đ</div>
                    
                    <div className="quantity-controls">
                      <button onClick={() => updateQuantity(item.menuItemId, item.quantity - 1)}>
                        -
                      </button>
                      <span>{item.quantity}</span>
                      <button onClick={() => updateQuantity(item.menuItemId, item.quantity + 1)}>
                        +
                      </button>
                    </div>
                    
                    <input
                      type="text"
                      placeholder="Ghi chú (vd: không cay)"
                      value={item.notes}
                      onChange={(e) => updateItemNotes(item.menuItemId, e.target.value)}
                      className="item-notes"
                    />
                    
                    <div className="item-total">
                      {(item.price * item.quantity).toLocaleString()}đ
                    </div>
                  </div>
                ))}
              </div>

              <textarea
                placeholder="Ghi chú cho đơn hàng..."
                value={orderNotes}
                onChange={(e) => setOrderNotes(e.target.value)}
                className="order-notes"
              />

              <div className="cart-total">
                <span>Tổng cộng:</span>
                <span className="total-amount">{calculateTotal().toLocaleString()}đ</span>
              </div>

              <button 
                className="submit-order-btn"
                onClick={handleSubmitOrder}
                disabled={submitting}
              >
                {submitting ? 'Đang gửi...' : 'Gửi đơn hàng'}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}