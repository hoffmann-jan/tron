#pragma once

struct Message
{
    enum Type
    {
        TYPE_CONNECT    = 0,  // S <-> C: Request (C) / accept (S) connection, server sends player his id
        TYPE_DISCONNECT = 1,  // S <-> C: Disconnect (C, S) connection
        TYPE_ADD        = 2,  // S  -> C: Add new player, send player id
        TYPE_MOVE       = 3,  // S <-  C: Send move to the server
        TYPE_UPDATE     = 4,  // S  -> C: Update positions of all players
        TYPE_DEAD       = 5   // S  -> C: Tell a player that he died
    } type;

    enum Move
    {
        MOVE_UP    = 0,
        MOVE_DOWN  = 1,
        MOVE_LEFT  = 2,
        MOVE_RIGHT = 3,
        MOVE_JUMP  = 4
    } move;

    int id;

    struct Coordinates
    {
        int id;
        int x;
        int y;
    } coordinates[4];
};