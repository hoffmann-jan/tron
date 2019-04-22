using System;

using Server.Enum;
using Server.Messages;

namespace Server
{
    [Obsolete]
    public struct Message
    {
        public Enum.Type type;

        public Move move;

        public int id;

        public Coordinates coordinates;

        public Field field;
    };
}
