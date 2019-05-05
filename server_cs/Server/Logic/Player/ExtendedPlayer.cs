using System.Collections.Generic;
using Server.Enum;
using Server.Protocol;
using Server;

namespace Server.Logic.Player
{
    public class ExtendedPlayer
    {
        // Spieler und Schwanzsegmente
        // pixel oben links
        // 10x10 pixel
        public Protocol.Player Player { get; set; }
        public Direction Direction { get; set; }
        public bool Death { get; set; }
        public int Length { get; set; }
        public Queue<TailSegment> Tail { get; set; }
        public short Jumping { get; set; }

        public ExtendedPlayer(Protocol.Player player)
        {
            Tail = new Queue<TailSegment>();
            Player = player;
            Direction = Direction.Left;
            Death = false;
            Jumping = 0;
        }
    }
}
