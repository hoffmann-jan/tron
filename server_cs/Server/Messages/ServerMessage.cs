using System;
using System.Collections.Generic;
using System.Text;

namespace Server.Messages
{
    /// <summary>
    /// S-->--C.
    /// </summary>
    public struct ServerMessage
    {
        /// <summary>
        /// Kind of message.
        /// </summary>
        enum Type
        {
            TYPE_CONNECT = 0,  // S <-> C: Request (C) / accept (S) connection, server sends player his id
            TYPE_DISCONNECT = 1,  // S <-> C: Disconnect (C, S) connection
            TYPE_ADD = 2,  // S  -> C: Add new player, send player id
            TYPE_MOVE = 3,  // S <-  C: Send move to the server
            TYPE_UPDATE = 4,  // S  -> C: Update positions of all players
            TYPE_DEAD = 5   // S  -> C: Tell a player that he died
        }

        /// <summary>
        /// Coordinates as array size of 4.
        /// </summary>
        Coordinates[] coordinates;

        /// <summary>
        /// Game field size. DEBUG 500x500.
        /// </summary>
        struct Field
        {
            int heigth;
            int width;
        }
    }

    /// <summary>
    /// Coordniates.
    /// </summary>
    struct Coordinates
    {
        int id;
        int x;
        int y;
    }
}
