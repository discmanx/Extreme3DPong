package com.weakentroll.extreme3dpong;

import java.util.List;
import java.util.ArrayList;

class GameMachineState extends IState
{
    String mName;
    String mDescription;
    List<GameMachineState> mNeighbors = new ArrayList<GameMachineState>();
    public List<GameMachineState> Neighbors() {  return mNeighbors;  }

    /// <summary>
/// Initializes a new instance of the FunnerState class.
/// </summary>
/// <param name="mName">Name to display for this state</param>
/// <param name="mDescription">Text to display for this state</param>
    public GameMachineState(String mName, String mDescription)
    {
        this.mName = mName;
        this.mDescription = mDescription;
    }

    ///#region IState Overrides
    public String GetName()
    {
        return mName;
    }

    public void Run()
    {
        // We don't do any fancy stuff, just print out where we are
        //Console.WriteLine();
        //Console.WriteLine(mDescription);
    }
    ///#endregion
}
