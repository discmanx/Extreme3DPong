package com.weakentroll.extreme3dpong;

import org.junit.Test;

import java.lang.Exception;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 *
 * Test local unit cases
 *
 * Created by: Ryan Baldwin
 */
public class GameLogicUnitTest {

    @Test
    public void Game_state_is_entry_on_creation() throws Exception {
        GameMachine gameMachine = new GameMachine();

        assertEquals(gameMachine.CurrentState().GetName(), "Entry");
    }
}