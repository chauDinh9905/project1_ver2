import { useTablesWebSocket } from '../../hooks/useTablesWebSocket';
import api from '../../api/axiosInstance';

function TableSelection() {
  const { tables, loading } = useTablesWebSocket();

  const occupyTable = async (tableId: number) => {
    try {
      await api.post(`/table/${tableId}/occupy`);
      localStorage.setItem('tableId', tableId.toString());
      alert(`Chọn thành công bàn ${tableId} 🍽️`);
    } catch (err: any) {
      alert('Bàn đã bị chiếm hoặc lỗi server!');
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-white">
        <span className="loading loading-spinner loading-lg text-orange-500"></span>
      </div>
    );
  }

  return (
    /* w-full và overflow-x-hidden là chốt chặn cuối cùng cho khoảng trắng thừa */
    <div className="min-h-screen w-full overflow-x-hidden bg-[#fcf9f5] py-10">
      
      {/* Header - Căn giữa tuyệt đối */}
      <header className="w-full px-4 mb-12 text-center">
        <h1 className="text-4xl md:text-6xl font-black text-gray-800 tracking-tight">
          Chọn Bàn Ăn
        </h1>
        <div className="h-1.5 w-20 bg-orange-500 mx-auto mt-4 rounded-full"></div>
      </header>

      {/* Main Content Area */}
      <main className="w-full max-w-7xl mx-auto px-4 md:px-6">
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4 md:gap-6">
          {tables?.map((table) => {
            const isAvailable = table.status === 'AVAILABLE';
            return (
              <button
                key={table.id}
                onClick={() => isAvailable && occupyTable(table.id)}
                disabled={!isAvailable}
                className={`
                  relative aspect-square w-full rounded-2xl flex flex-col items-center justify-center
                  transition-all duration-300 border-b-4 active:border-b-0 active:translate-y-1
                  ${isAvailable 
                    ? 'bg-white border-gray-200 hover:border-orange-300 shadow-sm hover:shadow-xl' 
                    : 'bg-gray-100 border-gray-200 opacity-60'
                  }
                `}
              >
                <span className="text-3xl md:text-5xl mb-2">{isAvailable ? '🍽️' : '🔒'}</span>
                <span className="text-lg md:text-xl font-bold text-gray-700">Bàn {table.id}</span>
                <span className="text-xs md:text-sm text-gray-400">{table.capacity} chỗ</span>
                
                {/* Badge trạng thái nhỏ */}
                <div className={`mt-2 px-2 py-0.5 rounded text-[10px] font-bold ${isAvailable ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'}`}>
                  {isAvailable ? 'TRỐNG' : 'HẾT'}
                </div>
              </button>
            );
          })}
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full text-center mt-16 px-4">
        <p className="text-gray-400 text-sm">© 2024 Nhà hàng XYZ - Realtime System</p>
      </footer>
    </div>
  );
}

export default TableSelection;