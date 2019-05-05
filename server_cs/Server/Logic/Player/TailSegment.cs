using System;
using System.Collections.Generic;
using System.Text;

namespace Server.Logic.Player
{
    public class TailSegment
    {
        public Point Position { get; set; }
        public short RowCount { get; set; }

        public TailSegment(Point position, short rowCount)
        {
            Position = position;
            RowCount = rowCount;
        }
    }
}
