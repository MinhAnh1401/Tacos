package com.example.tacos.response;

import java.util.Map;

public class ExchangeRateResponse {
    private String base_code;
    private Map<String, Double> conversion_rates;

    public ExchangeRateResponse(String base) {
        this.base_code = base;
    }

    public String getBase() {
        return base_code;
    }

    public Map<String, Double> getRates() {
        return conversion_rates;
    }
}
