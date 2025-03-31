package com.HZFinger_FpStdSample.model;

/**
 * 加班人员数据模型
 */
public class OvertimePerson {
    private String personId;    // 人员编号
    private String name;        // 姓名
    private String department;  // 部门
    private boolean selected;   // 是否选中

    public OvertimePerson() {
    }

    public OvertimePerson(String personId, String name, String department) {
        this.personId = personId;
        this.name = name;
        this.department = department;
        this.selected = false;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OvertimePerson that = (OvertimePerson) obj;
        return personId != null ? personId.equals(that.personId) : that.personId == null;
    }

    @Override
    public int hashCode() {
        return personId != null ? personId.hashCode() : 0;
    }
}