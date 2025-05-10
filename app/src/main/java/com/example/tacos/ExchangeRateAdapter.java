package com.example.tacos;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExchangeRateAdapter extends RecyclerView.Adapter<ExchangeRateAdapter.ExchangeRateViewHolder> {
    private final List<Map.Entry<String, Double>> exchangeRates;

    public ExchangeRateAdapter(Map<String, Double> exchangeRatesMap) {
        this.exchangeRates = exchangeRatesMap != null ? new ArrayList<>(exchangeRatesMap.entrySet()) : new ArrayList<>();
    }

    @NonNull
    @Override
    public ExchangeRateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exchange_rate, parent, false);
        return new ExchangeRateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExchangeRateViewHolder holder, int position) {
        Map.Entry<String, Double> entry = exchangeRates.get(position);

        Log.d("ExchangeRateAdapter", "Binding rate: " + entry.getKey() + " = " + entry.getValue());

        holder.tvCurrencyCode.setText(entry.getKey());
        holder.tvExchangeRate.setText(String.valueOf(entry.getValue()));
    }


    @Override
    public int getItemCount() {
        return exchangeRates.size();
    }

    static class ExchangeRateViewHolder extends RecyclerView.ViewHolder {
        TextView tvCurrencyCode, tvExchangeRate;

        public ExchangeRateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCurrencyCode = itemView.findViewById(R.id.tvCurrencyCode);
            tvExchangeRate = itemView.findViewById(R.id.tvExchangeRate);
        }
    }
}

