package com.example.darkstore.presentation.dto.mappers;

import com.example.darkstore.dataAccess.models.Stock;
import com.example.darkstore.presentation.dto.objects.stock.StockResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "productId",
            source = "product.id")

    @Mapping(target = "productName",
            source = "product.name")

    @Mapping(target = "availableQuantity",
            expression = "java(stock.getAvailableQuantity())")
    StockResponse toResponse(Stock stock);

    List<StockResponse> toResponseList(List<Stock> stocks);
}
