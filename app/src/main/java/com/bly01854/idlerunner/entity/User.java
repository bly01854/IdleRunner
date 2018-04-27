package com.bly01854.idlerunner.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by bly01854 on 4/17/2018.
 */
// Player table
@Entity(tableName = "user")
public class User {

    public User(int id, int steps, int runningShoes, int energyDrink, int joggers, int coolBreeze) {
        this.id = id;
        this.steps = steps;
        this.runningShoes = runningShoes;
        this.energyDrink = energyDrink;
        this.joggers = joggers;
        this.coolBreeze = coolBreeze;
    }

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "steps")
    private int steps;

    @ColumnInfo(name = "runningShoes")
    private int runningShoes;

    @ColumnInfo(name = "energyDrink")
    private int energyDrink;

    @ColumnInfo(name = "joggers")
    private int joggers;

    @ColumnInfo(name = "coolBreeze")
    private int coolBreeze;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getRunningShoes() {
        return runningShoes;
    }

    public void setRunningShoes(int runningShoes) {
        this.runningShoes = runningShoes;
    }

    public int getEnergyDrink() {
        return energyDrink;
    }

    public void setEnergyDrink(int energyDrink) {
        this.energyDrink = energyDrink;
    }

    public int getJoggers() {
        return joggers;
    }

    public void setJoggers(int joggers) {
        this.joggers = joggers;
    }

    public int getCoolBreeze() {
        return coolBreeze;
    }

    public void setCoolBreeze(int coolBreeze) {
        this.coolBreeze = coolBreeze;
    }
}
