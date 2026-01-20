package com.example.restaurant.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.TableRepository;

@Service
public class TableService {
    private final TableRepository tableRepository;
    private final OrderRepository orderRepository; 
    private final WebSocketNotificationService wsNotificationService;


    public TableService(TableRepository tableRepository1, OrderRepository orderRepository1, WebSocketNotificationService wsNotificationService1){
        this.tableRepository = tableRepository1;
        this.orderRepository = orderRepository1;
        this.wsNotificationService = wsNotificationService1;
    }

    public TableEntity getTableById(Integer id){
        return tableRepository.findById(id).orElseThrow(()-> new RuntimeException("id của bàn không tồn tại"));
    }

    public List<TableEntity> getAllTableByCapacity(Integer capacity){
        return tableRepository.findAllByCapacity(capacity);
    }

    public List<TableEntity> getAllTableByStatus(String status){
        return tableRepository.findAllByStatus(status);
    }

    public List<TableEntity> getAllTables() {
    return tableRepository.findAll();
}


    @Transactional
    public TableEntity createTable(TableEntity TE){
        if(TE.getCapacity() == null || TE.getCapacity() <= 0){
            throw new IllegalArgumentException("Sức chứa của bàn không hợp lệ");
        }
        TableEntity newTable =  tableRepository.save(TE);
        wsNotificationService.sendTableStatusUpdate();
        return newTable;
    }

    @Transactional
    public void deleteTable(Integer id){
        if(!tableRepository.existsById(id)){
            throw new RuntimeException("id của bản không hợp lệ");
        }
        tableRepository.deleteById(id);
        wsNotificationService.sendTableStatusUpdate();
    }

    @Transactional
    public TableEntity updateTable(Integer id, TableEntity TE){
        TableEntity oldTE = getTableById(id);
        boolean statusChanged = false;
        if(TE.getCapacity() != null && TE.getCapacity() > 0){
              oldTE.setCapacity(TE.getCapacity());
        }
        if(TE.getStatus() != null && !TE.getStatus().equals(oldTE.getStatus())){
            oldTE.setStatus(TE.getStatus());
            statusChanged = true;
        }
        TableEntity updated = tableRepository.save(oldTE);
        if(statusChanged) {
            wsNotificationService.sendTableStatusUpdate();
        }
        return updated;
    }

    @Transactional
    public TableEntity occupyTable(Integer tableId) {
        TableEntity table = getTableById(tableId);
        
        if (!"AVAILABLE".equals(table.getStatus())) {
            throw new RuntimeException("Bàn không khả dụng");
        }
        
        table.setStatus("OCCUPIED");
        TableEntity updated = tableRepository.save(table);
        
        wsNotificationService.sendTableStatusUpdate();
        
        return updated;
    }

    @Transactional
    public void releaseTable(Integer tableId) {
        TableEntity table = getTableById(tableId);
        
        List<Order> activeOrders = orderRepository.findByTableIdAndStatusNot(tableId, "COMPLETED");
        for (Order order : activeOrders) {
            order.setStatus("COMPLETED");
        }
        orderRepository.saveAll(activeOrders);
        
        table.setStatus("AVAILABLE");
        tableRepository.save(table);
        
        wsNotificationService.sendTableStatusUpdate();
        wsNotificationService.sendOrderUpdate(tableId); // Clear order cho khách
        wsNotificationService.sendAdminDashboardUpdate();
    }
}
