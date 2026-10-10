package com.optiflow.crm.client.dto;

// Criterios de busqueda para la búsqueda avanzada
public class ClientSearchDto {

    private String name;

    private String surname;

    private String lastSurname;

    private String Dni;

    private String phoneNumber;

    private String email;

    private String city;

    private String postalCode;

    private Boolean active = true;

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "FirstSurame";

    private String sortDirection = "asc";

    // Getters y Setters

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLastSurname() { return lastSurname; }

    public void setLastSurname(String lastSurname) {
        this.lastSurname = lastSurname;
    }

    public String getDni() {
        return Dni;
    }

    public void setDni(String Dni) {
        this.Dni = Dni;
    }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() { return postalCode; }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}