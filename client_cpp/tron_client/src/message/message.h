#pragma once

#include <string>
#include <vector>

#include <nlohmann/json.hpp>

#include "player.h"

using json = nlohmann::json;

enum Type
{
    TYPE_CONNECT    = 0,  // S <-> C: assigns id to a player
    TYPE_DISCONNECT = 1,  // S  -> C: removes a player from a lobby / game
    TYPE_ADD        = 2,  // S  -> C: broadcasts a new player
    TYPE_UPDATE     = 3,  // S  -> C: server updates player positions
    TYPE_ACTION     = 4,  // S <-  C: client does something
    TYPE_DEAD       = 5,  // S  -> C: broadcast the death of a player
    TYPE_LOBBY      = 6,  // S  -> C: initial message to enter a lobby
    TYPE_READY      = 7,  // S <-  C: clients can ready up in the lobby
    TYPE_START      = 8,  // S  -> C: ends the lobby and starts the game
    TYPE_RESULT     = 9   // S  -> C: shows the end result
};

enum Action
{
    ACT_UP    = 0,
    ACT_DOWN  = 1,
    ACT_LEFT  = 2,
    ACT_RIGHT = 3,
    ACT_JUMP  = 4
};

struct Message
{
    Type type;
    Action action;

    int lobbyId;
    int length;

    std::vector<Player> players;
};

void to_json(json& j, const Message& m);
void from_json(const json& j, Message& m);

// 1. TYPE_CONNECT: C -> S: (name, lobby id)
// 2. TYPE_CONNECT: S -> C: (player id, color)
// 3. TYPE_LOBBY: S -> C: inital lobby message (waiting players and their positions)
// 3. or TYPE_ADD for waiting players: add new player (new player and position)
// 4. TYPE_READY: C -> S
// 5. TYPE_START: S -> C: clients start moving (initially to the middle)
// 5. TYPE_UPDATE: S -> C: updated positions
// 5. TYPE_ACTION: C -> S: Action (also sends player id)
// 5. TYPE_DEAD: S -> C
// 5. TYPE_DISCONNECT: C -> S, S -> C (sends player id)
// 6. TYPE_RESULT: S -> C: sends winning player id
// 7. TYPE_LOBBY: S -> C: Back to 3.
