using Server.Logic;
using System.Net.Sockets;

namespace Server
{
    /// <summary>
    /// Player object in the tron game.
    /// </summary>
    public class TronClient
    {
        /// <summary>
        /// Player.
        /// </summary>
        public TronPlayer Player { get; set; }
        /// <summary>
        /// Socket.
        /// </summary>
        public Socket WorkSocket { get; set; }
    }
}
