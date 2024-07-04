package com.example.medmap;

public class Appointment {
    private String fullName;
    private String phoneNumber;
    private String dateOfAppointment;
    private String additionalNotes;

    public Appointment(){
    }

    public Appointment(String fullName,String phoneNumber,String dateOfAppointment,String additionalNotes){
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.dateOfAppointment = dateOfAppointment;
        this.additionalNotes = additionalNotes;
    }

    public String getFullName(){
        return fullName;
    }
    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfAppointment(){
        return dateOfAppointment;
    }

    public void setDateOfAppointment(String dateOfAppointment) {
        this.dateOfAppointment = dateOfAppointment;
    }

    public String getAdditionalNotes(){
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}
