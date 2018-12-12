package com.example.roomi.roomi;

public class HomeRoomDataStructure {
    private String name;
    private int temperature;
    private int brightness;

    public HomeRoomDataStructure() {

    }

    public HomeRoomDataStructure(String name, int temperature, int brightness) {
        this.name = name;
        this.temperature = temperature;
        this.brightness = brightness;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        String[] arr = name.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        this.name = sb.toString().trim();
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}
