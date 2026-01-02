package com.example.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.repository.TableRepository;

@Service
public class TableService {
    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository1){
        this.tableRepository = tableRepository1;
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

    @Transactional
    public TableEntity createTable(TableEntity TE){
        if(TE.getCapacity() == null || TE.getCapacity() <= 0){
            throw new IllegalArgumentException("Sức chứa của bàn không hợp lệ");
        }
        return tableRepository.save(TE);
    }

    @Transactional
    public void deleteTable(Integer id){
        if(!tableRepository.existsById(id)){
            throw new RuntimeException("id của bản không hợp lệ");
        }
        tableRepository.deleteById(id);
    }

    @Transactional
    public TableEntity updateTable(Integer id, TableEntity TE){
        TableEntity oldTE = getTableById(id);
        if(TE.getCapacity() != null && TE.getCapacity() > 0){
              oldTE.setCapacity(TE.getCapacity());
        }
        if(TE.getStatus() != null){
            oldTE.setStatus(TE.getStatus());
        }
        return tableRepository.save(oldTE);
    }
}
