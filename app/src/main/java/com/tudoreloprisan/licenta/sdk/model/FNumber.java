package com.tudoreloprisan.licenta.sdk.model;

/**
 * Created by Doru on 6/25/2016.
 */
public enum FNumber {
    F140("1.4"),
    F180("1.8"),
    F20("2"),
    F220("2.2"),
    F250("2.5"),
    F280("2.8"),
    F350("3.5"),
    F40("4"),
    F450("4.5"),
    F50("5"),
    F560("5.6"),
    F630("6.3"),
    F710("7.1"),
    F80("8"),
    F90("9"),
    F100("10"),
    F110("11"),
    F130("13"),
    F1400("14"),
    F1600("16"),
    FD1800("18"),
    F2000("20"),
    F2200("22"),
    F2500("25"),
    F2900("29"),
    F3200("32"),
    F3600("36");

    FNumber(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }
}
