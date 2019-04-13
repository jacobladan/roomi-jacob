package com.example.roomi.roomi;

public class SecurityRoomDataStructure {
    private String name;
    private String accessLevel;

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

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public SecurityRoomDataStructure() {}

    public SecurityRoomDataStructure(String name, String accessLevel) {
        this.name = name;
        this.accessLevel = accessLevel;
    }
}
