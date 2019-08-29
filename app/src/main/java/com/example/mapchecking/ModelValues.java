package com.example.mapchecking;

public class ModelValues {
    Double latitude;
    Double longitude;
    String isFromMap;
    String date;
    String farmerName;
    String userNmae;
    String address;
    String city;
    String state;
    String country;
    String postalCode;

    public ModelValues(Double latitude, Double longitude, String isFromMap, String date, String farmerName, String userNmae, String address, String city, String state, String country, String postalCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFromMap = isFromMap;
        this.date = date;
        this.farmerName = farmerName;
        this.userNmae = userNmae;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getIsFromMap() {
        return isFromMap;
    }

    public void setIsFromMap(String isFromMap) {
        this.isFromMap = isFromMap;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getUserNmae() {
        return userNmae;
    }

    public void setUserNmae(String userNmae) {
        this.userNmae = userNmae;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
