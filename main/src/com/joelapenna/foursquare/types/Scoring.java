/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2009-11-12 21:45:34.375796
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Scoring implements FoursquareType {

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

}
