package com.weakentroll.extreme3dpong;


import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

class GameMachine extends IStateMachine
{
    List<GameMachineState> mStates;
    GameMachineState mCurrent;
    GameMachineState mExit;

    public GameMachine()
    {
        // Create all the fun states in our mini-world
        GameMachineState entry = new GameMachineState("Entry", "Select a new game, view high score, or choose options.");
        GameMachineState pong = new GameMachineState("Pong", "Active Game State.");
        GameMachineState playerWins = new GameMachineState("PlayerWins", "Show player winning animation.");
        GameMachineState opponentWins = new GameMachineState("OpponentWins", "Show player losing animation .");
        GameMachineState gameOver = new GameMachineState("GameOver", "Round Over State.");

        mExit = new GameMachineState("Outside", "You have successfully exited the game.");

        // Hook up doors.
        entry.mNeighbors.add(pong);
        entry.mNeighbors.add(mExit);

        pong.mNeighbors.add(entry);
        pong.mNeighbors.add(gameOver);
        pong.mNeighbors.add(playerWins);
        pong.mNeighbors.add(opponentWins);

        gameOver.mNeighbors.add(pong);
        gameOver.mNeighbors.add(playerWins);
        gameOver.mNeighbors.add(opponentWins);


        // Add them to the collection
        mStates = new ArrayList<GameMachineState>();
        mStates.add(entry);
        mStates.add(pong);
        mStates.add(gameOver);
        mStates.add(playerWins);
        mStates.add(opponentWins);
        mStates.add(mExit);

        // Finally set my starting point
        mCurrent = pong;
    }

    ///#region IStateMachine Overrides
    public IState CurrentState()
    {
        return mCurrent;
    }
    public  String[] PossibleTransitions()
    {
        List<String> result = new ArrayList<String>();
        for (Iterator<GameMachineState> i = mCurrent.mNeighbors.iterator(); i.hasNext(); )
        {
            GameMachineState item = i.next();
            result.add(item.GetName());
        }
        return (String[]) result.toArray();

    }
    public  boolean Advance(String nextState)
    {
        for (Iterator<GameMachineState> i = mCurrent.mNeighbors.iterator(); i.hasNext(); )
        {
            GameMachineState item = i.next();

            if (nextState == item.GetName())
            {
                mCurrent = item;
                return true;
            }
        }
        // System.Console.WriteLine("Invalid state.");
        return false;
    }
    public  boolean IsComplete()
    {
        return mCurrent == mExit;
    }
    ///#endregion
}
