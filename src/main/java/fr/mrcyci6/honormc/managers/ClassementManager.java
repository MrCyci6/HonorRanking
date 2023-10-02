package fr.mrcyci6.honormc.managers;

import java.util.HashMap;

public class ClassementManager {

    private String name;
    private int cPoints;
    private int fPoints;

    public ClassementManager(String name, int cPoints, int fPoints) {
        this.name = name;
        this.cPoints = cPoints;
        this.fPoints = fPoints;
    }

    public String getName() {
        return name;
    }

    public int getcPoints() {
        return cPoints;
    }

    public int getfPoints() {
        return fPoints;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setcPoints(int cPoints) {
        this.cPoints = cPoints;
    }

    public void setfPoints(int fPoints) {
        this.fPoints = fPoints;
    }
}
