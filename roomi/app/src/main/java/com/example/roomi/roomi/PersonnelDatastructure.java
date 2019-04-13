package com.example.roomi.roomi;

// The data structure for for working with personal
public class PersonnelDatastructure {
    private String name;
    private String accessLevel;

    public PersonnelDatastructure() {

    }

    public PersonnelDatastructure(String name, String accessLevel) {
        this.name = name;
        this.accessLevel = accessLevel;
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


    public String getaccessLevel() {
        return accessLevel;
    }

    public void setaccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}

