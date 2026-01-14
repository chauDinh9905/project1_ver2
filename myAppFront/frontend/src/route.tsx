import { Routes, Route } from 'react-router-dom';
import TableSelection from './pages/customer/TableSelection';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<TableSelection />} />  {/* Trang chủ */}
      {/* Thêm route sau này ở đây */}
    </Routes>
  );
}

export default AppRoutes;