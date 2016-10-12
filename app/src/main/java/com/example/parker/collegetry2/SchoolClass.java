package com.example.parker.collegetry2;

import java.io.Serializable;

// Class to hold all "SchoolClass"/Class information.
public class SchoolClass implements Serializable {
    public int ID;
    public String name;
    public String room;
    public String dayStart;
    public String classStart;
    public String classEnd;
    public byte[] syllabus;
    public int notifyStart;

    public int hashCode() {
        return ID;
    }
}
