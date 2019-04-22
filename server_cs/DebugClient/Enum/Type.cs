namespace Server.Enum
{
    public enum Type
    {
        TYPE_CONNECT = 0,  // S <-> C: Request (C) / accept (S) connection, server sends player his id
        TYPE_DISCONNECT = 1,  // S <-> C: Disconnect (C, S) connection
        TYPE_ADD = 2,  // S  -> C: Add new player, send player id
        TYPE_MOVE = 3,  // S <-  C: Send move to the server
        TYPE_UPDATE = 4,  // S  -> C: Update positions of all players
        TYPE_DEAD = 5   // S  -> C: Tell a player that he died
    }
}
