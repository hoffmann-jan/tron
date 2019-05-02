using System.Collections.Generic;
using Server.Enum;
using Server.Protocol;

namespace Server
{
    public class ExtendedPlayer
    {
        // Spieler und Schwanzsegmente
        // pixel oben links
        // 10x10 pixel
        public Player Player { get; set; }
        public Direction Direction { get; set; }
        public bool Death { get; set; }
        public int Length { get; set; }
        public List<Point> Tail { get; set; }

        public ExtendedPlayer(Player player)
        {
            Tail = new List<Point>();
            Player = player;
            Direction = Direction.Left;
            Death = false;
        }
    }
}
