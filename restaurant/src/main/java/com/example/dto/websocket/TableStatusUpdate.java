package com.example.dto.websocket;

import java.util.List;

import com.example.dto.response.TableResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableStatusUpdate {
    private List<TableResponse> tables;
}
