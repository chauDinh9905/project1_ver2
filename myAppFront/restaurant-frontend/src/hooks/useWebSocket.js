import { useEffect, useRef, useCallback, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = 'http://localhost:8080/ws';
const RECONNECT_DELAY = 3000;
const MAX_RECONNECT_ATTEMPTS = 5;

export const useWebSocket = () => {
  const clientRef = useRef(null);
  const subscriptionsRef = useRef({});
  const reconnectAttemptsRef = useRef(0);
  const [connectionStatus, setConnectionStatus] = useState('DISCONNECTED');

  const connect = useCallback(() => {
    if (clientRef.current?.active) {
      console.log('WebSocket already connected');
      return;
    }

    setConnectionStatus('CONNECTING');

    const client = new Client({
      webSocketFactory: () => new SockJS(SOCKET_URL),
      
      onConnect: () => {
        console.log('✅ WebSocket Connected');
        setConnectionStatus('CONNECTED');
        reconnectAttemptsRef.current = 0;

        // Re-subscribe to all previous topics
        Object.entries(subscriptionsRef.current).forEach(([topic, callback]) => {
          if (clientRef.current?.connected) {
            clientRef.current.subscribe(topic, (message) => {
              try {
                const data = JSON.parse(message.body);
                callback(data);
              } catch (error) {
                console.error('Error parsing message:', error);
              }
            });
          }
        });
      },

      onDisconnect: () => {
        console.log('❌ WebSocket Disconnected');
        setConnectionStatus('DISCONNECTED');
        // Auto reconnect after 3 seconds
        handleReconnect();
      },

      onStompError: (frame) => {
        console.error('WebSocket Error:', frame);
        setConnectionStatus('ERROR');
        handleReconnect();
      },

      // Disable debug in production
      debug: (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('STOMP:', str);
        }
      }
    });

    client.activate();
    clientRef.current = client;
  }, []);

  const handleReconnect = useCallback(() => {
    if (reconnectAttemptsRef.current >= MAX_RECONNECT_ATTEMPTS) {
      console.error('Max reconnect attempts reached');
      setConnectionStatus('FAILED');
      return;
    }

    reconnectAttemptsRef.current += 1;
    console.log(`Reconnecting... (${reconnectAttemptsRef.current}/${MAX_RECONNECT_ATTEMPTS})`);
    
    setTimeout(() => {
      connect();
    }, RECONNECT_DELAY);
  }, [connect]);

  const subscribe = useCallback((topic, callback) => {
    if (!clientRef.current?.connected) {
      console.warn('WebSocket not connected yet. Saving subscription...');
      subscriptionsRef.current[topic] = callback;
      return null;
    }

    try {
      const subscription = clientRef.current.subscribe(topic, (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      });

      subscriptionsRef.current[topic] = callback;
      console.log('✅ Subscribed to:', topic);
      return subscription;
    } catch (error) {
      console.error('Subscribe error:', error);
      return null;
    }
  }, []);

  const unsubscribe = useCallback((topic) => {
    delete subscriptionsRef.current[topic];
    console.log('Unsubscribed from:', topic);
  }, []);

  const disconnect = useCallback(() => {
    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
      subscriptionsRef.current = {};
      setConnectionStatus('DISCONNECTED');
      console.log('WebSocket manually disconnected');
    }
  }, []);

  // Auto-connect on mount
  useEffect(() => {
    connect();
    return () => disconnect();
  }, [connect, disconnect]);

  return {
    subscribe,
    unsubscribe,
    disconnect,
    reconnect: connect,
    connectionStatus,
    isConnected: connectionStatus === 'CONNECTED'
  };
};