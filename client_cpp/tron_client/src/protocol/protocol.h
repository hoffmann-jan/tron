#pragma once

#include <vector>

struct Position
{
    int id;  // Player id
    int x;   // Player x coordinate
    int y;   // Player y coordinate
};

enum Type  // Type of request / response
{
    TYPE_ADD    = 0,  // S  -> C: add a new player to the lobby
    TYPE_REMOVE = 1,  // S  -> C: remove a player from a lobby / game
    TYPE_READY  = 2,  // S <-  C: clients can ready up to start the game
    TYPE_UPDATE = 3,  // S  -> C: server updates player positions
    TYPE_ACTION = 4,  // S <-  C: the client changes its direction
    TYPE_DIED   = 5,  // S  -> C: broadcast the death of a player
    TYPE_LOBBY  = 6   // S  -> C: tells clients to enter the lobby screen
};

enum Action
{
    ACT_UP    = 0,
    ACT_DOWN  = 1,
    ACT_LEFT  = 2,
    ACT_RIGHT = 3,
    ACT_JUMP  = 4
};

struct Protocol
{
    Type type;

    Action action;

    int id;  // Player id, send in TYPE_ADD / TYPE_REMOVE / TYPE_DEAD

    std::vector<Position> positions;  // Array of player positions
};

// Example
// - C1 connects to the S
// - S sends C1 its id (TYPE_ADD) and positions it somewhere in the field (TYPE_UPDATE)
// - S and C1 wait until a second player connects
// - S adds the seconds player (TYPE_ADD) and positions him on the field (TYPE_UPDATE)
// - if the lobby contains at least 2 players, they can ready up to start the game (TYPE_READY)
// - if all players readied up, the game starts and their initial direction is either up or down
// - players can change their direction or jump and send TYPE_ACTION and ACT_
// - if a player dies, the server broadcasts it to all other players (TYPE_DIED) and ignores actions of the dead player
// - after a player won, all other players enter the lobby screen again