package com.example.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AddItemsRequest {
    private List<CreateOrderItemRequest> items;
    private String notes;
}