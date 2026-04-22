package xyz.peasfultown.ecommerce.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

public enum StockStatus implements Serializable {
    LOW_STOCK("LOW_STOCK"),
    IN_STOCK("IN_STOCK"),
    OUT_OF_STOCK("OUT_OF_STOCK");

    private final String value;

    StockStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static StockStatus fromValue(String value) {
        for (StockStatus s : StockStatus.values())
            if (s.value.equals(value))
                return s;

        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
