import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../api/axiosInstance';

interface Table {
  id: number;
  capacity: number;
  status: 'AVAILABLE' | 'OCCUPIED';
  currentOrderId?: number | null;  // thêm từ TableResponse
  totalPrice?: number;             // BigDecimal → number ở JS (hoặc string nếu cần định dạng)
}

interface TableStatusUpdate {
  tables: Table[];
}

export function useTablesWebSocket() {
  const [tables, setTables] = useState<Table[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadTables = async () => {
      try {
        const res = await api.get('/table');  // giữ path backend của bạn
        console.log('Initial fetch tables từ REST:', res.data);
        setTables(res.data?.tables || res.data || []);  // phòng trường hợp backend trả {tables: [...]} hoặc array trực tiếp
      } catch (err) {
        console.error('Lỗi initial fetch:', err);
      } finally {
        setLoading(false);
      }
    };
    loadTables();

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => console.log('STOMP DEBUG:', str),  // thêm debug STOMP chi tiết
    });

    client.onConnect = () => {
      console.log('WebSocket CONNECTED - Bắt đầu subscribe');

      client.subscribe('/topic/tables', (msg) => {
        console.log('NHẬN MESSAGE RAW:', msg);  // log toàn bộ message object
        console.log('BODY RAW:', msg.body);  // JSON string từ backend

        if (msg.body) {
          try {
            const update: TableStatusUpdate = JSON.parse(msg.body);
            console.log('PARSE THÀNH CÔNG - New list có', update.tables?.length || 0, 'bàn');
            console.log('Sample bàn đầu tiên:', update.tables?.[0]);  // xem full field (currentOrderId, totalPrice)
            setTables(update.tables || []);
          } catch (parseErr) {
            console.error('PARSE JSON LỖI (DTO không match?):', parseErr);
            console.error('Body gây lỗi:', msg.body);
          }
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP ERROR:', frame);
    };

    client.onWebSocketError = (err) => {
      console.error('WebSocket ERROR:', err);
    };

    client.activate();

    return () => {
      client.deactivate();
      console.log('WebSocket deactivated');
    };
  }, []);

  return { tables, loading };
}