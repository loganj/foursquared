/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-07-26 20:59:18.462788
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Scoring implements Parcelable, FoursquareType {

    private Group mRank;
    private Score mScore;
    private Score mTotal;

    public Scoring() {
    }

    public Group getRank() {
        return mRank;
    }

    public void setRank(Group rank) {
        mRank = rank;
    }

    public Score getScore() {
        return mScore;
    }

    public void setScore(Score score) {
        mScore = score;
    }

    public Score getTotal() {
        return mTotal;
    }

    public void setTotal(Score total) {
        mTotal = total;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {

        };
        dest.writeBooleanArray(booleanArray);
        dest.writeParcelable((Parcelable)this.mRank, flags);
        dest.writeParcelable(this.mScore, 0);
        dest.writeParcelable(this.mTotal, 0);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mRank = (Group)source.readParcelable(null);
        this.mScore = source.readParcelable(null);
        this.mTotal = source.readParcelable(null);
    }

    public static final Parcelable.Creator<Scoring> CREATOR = new Parcelable.Creator<Scoring>() {

        @Override
        public Scoring createFromParcel(Parcel source) {
            Scoring instance = new Scoring();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Scoring[] newArray(int size) {
            return new Scoring[size];
        }

    };

}
