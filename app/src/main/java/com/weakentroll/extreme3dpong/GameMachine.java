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
        GameMachineState entry = new GameMachineState("EntryMenu", "Select a single player game, multiplayer game, view high score, or choose options.");
        GameMachineState singlePlayer = new GameMachineState("SinglePlayerMenu", "Select a new game, load game");
        GameMachineState saveSinglePlayerGame = new GameMachineState("SaveSinglePlayerGame", "Save a game");
        GameMachineState loadSinglePlayerGame = new GameMachineState("LoadSinglePlayerGame", "Load a game from save");
        GameMachineState multiPlayer = new GameMachineState("MultiPlayerMenu", "Login or register.");
        GameMachineState multiPlayerList = new GameMachineState("MultiPlayerListMenu", "Select a player to challenge.");
        GameMachineState registerMultiPlayer = new GameMachineState("RegisterMultiPlayerMenu", "Register a new player account.");
        GameMachineState activeMatch = new GameMachineState("ActiveMatch", "Fighting opponent!");


        GameMachineState viewHighScores = new GameMachineState("ViewHighScores", "View high score.");
        GameMachineState options = new GameMachineState("ViewOptions", "Choose options.");

        GameMachineState pong = new GameMachineState("Pong", "Active Game State.");
        GameMachineState playerWins = new GameMachineState("PlayerWins", "Show player winning animation.");
        GameMachineState opponentWins = new GameMachineState("OpponentWins", "Show player losing animation .");
        GameMachineState gameOver = new GameMachineState("GameOver", "Round Over State.");

        mExit = new GameMachineState("Outside", "You have successfully exited the game.");

        // Hook up doors.
        entry.mNeighbors.add(singlePlayer);
        entry.mNeighbors.add(multiPlayer);
        entry.mNeighbors.add(viewHighScores);
        entry.mNeighbors.add(mExit);

        singlePlayer.mNeighbors.add(pong);
        singlePlayer.mNeighbors.add(saveSinglePlayerGame);
        singlePlayer.mNeighbors.add(loadSinglePlayerGame);
        singlePlayer.mNeighbors.add(entry);

        multiPlayer.mNeighbors.add(entry);
        multiPlayer.mNeighbors.add(multiPlayerList);
        multiPlayer.mNeighbors.add(registerMultiPlayer);

        multiPlayerList.mNeighbors.add(activeMatch);


        registerMultiPlayer.mNeighbors.add(multiPlayer);


        pong.mNeighbors.add(singlePlayer);
        pong.mNeighbors.add(gameOver);
        pong.mNeighbors.add(playerWins);
        pong.mNeighbors.add(opponentWins);

        gameOver.mNeighbors.add(pong);
        gameOver.mNeighbors.add(playerWins);
        gameOver.mNeighbors.add(opponentWins);


        // Add them to the collection
        mStates = new ArrayList<GameMachineState>();
        mStates.add(entry);
        mStates.add(singlePlayer);
        mStates.add(saveSinglePlayerGame);
        mStates.add(loadSinglePlayerGame);
        mStates.add(multiPlayer);
        mStates.add(multiPlayerList);
        mStates.add(registerMultiPlayer);
        mStates.add(pong);
        mStates.add(gameOver);
        mStates.add(playerWins);
        mStates.add(opponentWins);
        mStates.add(activeMatch);
        mStates.add(mExit);

        // Finally set my starting point
        mCurrent = entry;
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
