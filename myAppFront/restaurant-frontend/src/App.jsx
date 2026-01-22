import { useState, useEffect } from 'react';
import TableSelection from './components/customer/TableSelection.jsx';
import Menu from './components/customer/Menu.jsx';
import OrderStatus from './components/customer/OrderStatus.jsx';
import AdminLogin from './components/admin/Login.jsx';
import AdminDashboard from './components/admin/Dashboard.jsx';
import MenuManagement from './components/admin/MenuManagement.jsx';
import TableManagement from './components/admin/TableManagement.jsx';
import { tableApi } from './services/api';
import authService from './services/authService';
import './App.css';

function App() {
  const [appMode, setAppMode] = useState('customer'); // 'customer' or 'admin'
  const [currentView, setCurrentView] = useState('table-selection');
  const [selectedTableId, setSelectedTableId] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Setup axios interceptor khi app start
  useEffect(() => {
    authService.setupAxiosInterceptor();
    setIsAuthenticated(authService.isAuthenticated());
  }, []);

  // Cleanup khi unmount hoặc reload page
  useEffect(() => {
    const handleBeforeUnload = async (e) => {
      const tableId = localStorage.getItem('currentTableId');
      if (tableId && appMode === 'customer') {
        const data = JSON.stringify({ tableId: parseInt(tableId) });
        navigator.sendBeacon('/api/tables/' + tableId + '/release', data);
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [appMode]);

  // Customer handlers
  const handleTableSelected = (tableId) => {
    setSelectedTableId(tableId);
    setCurrentView('customer-menu');  // ⭐ ĐỔI TÊN
  };

  const handleOrderCreated = () => {
    setCurrentView('order-status');
  };

  const handleBackToMenu = () => {
    setCurrentView('customer-menu');  // ⭐ ĐỔI TÊN
  };

  const handleAddMoreItems = () => {
    setCurrentView('customer-menu');  // ⭐ ĐỔI TÊN
  };

  const handleBackToTables = async () => {
    const oldTableId = localStorage.getItem('currentTableId');
    
    const confirmed = window.confirm(
      'Bạn có chắc muốn đổi bàn? Bàn hiện tại sẽ được giải phóng.'
    );
    
    if (!confirmed) return;
    
    if (oldTableId) {
      try {
        await tableApi.releaseTable(parseInt(oldTableId));
        console.log(`Released table ${oldTableId}`);
      } catch (error) {
        console.error('Error releasing table:', error);
      }
    }
    
    localStorage.removeItem('currentTableId');
    setSelectedTableId(null);
    setCurrentView('table-selection');
  };

  // Admin handlers
  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
    setCurrentView('dashboard');
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setCurrentView('login');
  };

  const handleAdminNavigate = (view) => {
    setCurrentView(view);  // 'dashboard' hoặc 'menu'
  };

  // Switch between customer and admin mode
  const switchToAdmin = () => {
    setAppMode('admin');
    setCurrentView(isAuthenticated ? 'dashboard' : 'login');
  };

  const switchToCustomer = () => {
    setAppMode('customer');
    setCurrentView('table-selection');
  };

  return (
    <div className="app">
      {/* Navigation Bar */}
      <nav className="navbar">
        <h1 className="app-title">
          {appMode === 'admin' ? '🔐 Admin Panel' : '🍽️ Nhà hàng ABC'}
        </h1>
        <div className="nav-buttons">
          {/* Mode Switcher */}
          {appMode === 'customer' && currentView === 'table-selection' && (
            <button onClick={switchToAdmin} className="nav-btn admin-mode">
              🔐 Quản trị
            </button>
          )}

          {appMode === 'admin' && currentView === 'login' && (
            <button onClick={switchToCustomer} className="nav-btn">
              ← Về trang khách
            </button>
          )}

          {/* Customer Navigation */}
          {appMode === 'customer' && currentView !== 'table-selection' && (
            <>
              {currentView === 'order-status' && (
                <button onClick={handleBackToMenu} className="nav-btn">
                  📋 Về Menu
                </button>
              )}
              {currentView === 'customer-menu' && (  // ⭐ ĐỔI TÊN
                <button onClick={() => setCurrentView('order-status')} className="nav-btn">
                  📊 Xem trạng thái
                </button>
              )}
              <button onClick={handleBackToTables} className="nav-btn danger">
                🚪 Đổi bàn
              </button>
            </>
          )}
        </div>
      </nav>

      {/* Main Content */}
      <main className="main-content">
        {/* CUSTOMER MODE */}
        {appMode === 'customer' && (
          <>
            {currentView === 'table-selection' && (
              <TableSelection onTableSelected={handleTableSelected} />
            )}

            {currentView === 'customer-menu' && (  // ⭐ ĐỔI TÊN
              <Menu 
                tableId={selectedTableId} 
                onOrderCreated={handleOrderCreated}
              />
            )}

            {currentView === 'order-status' && (
              <OrderStatus 
                tableId={selectedTableId}
                onAddMoreItems={handleAddMoreItems}
              />
            )}
          </>
        )}

        {/* ADMIN MODE */}
        {appMode === 'admin' && (
          <>
            {currentView === 'login' && (
              <AdminLogin onLoginSuccess={handleLoginSuccess} />
            )}

            {currentView === 'dashboard' && isAuthenticated && (
              <AdminDashboard 
                onNavigate={handleAdminNavigate}
                onLogout={handleLogout}
              />
            )}

            {currentView === 'menu' && isAuthenticated && (  // ⭐ ADMIN MENU GIỮ NGUYÊN
              <MenuManagement 
                onBack={() => setCurrentView('dashboard')}
              />
            )}

            {currentView === 'tables' && isAuthenticated && (
              <TableManagement 
                onBack={() => setCurrentView('dashboard')}
              />
            )}
          </>
        )}
      </main>
    </div>
  );
}

export default App;