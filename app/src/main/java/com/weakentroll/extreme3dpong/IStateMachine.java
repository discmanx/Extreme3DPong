package com.weakentroll.extreme3dpong;

/**
 * Special thanks to Bryan Wagstaff (frob) and his article on 'State Machines in Games' from gamedev.net
 */
public abstract class IStateMachine
{
    // Accessor to look at the current state.
    public abstract IState CurrentState();// { super(); }

    // List of all possible transitions we can make from this current state.
    public abstract String[] PossibleTransitions();

    // Advance to a named state, returning true on success.
    public abstract boolean Advance(String nextState);

    // Is this state a "completion" state. Are we there yet?
    public abstract boolean IsComplete();
}
