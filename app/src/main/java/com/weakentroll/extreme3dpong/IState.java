package com.weakentroll.extreme3dpong;

public abstract class IState
{
    // Utility function to help us display useful things
    public abstract String GetName();
    // Do something
    public abstract void Run();

    // This isn't really needed, but it helps in debugging and other tasks.
    // It allows hover-tips and debug info to show me the name of the state
    // rather than the default of the type of the object
    public String ToString()
    {
        return GetName();
    }
}
