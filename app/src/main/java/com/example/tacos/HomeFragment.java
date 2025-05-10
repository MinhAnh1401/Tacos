package com.example.tacos;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextPaint;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.blurry.Blurry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HomeFragment extends Fragment {
    private TextView userName,
            totalExpenseMonth,
            totalIncomeMonth,
            totalBalance,
            firstTopSpendingWeek,
            firstTopSpendingAmountWeek,
            secondTopSpendingWeek,
            secondTopSpendingAmountWeek,
            thirdTopSpendingWeek,
            thirdTopSpendingAmountWeek,
            firstTopSpendingMonth,
            firstTopSpendingAmountMonth,
            secondTopSpendingMonth,
            secondTopSpendingAmountMonth,
            thirdTopSpendingMonth,
            thirdTopSpendingAmountMonth,
            firstTopSpendingYear,
            firstTopSpendingAmountYear,
            secondTopSpendingYear,
            secondTopSpendingAmountYear,
            thirdTopSpendingYear,
            thirdTopSpendingAmountYear,
            firstRecentTransaction,
            firstRecentTransactionDay,
            firstRecentTransactionAmount,
            firstRecentTransactionNote,
            secondRecentTransaction,
            secondRecentTransactionDay,
            secondRecentTransactionAmount,
            secondRecentTransactionNote,
            thirdRecentTransaction,
            thirdRecentTransactionDay,
            thirdRecentTransactionAmount,
            thirdRecentTransactionNote,
            tvThisMonth,
            tvNoExpense,
            tvNoTransactions,
            tvFullDay,
            tvHello,
            tvReportThisMonth,
            tvBaseCurrency,
            tvExchangeRateDate,
            tvWeather;

    private ImageView ivProfile, ivExpenseIcon, ivIncomeIcon, ivReportIcon;

    private PieChart pcExpense, pcIncome;

    private LinearLayout llThisWeek, llThisMonth, llThisYear;

    private HorizontalScrollView hsvTopExpenses;

    private TableRow trRecentTransaction1, trRecentTransaction2, trRecentTransaction3;

    private RecyclerView rvExchangeRates;

    private static final String TAG = "HomeFragment";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        userName = view.findViewById(R.id.tv_user_name);
        ivProfile = view.findViewById(R.id.iv_profile);
        totalExpenseMonth = view.findViewById(R.id.tv_amount_total_spent);
        totalIncomeMonth = view.findViewById(R.id.tv_amount_total_income);
        totalBalance = view.findViewById(R.id.tv_amount_total_balance);
        firstTopSpendingWeek = view.findViewById(R.id.tv_top1_spending_week);
        firstTopSpendingAmountWeek = view.findViewById(R.id.tv_top1_spending_amount_week);
        secondTopSpendingWeek = view.findViewById(R.id.tv_top2_spending_week);
        secondTopSpendingAmountWeek = view.findViewById(R.id.tv_top2_spending_amount_week);
        thirdTopSpendingWeek = view.findViewById(R.id.tv_top3_spending_week);
        thirdTopSpendingAmountWeek = view.findViewById(R.id.tv_top3_spending_amount_week);
        firstTopSpendingMonth = view.findViewById(R.id.tv_top1_spending_month);
        firstTopSpendingAmountMonth = view.findViewById(R.id.tv_top1_spending_amount_month);
        secondTopSpendingMonth = view.findViewById(R.id.tv_top2_spending_month);
        secondTopSpendingAmountMonth = view.findViewById(R.id.tv_top2_spending_amount_month);
        thirdTopSpendingMonth = view.findViewById(R.id.tv_top3_spending_month);
        thirdTopSpendingAmountMonth = view.findViewById(R.id.tv_top3_spending_amount_month);
        firstTopSpendingYear = view.findViewById(R.id.tv_top1_spending_year);
        firstTopSpendingAmountYear = view.findViewById(R.id.tv_top1_spending_amount_year);
        secondTopSpendingYear = view.findViewById(R.id.tv_top2_spending_year);
        secondTopSpendingAmountYear = view.findViewById(R.id.tv_top2_spending_amount_year);
        thirdTopSpendingYear = view.findViewById(R.id.tv_top3_spending_year);
        thirdTopSpendingAmountYear = view.findViewById(R.id.tv_top3_spending_amount_year);
        firstRecentTransaction = view.findViewById(R.id.tv_recent_transaction_1);
        firstRecentTransactionDay = view.findViewById(R.id.tv_dt_recent_transaction_1);
        firstRecentTransactionAmount = view.findViewById(R.id.tv_recent_transaction_1_amount);
        firstRecentTransactionNote = view.findViewById(R.id.tv_note_recent_transaction_1);
        secondRecentTransaction = view.findViewById(R.id.tv_recent_transaction_2);
        secondRecentTransactionDay = view.findViewById(R.id.tv_dt_recent_transaction_2);
        secondRecentTransactionAmount = view.findViewById(R.id.tv_recent_transaction_2_amount);
        secondRecentTransactionNote = view.findViewById(R.id.tv_note_recent_transaction_2);
        thirdRecentTransaction = view.findViewById(R.id.tv_recent_transaction_3);
        thirdRecentTransactionDay = view.findViewById(R.id.tv_dt_recent_transaction_3);
        thirdRecentTransactionAmount = view.findViewById(R.id.tv_recent_transaction_3_amount);
        thirdRecentTransactionNote = view.findViewById(R.id.tv_note_recent_transaction_3);
        pcExpense = view.findViewById(R.id.pc_expense);
        pcIncome = view.findViewById(R.id.pc_income);
        ivExpenseIcon = view.findViewById(R.id.iv_expense_icon);
        ivIncomeIcon = view.findViewById(R.id.iv_income_icon);
        ivReportIcon = view.findViewById(R.id.iv_report_icon);

        llThisWeek = view.findViewById(R.id.ll_this_week);
        llThisMonth = view.findViewById(R.id.ll_this_month);
        llThisYear = view.findViewById(R.id.ll_this_year);

        tvThisMonth = view.findViewById(R.id.tv_this_month);
        tvNoExpense = view.findViewById(R.id.tv_no_expense);
        tvNoTransactions = view.findViewById(R.id.tv_no_transactions);
        tvFullDay = view.findViewById(R.id.tv_full_day);
        tvHello = view.findViewById(R.id.tv_hello);
        tvReportThisMonth = view.findViewById(R.id.tv_report_this_month);

        hsvTopExpenses = view.findViewById(R.id.hsv_top_expenses);

        trRecentTransaction1 = view.findViewById(R.id.tr_recent_transaction_1);
        trRecentTransaction2 = view.findViewById(R.id.tr_recent_transaction_2);
        trRecentTransaction3 = view.findViewById(R.id.tr_recent_transaction_3);

        rvExchangeRates = view.findViewById(R.id.rvExchangeRates);
        tvBaseCurrency = view.findViewById(R.id.tvBaseCurrency);

        rvExchangeRates.setLayoutManager(new LinearLayoutManager(getContext()));
        tvExchangeRateDate = view.findViewById(R.id.tv_exchange_rate_date);

        requestLocationPermission();
        loadExchangeRates();

        tvWeather = view.findViewById(R.id.tvWeather);


        loadData();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void loadExchangeRates() {
        LocalDateTime now = LocalDateTime.now();
        String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        tvExchangeRateDate.setText(currentDate);

        String url = "https://v6.exchangerate-api.com/v6/efa9e22ea166346bab4fff06/latest/USD";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();

                    Log.d(TAG, "Response: " + jsonResponse);
                    Gson gson = new Gson();
                    ExchangeRateResponse exchangeRateResponse = gson.fromJson(jsonResponse, ExchangeRateResponse.class);

                    requireActivity().runOnUiThread(() -> {
                        tvBaseCurrency.setText("Exchange rates for " + exchangeRateResponse.getBase());

                        ExchangeRateAdapter adapter = new ExchangeRateAdapter(
                                exchangeRateResponse.getRates() != null ? exchangeRateResponse.getRates() : new HashMap<>()
                        );
                        rvExchangeRates.setAdapter(adapter);
                    });

                } else {
                    Log.e(TAG, "Response code: " + response.code());
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to get exchange rates", e);
            }
        }).start();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Log.e("LocationError", "Location permission denied.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(FusedLocationProviderClient fusedLocationClient, LocationRequest locationRequest) {
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    fetchWeatherData(latitude, longitude);
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        }, Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        fetchWeatherData(latitude, longitude);
                    } else {
                        Log.e("LocationError", "Can't get location. Requesting new updates...");
                        requestNewLocationData(fusedLocationClient, locationRequest);
                    }
                })
                .addOnFailureListener(e -> Log.e("LocationError", "Error getting location: " + e.getMessage()));
    }

    @SuppressLint("SetTextI18n")
    private void fetchWeatherData(double latitude, double longitude) {
        String apiKey = "1bc59958a8e26ed42830a512307cc9ec";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";
        Log.d("WeatherURL", "URL: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();

                    Gson gson = new Gson();
                    WeatherResponse weatherResponse = gson.fromJson(jsonResponse, WeatherResponse.class);

                    double temperature = weatherResponse.getMain().getTemp();
                    String description = weatherResponse.getWeather()[0].getDescription();
                    String cityName = weatherResponse.getName();

                    requireActivity().runOnUiThread(() -> tvWeather.setText(cityName + " " + temperature + "°C - " + description));
                } else {
                    Log.e("WeatherAPIError", "API call failed with response code: " + response.code());
                }
            } catch (IOException | JsonSyntaxException e) {
                Log.e("WeatherAPIError", "Error fetching weather data: " + e.getMessage(), e);
            }
        }).start();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "ClickableViewAccessibility"})
    private void loadData() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String phone = bundle.getString("userPhone");
            User user = dbHelper.getUser(phone);
            int userId = user.getUserId();

            userName.setText(user.getFullName());

            double totalBalanceAllTime = dbHelper.getTotalBalanceAllTime(userId);
            String totalBalanceString = String.format("%.2f", totalBalanceAllTime);
            String hiddenBalance = maskCurrentBalance();
            setupCurrentBalanceToggle(totalBalanceString, hiddenBalance);

            LocalDateTime now = LocalDateTime.now();

            String day = String.valueOf(now.getDayOfWeek());
            String formattedDay = day.charAt(0) + day.substring(1).toLowerCase();
            String date = String.valueOf(now.getDayOfMonth());

            String month = now.getMonth().toString().toLowerCase();
            String showMonth = month.substring(0, 1).toUpperCase() + month.substring(1);
            tvThisMonth.setText(showMonth);

            String year = String.valueOf(now.getYear());

            String fullDay = formattedDay + ", " + date + " " + showMonth + " " + year;
            tvFullDay.setText(fullDay);

            tvHello.setText(getGreetingMessage());
            textGradient(tvHello);

            setScaleAnimation(ivExpenseIcon);
            setScaleAnimation(totalExpenseMonth);
            setScaleAnimation(ivIncomeIcon);
            setScaleAnimation(totalIncomeMonth);

            ivExpenseIcon.setOnClickListener(v -> handleTransactionClick(userId, "Expense", "Expenses this month", R.drawable.dangerous_alert));
            totalExpenseMonth.setOnClickListener(v -> handleTransactionClick(userId, "Expense", "Expenses this month", R.drawable.dangerous_alert));
            ivIncomeIcon.setOnClickListener(v -> handleTransactionClick(userId, "Income", "Incomes this month", R.drawable.success_alert));
            totalIncomeMonth.setOnClickListener(v -> handleTransactionClick(userId, "Income", "Incomes this month", R.drawable.success_alert));

            double totalExpense = dbHelper.getTotalForCurrentMonth(userId, "Expense");
            double totalIncome = dbHelper.getTotalForCurrentMonth(userId, "Income");
            animateTextViewAmount(totalExpenseMonth, totalExpense);
            animateTextViewAmount(totalIncomeMonth, totalIncome);

            loadRecentTransactions(dbHelper, userId);

            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_menu);

            setScaleAnimation(ivProfile);
            ivProfile.setOnClickListener(v -> {
                ProfileFragment profileFragment = new ProfileFragment();
                bundle.putString("userPhone", phone);
                profileFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .commit();

                bottomNavigationView.setSelectedItemId(R.id.profile);
            });

            setScaleAnimation(userName);
            userName.setOnClickListener(v -> {
                ProfileFragment profileFragment = new ProfileFragment();
                bundle.putString("userPhone", phone);
                profileFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .commit();

                bottomNavigationView.setSelectedItemId(R.id.profile);
            });

            if (dbHelper.getTotalTransactions(userId) == 0) {
                pcExpense.setVisibility(View.GONE);
                pcIncome.setVisibility(View.GONE);
                tvNoExpense.setVisibility(View.VISIBLE);
                hsvTopExpenses.setVisibility(View.GONE);
                tvNoTransactions.setVisibility(View.VISIBLE);
                ivExpenseIcon.setVisibility(View.GONE);
                ivIncomeIcon.setVisibility(View.GONE);
                totalExpenseMonth.setVisibility(View.GONE);
                totalIncomeMonth.setVisibility(View.GONE);
            } else if (dbHelper.getTotalTransactionsExpenseIncome(userId, "Expense") == 0 &&
                    dbHelper.getTotalTransactionsExpenseIncome(userId, "Income") != 0) {
                if (dbHelper.getTotalForCurrentMonth(userId, "Income") == 0){
                    pcIncome.setVisibility(View.GONE);
                    ivIncomeIcon.setVisibility(View.GONE);
                    totalIncomeMonth.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.VISIBLE);
                } else {
                    loadIncomePieChart(dbHelper, userId);
                    pcIncome.setVisibility(View.VISIBLE);
                    ivIncomeIcon.setVisibility(View.VISIBLE);
                    totalIncomeMonth.setVisibility(View.VISIBLE);
                    tvNoTransactions.setVisibility(View.GONE);
                }
                pcExpense.setVisibility(View.GONE);
                tvNoExpense.setVisibility(View.VISIBLE);
                hsvTopExpenses.setVisibility(View.GONE);
                ivExpenseIcon.setVisibility(View.GONE);
                totalExpenseMonth.setVisibility(View.GONE);
            } else if (dbHelper.getTotalTransactionsExpenseIncome(userId, "Expense") != 0 &&
                    dbHelper.getTotalTransactionsExpenseIncome(userId, "Income") == 0) {
                if (dbHelper.getTotalForCurrentMonth(userId, "Expense") == 0){
                    pcExpense.setVisibility(View.GONE);
                    ivExpenseIcon.setVisibility(View.GONE);
                    totalExpenseMonth.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.VISIBLE);
                } else {
                    loadExpensePieChart(dbHelper, userId);
                    pcExpense.setVisibility(View.VISIBLE);
                    ivExpenseIcon.setVisibility(View.VISIBLE);
                    totalExpenseMonth.setVisibility(View.VISIBLE);
                    tvNoTransactions.setVisibility(View.GONE);
                }
                pcIncome.setVisibility(View.GONE);
                tvNoExpense.setVisibility(View.GONE);
                hsvTopExpenses.setVisibility(View.VISIBLE);
                ivIncomeIcon.setVisibility(View.GONE);
                totalIncomeMonth.setVisibility(View.GONE);
            } else {
                if (dbHelper.getTotalForCurrentMonth(userId, "Expense") == 0 &&
                        dbHelper.getTotalForCurrentMonth(userId, "Income") == 0){
                    pcExpense.setVisibility(View.GONE);
                    ivExpenseIcon.setVisibility(View.GONE);
                    totalExpenseMonth.setVisibility(View.GONE);
                    pcIncome.setVisibility(View.GONE);
                    ivIncomeIcon.setVisibility(View.GONE);
                    totalIncomeMonth.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.VISIBLE);
                } else if (dbHelper.getTotalForCurrentMonth(userId, "Expense") != 0 &&
                        dbHelper.getTotalForCurrentMonth(userId, "Income") == 0) {
                    loadExpensePieChart(dbHelper, userId);
                    pcExpense.setVisibility(View.VISIBLE);
                    ivExpenseIcon.setVisibility(View.VISIBLE);
                    totalExpenseMonth.setVisibility(View.VISIBLE);
                    pcIncome.setVisibility(View.GONE);
                    ivIncomeIcon.setVisibility(View.GONE);
                    totalIncomeMonth.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.GONE);
                } else if (dbHelper.getTotalForCurrentMonth(userId, "Expense") == 0 &&
                        dbHelper.getTotalForCurrentMonth(userId, "Income") != 0) {
                    loadIncomePieChart(dbHelper, userId);
                    pcExpense.setVisibility(View.GONE);
                    ivExpenseIcon.setVisibility(View.GONE);
                    totalExpenseMonth.setVisibility(View.GONE);
                    pcIncome.setVisibility(View.VISIBLE);
                    ivIncomeIcon.setVisibility(View.VISIBLE);
                    totalIncomeMonth.setVisibility(View.VISIBLE);
                    tvNoTransactions.setVisibility(View.GONE);
                } else {
                    loadExpensePieChart(dbHelper, userId);
                    loadIncomePieChart(dbHelper, userId);
                    pcExpense.setVisibility(View.VISIBLE);
                    ivExpenseIcon.setVisibility(View.VISIBLE);
                    totalExpenseMonth.setVisibility(View.VISIBLE);
                    pcIncome.setVisibility(View.VISIBLE);
                    ivIncomeIcon.setVisibility(View.VISIBLE);
                    totalIncomeMonth.setVisibility(View.VISIBLE);
                    tvNoTransactions.setVisibility(View.GONE);
                }
                tvNoExpense.setVisibility(View.GONE);
                hsvTopExpenses.setVisibility(View.VISIBLE);
            }

            if (hsvTopExpenses.getVisibility() == View.VISIBLE) {
                loadTopSpending(dbHelper, userId);
            }

            setScaleAnimation(llThisWeek);
            llThisWeek.setOnClickListener(v -> {
                List<Pair<String, Double>> expenseTransactionsThisWeek = dbHelper.getExpenseTransactionsThisWeek(userId);

                expenseTransactionsThisWeek.sort((t1, t2) -> Double.compare(t2.second, t1.second));
                StringBuilder messageBuilder = new StringBuilder();
                double totalExpenseThisWeek = 0;
                for (Pair<String, Double> expenseTransaction : expenseTransactionsThisWeek) {
                    messageBuilder.append(expenseTransaction.first).append(": $ ").append(String.format("%.2f", expenseTransaction.second)).append("\n");
                    totalExpenseThisWeek += expenseTransaction.second;
                }

                String message = messageBuilder.length() > 0
                        ? messageBuilder + "\n" + "Total: $ " + String.format("%.2f", totalExpenseThisWeek)
                        : "You haven't made any expense transactions this week.";

                alertSuccess(
                        getContext(),
                        "Top expenses" + "\n" + "This week",
                        message,
                        R.drawable.dangerous_alert
                );
            });

            setScaleAnimation(llThisMonth);
            llThisMonth.setOnClickListener(v -> {
                List<Pair<String, Double>> expenseTransactionsThisMonth = dbHelper.getTransactionsByCategoryThisMonth(userId, "Expense");

                expenseTransactionsThisMonth.sort((t1, t2) -> Double.compare(t2.second, t1.second));
                StringBuilder messageBuilder = new StringBuilder();
                double totalExpenseThisMonth = 0;
                for (Pair<String, Double> expenseTransaction : expenseTransactionsThisMonth) {
                    messageBuilder.append(expenseTransaction.first).append(": $ ").append(String.format("%.2f", expenseTransaction.second)).append("\n");
                    totalExpenseThisMonth += expenseTransaction.second;
                }

                String message = messageBuilder.length() > 0
                        ? messageBuilder + "\n" + "Total: $ " + String.format("%.2f", totalExpenseThisMonth)
                        : "You haven't made any expense transactions this month.";

                alertSuccess(
                        getContext(),
                        "Top expenses" + "\n" + showMonth,
                        message,
                        R.drawable.dangerous_alert
                );
            });

            setScaleAnimation(llThisYear);
            llThisYear.setOnClickListener(v -> {
                List<Pair<String, Double>> expenseTransactionsThisYear = dbHelper.getExpenseTransactionsThisYear(userId);

                expenseTransactionsThisYear.sort((t1, t2) -> Double.compare(t2.second, t1.second));
                StringBuilder messageBuilder = new StringBuilder();
                double totalExpenseThisYear = 0;
                for (Pair<String, Double> expenseTransaction : expenseTransactionsThisYear) {
                    messageBuilder.append(expenseTransaction.first).append(": $ ").append(String.format("%.2f", expenseTransaction.second)).append("\n");
                    totalExpenseThisYear += expenseTransaction.second;
                }

                String message = messageBuilder.length() > 0
                        ? messageBuilder + "\n" + "Total: $ " + String.format("%.2f", totalExpenseThisYear)
                        : "You haven't made any expense transactions this year.";

                alertSuccess(
                        getContext(),
                        "Top expenses" + "\n" + year,
                        message,
                        R.drawable.dangerous_alert
                );
            });

            setScaleAnimation(ivReportIcon);
            ivReportIcon.setOnClickListener(this::toReportPage);
            setScaleAnimation(tvReportThisMonth);
            tvReportThisMonth.setOnClickListener(this::toReportPage);
            setScaleAnimation(tvThisMonth);
            tvThisMonth.setOnClickListener(this::toReportPage);
        }
    }

    private void toReportPage(View view) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            assert getArguments() != null;
            String phone = getArguments().getString("userPhone");
            int userId = dbHelper.getUser(phone).getUserId();
            Intent intent = new Intent(getActivity(), ReportActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
    }

    private String getGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good morning!";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon!";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening!";
        } else {
            return "Good night!";
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    @SuppressLint("DefaultLocale")
    private void handleTransactionClick(int userId, String categoryType, String title, int iconResId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())){
            List<Pair<String, Double>> transactionsByCategory = dbHelper.getTransactionsByCategoryThisMonth(userId, categoryType);
            transactionsByCategory.sort((t1, t2) -> Double.compare(t2.second, t1.second));
            StringBuilder messageBuilder = new StringBuilder();
            double totalAmount = transactionsByCategory.stream().mapToDouble(pair -> pair.second).sum();
            for (Pair<String, Double> transaction : transactionsByCategory) {
                messageBuilder.append(transaction.first).append(": $ ").append(String.format("%.2f", transaction.second)).append("\n");
            }

            String message = messageBuilder.length() > 0
                    ? messageBuilder + "\n" + "Total: $ " + String.format("%.2f", totalAmount)
                    : "You haven't made any " + categoryType.toLowerCase() + " transactions this month.";

            alertSuccess(
                    getContext(),
                    title,
                    message,
                    iconResId
            );
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
    }

    private String maskCurrentBalance() {
        return "••••••••••";
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setupCurrentBalanceToggle(String totalBalanceString, String hiddenBalance) {
        totalBalance.setText(hiddenBalance);
        totalBalance.setOnClickListener(v -> {
            String currentBalance = totalBalance.getText().toString();
            if (currentBalance.equals(hiddenBalance)) {
                totalBalance.setText("$ " + totalBalanceString);
            } else {
                totalBalance.setText(hiddenBalance);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void animateTextViewAmount(final TextView textView, double targetValue) {
        if (targetValue == 0) {
            textView.setText("$ 0.00");
            textView.setScaleX(1f);
            textView.setScaleY(1f);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, (float) targetValue);
        animator.setDuration(700);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                textView.setText("$ " + String.format("%.2f", animatedValue));

                float scale = 1 + animatedValue / (float) targetValue;
                textView.setScaleX(scale);
                textView.setScaleY(scale);
            }
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                ValueAnimator shrinkAnimator = ValueAnimator.ofFloat(textView.getScaleX(), 1f);
                shrinkAnimator.setDuration(700);
                shrinkAnimator.setInterpolator(new DecelerateInterpolator());

                shrinkAnimator.addUpdateListener(animation1 -> {
                    float scale = (float) animation1.getAnimatedValue();
                    textView.setScaleX(scale);
                    textView.setScaleY(scale);
                });

                shrinkAnimator.start();
            }
        });

        animator.start();
    }

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    private void loadPieChart(DatabaseHelper dbHelper, int userId, String transactionType,
                              PieChart pieChart, String centerText, int[] colors, String centerTextColor) {
        List<Pair<String, Double>> transactionsByCategory = dbHelper.getTransactionsByCategoryThisMonth(userId, transactionType);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        double totalAmount = transactionsByCategory.stream().mapToDouble(pair -> pair.second).sum();

        for (Pair<String, Double> pair : transactionsByCategory) {
            float percentage = (float) (pair.second / totalAmount * 100);
            pieEntries.add(new PieEntry(percentage, pair.first));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Category");
        pieDataSet.setSliceSpace(2f);
        pieDataSet.setSelectionShift(18f);
        pieDataSet.setColors(colors);
        pieDataSet.setDrawValues(false);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        pieChart.setRotationAngle(270f);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.parseColor(centerTextColor));

        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawRoundedSlices(true);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.setHighlightPerTapEnabled(true);

        transactionsByCategory.sort((p1, p2) -> Double.compare(p2.second, p1.second));

        pieChart.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();

                float centerX = pieChart.getWidth() / 2f;
                float centerY = pieChart.getHeight() / 2f;
                float radius = pieChart.getRadius();
                float distanceFromCenter = (float) Math.sqrt(Math.pow(event.getX() - centerX, 2) + Math.pow(event.getY() - centerY, 2));

                if (distanceFromCenter < radius / 2) {
                    StringBuilder message = new StringBuilder();
                    for (Pair<String, Double> pair : transactionsByCategory) {
                        float percentage = (float) (pair.second / totalAmount * 100);
                        message.append(pair.first).append(": ").append(String.format("%.2f", percentage)).append("%\n");
                    }

                    alertSuccess(
                            getContext(),
                            transactionType.equals("Expense") ? "Expense breakdown" : "Income breakdown",
                            message.toString(),
                            transactionType.equals("Expense") ? R.drawable.dangerous_alert : R.drawable.success_alert
                    );
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false;
        });
        pieChart.invalidate();
    }

    public void alertSuccess(Context context, String title, String message, int backgroundResource) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_layout, null);
        builder.setView(dialogView);

        LinearLayout llConfirmDialog = dialogView.findViewById(R.id.ll_confirm_dialog);
        llConfirmDialog.setBackgroundResource(backgroundResource);

        TextView tvTitle = dialogView.findViewById(R.id.dialog_title_confirm);
        tvTitle.setText(title);
        textGradient(tvTitle);

        TextView tvMessage = dialogView.findViewById(R.id.dialog_message_confirm);
        tvMessage.setText(message);

        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(context)
                .radius(10)
                .sampling(4)
                .onto((ViewGroup) rootView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_confirm);
        btnConfirm.setVisibility(View.GONE);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_confirm);
        btnCancel.setVisibility(View.GONE);

        dialog.setOnDismissListener(d -> Blurry.delete((ViewGroup) rootView));

        dialog.show();
    }

    private void loadExpensePieChart(DatabaseHelper dbHelper, int userId) {
        int[] colors = new int[]{
                Color.parseColor("#D94F4F"),
                Color.parseColor("#CC4545"),
                Color.parseColor("#B33C3C"),
                Color.parseColor("#993333"),
                Color.parseColor("#802929"),
                Color.parseColor("#662020"),
                Color.parseColor("#4D1717"),
                Color.parseColor("#331010"),
                Color.parseColor("#1A0808")
        };

        loadPieChart(dbHelper, userId, "Expense", pcExpense, "Expense", colors, "#640000");
    }

    private void loadIncomePieChart(DatabaseHelper dbHelper, int userId) {
        int[] colors = new int[]{
                Color.parseColor("#4FB54F"),
                Color.parseColor("#45A545"),
                Color.parseColor("#3C963C"),
                Color.parseColor("#338733"),
                Color.parseColor("#2A782A"),
                Color.parseColor("#206920"),
                Color.parseColor("#174A17"),
                Color.parseColor("#0E3B0E"),
                Color.parseColor("#062D06")
        };

        loadPieChart(dbHelper, userId, "Income", pcIncome, "Income", colors, "#006400");
    }

    private FragmentManager getSupportFragmentManager() {
        return getParentFragmentManager();
    }


    private void loadTopSpending(DatabaseHelper dbHelper, int userId) {
        loadTopSpendingForTimeFrame(dbHelper.getTopSpendingWeek(userId, 3),
                firstTopSpendingWeek,
                firstTopSpendingAmountWeek,
                secondTopSpendingWeek,
                secondTopSpendingAmountWeek,
                thirdTopSpendingWeek,
                thirdTopSpendingAmountWeek);

        loadTopSpendingForTimeFrame(dbHelper.getTopSpendingMonth(userId, 3),
                firstTopSpendingMonth,
                firstTopSpendingAmountMonth,
                secondTopSpendingMonth,
                secondTopSpendingAmountMonth,
                thirdTopSpendingMonth,
                thirdTopSpendingAmountMonth);

        loadTopSpendingForTimeFrame(dbHelper.getTopSpendingYear(userId, 3),
                firstTopSpendingYear,
                firstTopSpendingAmountYear,
                secondTopSpendingYear,
                secondTopSpendingAmountYear,
                thirdTopSpendingYear,
                thirdTopSpendingAmountYear);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void loadTopSpendingForTimeFrame(List<Pair<String, Double>> topSpending,
                                             TextView name1, TextView amount1,
                                             TextView name2, TextView amount2,
                                             TextView name3, TextView amount3) {
        if (!topSpending.isEmpty()) {
            name1.setText(topSpending.get(0).first);
            amount1.setText(String.format("$ %.2f", topSpending.get(0).second));

            if (topSpending.size() > 1) {
                name2.setText(topSpending.get(1).first);
                amount2.setText(String.format("$ %.2f", topSpending.get(1).second));
            } else {
                name2.setText("No data");
                amount2.setVisibility(View.GONE);
            }

            if (topSpending.size() > 2) {
                name3.setText(topSpending.get(2).first);
                amount3.setText(String.format("$ %.2f", topSpending.get(2).second));
            } else {
                name3.setText("No data");
                amount3.setVisibility(View.GONE);
            }
        } else {
            name1.setText("No data");
            amount1.setVisibility(View.GONE);

            name2.setText("No data");
            amount2.setVisibility(View.GONE);

            name3.setText("No data");
            amount3.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadRecentTransactions(DatabaseHelper dbHelper, int userId) {
        List<Pair<String, String>> recentTransactions = dbHelper.getRecentTransactions(userId);

        if (recentTransactions.isEmpty()) {
            firstRecentTransaction.setText("You haven't made any transactions yet.");
            firstRecentTransactionDay.setVisibility(View.GONE);
            firstRecentTransactionAmount.setVisibility(View.GONE);
            firstRecentTransactionNote.setVisibility(View.GONE);
            trRecentTransaction1.setVisibility(View.GONE);

            secondRecentTransaction.setVisibility(View.GONE);
            secondRecentTransactionDay.setVisibility(View.GONE);
            secondRecentTransactionAmount.setVisibility(View.GONE);
            secondRecentTransactionNote.setVisibility(View.GONE);
            trRecentTransaction2.setVisibility(View.GONE);

            thirdRecentTransaction.setVisibility(View.GONE);
            thirdRecentTransactionDay.setVisibility(View.GONE);
            thirdRecentTransactionAmount.setVisibility(View.GONE);
            thirdRecentTransactionNote.setVisibility(View.GONE);
            trRecentTransaction3.setVisibility(View.GONE);
            return;
        }

        setTransactionVisibility(firstRecentTransaction, firstRecentTransactionDay, firstRecentTransactionAmount, firstRecentTransactionNote, trRecentTransaction1, recentTransactions, 0);

        if (recentTransactions.size() > 1) {
            setTransactionVisibility(secondRecentTransaction, secondRecentTransactionDay, secondRecentTransactionAmount, secondRecentTransactionNote, trRecentTransaction2, recentTransactions, 1);
        } else {
            secondRecentTransaction.setVisibility(View.GONE);
            secondRecentTransactionDay.setVisibility(View.GONE);
            secondRecentTransactionAmount.setVisibility(View.GONE);
            secondRecentTransactionNote.setVisibility(View.GONE);
            trRecentTransaction2.setVisibility(View.GONE);
        }

        if (recentTransactions.size() > 2) {
            setTransactionVisibility(thirdRecentTransaction, thirdRecentTransactionDay, thirdRecentTransactionAmount, thirdRecentTransactionNote, trRecentTransaction3, recentTransactions, 2);
        } else {
            thirdRecentTransaction.setVisibility(View.GONE);
            thirdRecentTransactionDay.setVisibility(View.GONE);
            thirdRecentTransactionAmount.setVisibility(View.GONE);
            thirdRecentTransactionNote.setVisibility(View.GONE);
            trRecentTransaction3.setVisibility(View.GONE);
        }
    }

    private void setTransactionVisibility(TextView transactionView, TextView transactionDay, TextView transactionAmount,
                                          TextView transactionNote, TableRow recentTransactionView, List<Pair<String, String>> recentTransactions, int index) {
        transactionView.setVisibility(View.VISIBLE);
        transactionAmount.setVisibility(View.VISIBLE);
        transactionDay.setVisibility(View.VISIBLE);
        transactionNote.setVisibility(View.VISIBLE);
        transactionView.setVisibility(View.VISIBLE);

        setTransactionView(transactionView, transactionAmount, transactionNote, recentTransactionView, recentTransactions.get(index));

        String transactionDate = recentTransactions.get(index).second.split(" - ")[0];
        transactionDay.setText(transactionDate);
    }


    @SuppressLint("SetTextI18n")
    private void setTransactionView(TextView transactionView, TextView amountView, TextView noteView, TableRow recentTransactionView, Pair<String, String> transaction) {
        String transactionInfo = transaction.first;
        String transactionType = transaction.second.split(" - ")[1];
        String transactionNote = transaction.second.split(" - ")[2];

        transactionView.setText(transactionInfo.split(" - ")[0]);
        amountView.setText("$ " + transactionInfo.split(" - ")[1]);
        noteView.setText(transactionNote);

        if (transactionType.equals("Expense")) {
            recentTransactionView.setBackgroundResource(R.drawable.dangerous_widget);
        } else {
            recentTransactionView.setBackgroundResource(R.drawable.success_widget);
        }
    }

    public void textGradient(TextView textView) {
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#FF6464"),
                        Color.parseColor("#6464FF")
                }, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);
    }
}
