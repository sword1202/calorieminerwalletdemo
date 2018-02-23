package com.calorieminer.minerapp.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class GameParam implements Comparable<GameParam> {

    public String whoseGame;
    public int total_rounds;
    public ArrayList<Integer> starterAssignment;
    public ArrayList<Integer> joinerAssignment;
    public ArrayList<Integer> resultAssignment;
    public boolean started;
    public boolean waitFlag;
    public int starterScore;
    public int joinerScore;
    public ArrayList<Integer> user1Selection, user2Selection;
    public boolean starterSound;
    public boolean joinerSound;
    public boolean readyJoinerToStart;
    public boolean starterFinished;
    public boolean joinerFinished;

    public GameParam()
    {

    }

    public GameParam(String whoseGame, int total_rounds, ArrayList<Integer> serverAssignment, ArrayList<Integer> customerAssignment,
                     ArrayList<Integer> resultAssignment, boolean started, boolean waitFlag, int serverScore, int customerScore,
                     ArrayList<Integer> user1Selection, ArrayList<Integer> user2Selection, boolean starterSound, boolean joinerSound, boolean readyJoinerToStart,
                     boolean starterFinished, boolean joinerFinished)
    {
        this.whoseGame = whoseGame;
        this.total_rounds = total_rounds;
        this.starterAssignment = serverAssignment;
        this.joinerAssignment = customerAssignment;
        this.resultAssignment = resultAssignment;
        this.started = started;
        this.waitFlag = waitFlag;
        this.starterScore = serverScore;
        this.joinerScore = customerScore;
        this.user1Selection = user1Selection;
        this.user2Selection = user2Selection;
        this.starterSound = starterSound;
        this.joinerSound = joinerSound;
        this.readyJoinerToStart = readyJoinerToStart;
        this.starterFinished = starterFinished;
        this.joinerFinished = joinerFinished;
    }

    public boolean isReadyJoinerToStart()
    {
        return readyJoinerToStart;
    }

    public String getWhoseGame()
    {
        return whoseGame;
    }
    public int getTotal_rounds()
    {
        return total_rounds;
    }

    public ArrayList<Integer> getStarterAssignment()
    {
        return starterAssignment;
    }

    public ArrayList<Integer> getJoinerAssignment()
    {
        return joinerAssignment;
    }

    public ArrayList<Integer> getResultAssignment()
    {
        return resultAssignment;
    }

    public boolean isStarted()
    {
        return started;
    }

    public boolean isWaitFlag()
    {
        return waitFlag;
    }

    public int getStarterScore()
    {
        return starterScore;
    }

    public int getJoinerScore()
    {
        return joinerScore;
    }

    public ArrayList<Integer> getUser1Selection()
    {
        return user1Selection;
    }

    public ArrayList<Integer> getUser2Selection()
    {
        return user2Selection;
    }

    public boolean isStarterSound()
    {
        return starterSound;
    }

    public boolean isJoinerSound()
    {
        return joinerSound;
    }

    public boolean isStarterFinished()
    {
        return starterFinished;
    }

    public boolean isJoinerFinished()
    {
        return joinerFinished;
    }
    // ===============

    public void setWhoseGame(String whoseGame)
    {
        this.whoseGame = whoseGame;
    }

    public void setTotal_rounds(int total_rounds)
    {
        this.total_rounds = total_rounds;
    }

    public void setStarterAssignment(ArrayList<Integer> serverAssignment)
    {
        this.starterAssignment = serverAssignment;
    }

    public void setJoinerAssignment(ArrayList<Integer> customerAssignment)
    {
        this.joinerAssignment = customerAssignment;
    }

    public void setResultAssignment(ArrayList<Integer> resultAssignment)
    {
        this.resultAssignment = resultAssignment;
    }

    public void setReadyJoinerToStart(boolean readyJoinerToStart)
    {
        this.readyJoinerToStart = readyJoinerToStart;
    }
    public void setStarted(boolean started)
    {
        this.started = started;
    }

    public void setWaitFlag(boolean waitFlag)
    {
        this.waitFlag = waitFlag;
    }

    public void setStarterScore(int serverScore)
    {
        this.starterScore = serverScore;
    }

    public void setJoinerScore(int customerScore)
    {
        this.joinerScore = customerScore;
    }

    public void setUser1Selection(ArrayList<Integer> user1Selection)
    {
        this.user1Selection = user1Selection;
    }

    public void setUser2Selection(ArrayList<Integer> user2Selection)
    {
        this.user2Selection = user2Selection;
    }

    public void setStarterSound(boolean starterSound)
    {
        this.starterSound = starterSound;
    }

    public void setJoinerSound(boolean joinerSound)
    {
        this.joinerSound = joinerSound;
    }

    public void setStarterFinished(boolean starterFinished)
    {
        this.starterFinished = starterFinished;
    }

    public void setJoinerFinished(boolean joinerFinished)
    {
        this.joinerFinished = joinerFinished;
    }


    @Override
    public int compareTo(@NonNull GameParam o) {
        int resultValue = -1;
        if (this.whoseGame == o.whoseGame && this.starterAssignment == null && this.joinerAssignment == null &&
                this.resultAssignment == o.resultAssignment && this.started == o.started && this.waitFlag == o.waitFlag && this.starterScore == o.starterScore &&
                this.joinerScore == o.joinerScore && this.user1Selection == null && this.user2Selection == null && this.starterSound == o.starterSound &&
                this.joinerSound == o.joinerSound && this.readyJoinerToStart == o.readyJoinerToStart && this.starterFinished == o.starterFinished && this.joinerFinished == o.joinerFinished)
        {
            resultValue = 1;
        }


        return resultValue;
    }
}
