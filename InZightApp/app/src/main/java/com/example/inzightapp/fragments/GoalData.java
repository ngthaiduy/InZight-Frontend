package com.example.inzightapp.fragments;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data class to store goal information before calculating
 */
public class GoalData implements Parcelable {
    public String name;
    public double targetAmount;
    public double monthlySaving;
    public double interestRate;
    public int priority;
    public String targetDate;
    public int iconResId;
    
    // Calculated results
    public int monthsNeeded;
    public int yearsNeeded;
    public double totalWithInterest;
    public double interestEarned;
    
    public GoalData() {
    }
    
    public GoalData(String name, double targetAmount, double monthlySaving, 
                   double interestRate, int priority, String targetDate, int iconResId) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.monthlySaving = monthlySaving;
        this.interestRate = interestRate;
        this.priority = priority;
        this.targetDate = targetDate;
        this.iconResId = iconResId;
    }
    
    // Parcelable implementation
    protected GoalData(Parcel in) {
        name = in.readString();
        targetAmount = in.readDouble();
        monthlySaving = in.readDouble();
        interestRate = in.readDouble();
        priority = in.readInt();
        targetDate = in.readString();
        iconResId = in.readInt();
        monthsNeeded = in.readInt();
        yearsNeeded = in.readInt();
        totalWithInterest = in.readDouble();
        interestEarned = in.readDouble();
    }
    
    public static final Creator<GoalData> CREATOR = new Creator<GoalData>() {
        @Override
        public GoalData createFromParcel(Parcel in) {
            return new GoalData(in);
        }
        
        @Override
        public GoalData[] newArray(int size) {
            return new GoalData[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(targetAmount);
        dest.writeDouble(monthlySaving);
        dest.writeDouble(interestRate);
        dest.writeInt(priority);
        dest.writeString(targetDate);
        dest.writeInt(iconResId);
        dest.writeInt(monthsNeeded);
        dest.writeInt(yearsNeeded);
        dest.writeDouble(totalWithInterest);
        dest.writeDouble(interestEarned);
    }
}

