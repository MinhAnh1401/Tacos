package com.example.tacos;

public class WeatherResponse {
    private Main main;
    private Weather[] weather;
    private String name;

    public Main getMain() { return main; }
    public Weather[] getWeather() { return weather; }
    public String getName() { return name; }

    public class Main {
        private double temp;
        public double getTemp() { return temp; }
    }

    public class Weather {
        private String description;
        public String getDescription() { return description; }
    }
}

