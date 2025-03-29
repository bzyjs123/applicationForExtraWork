package com.HZFinger_FpStdSample.model;

public class Person {
    private String id;
    private String name;
    private String department;
    private String cardNo;
    private byte[] signature;
    private byte[] fingerprint;

    public Person(String id, String name, String department, String cardNo, byte[] signature, byte[] fingerprint) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.cardNo = cardNo;
        this.signature = signature;
        this.fingerprint = fingerprint;
    }

    public Person() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(byte[] fingerprint) {
        this.fingerprint = fingerprint;
    }
}
