using Server.Enum;
using Server.Protocol;

namespace Server
{
    public class ExtendedPlayer
    {
        public Player Player { get; set; }
        public Direction Direction { get; set; }
        public Point LastDraw { get; set; }
        public Point LastJump { get; set; }
        public bool JumpCooldown { get; set; }
        public bool Death { get; set; }
        public int Length { get; set; }

        public ExtendedPlayer(Player player)
        {
            Player = player;
            Direction = Direction.Left;
            Death = false;
        }
    }
}
