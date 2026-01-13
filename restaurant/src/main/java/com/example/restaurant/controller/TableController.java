package com.example.restaurant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.service.TableService;

@RestController
@RequestMapping("/api/table")
@CrossOrigin(origins = "*")
public class TableController {
    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableEntity> getAllTables() {
        return tableService.getAllTables();
    }

    @GetMapping("/available")
    public List<TableEntity> getAvailableTables() {
        List<TableEntity> availableTables = tableService.getAllTableByStatus("AVAILABLE");
        return availableTables;
    }

    @PostMapping("/{id}/occupy")
    public ResponseEntity<TableEntity> occupyTable(@PathVariable Integer id) {
        try {
            TableEntity table = tableService.occupyTable(id);
            return ResponseEntity.ok(table);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(null);
        }
    }

    @GetMapping("/{id}")
    public TableEntity getTableById(@PathVariable Integer id) {
        return tableService.getTableById(id);
    }

    @GetMapping("/all-capacity/{capacity}")
    public List<TableEntity> getAllTableByCapacity(@PathVariable Integer capacity) {
        return tableService.getAllTableByCapacity(capacity);
    }

    @GetMapping("/all-status/{status}")
    public List<TableEntity> getAllTableByStatus(@PathVariable String status) {
        return tableService.getAllTableByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableEntity createTable(@RequestBody TableEntity TE) {
        return tableService.createTable(TE);
    }

    @PutMapping("/{id}")
    public TableEntity updateTable(@PathVariable Integer id, @RequestBody TableEntity TE) {
        return tableService.updateTable(id, TE);
    }

    @DeleteMapping("/{id}")
    public void deleteTable(@PathVariable Integer id) {
        tableService.deleteTable(id);
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseTable(@PathVariable Integer id) {
        tableService.releaseTable(id);
        return ResponseEntity.ok().build();
    }
}
