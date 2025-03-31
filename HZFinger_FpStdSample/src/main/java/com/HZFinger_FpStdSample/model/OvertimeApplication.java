package com.HZFinger_FpStdSample.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 加班申请数据模型
 */
public class OvertimeApplication {
    private Date overtimeDate;      // 加班日期
    private String startTime;       // 开始时间
    private String endTime;         // 结束时间
    private String reason;          // 加班原因
    private String department;      // 部门
    private List<OvertimePerson> persons; // 加班人员列表

    public OvertimeApplication() {
        this.persons = new ArrayList<>();
    }

    public OvertimeApplication(Date overtimeDate, String startTime, String endTime, String reason, String department) {
        this.overtimeDate = overtimeDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.department = department;
        this.persons = new ArrayList<>();
    }

    public Date getOvertimeDate() {
        return overtimeDate;
    }

    public void setOvertimeDate(Date overtimeDate) {
        this.overtimeDate = overtimeDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<OvertimePerson> getPersons() {
        return persons;
    }

    public void setPersons(List<OvertimePerson> persons) {
        this.persons = persons;
    }

    public void addPerson(OvertimePerson person) {
        this.persons.add(person);
    }

    public void removePerson(OvertimePerson person) {
        this.persons.remove(person);
    }

    public void removePerson(int position) {
        if (position >= 0 && position < persons.size()) {
            this.persons.remove(position);
        }
    }
}