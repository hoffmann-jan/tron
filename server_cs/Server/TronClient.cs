using System.Net.Sockets;

namespace Server
{
    /// <summary>
    /// Player object in the tron game.
    /// </summary>
    public class TronClient
    {
        /// <summary>
        /// Id.
        /// </summary>
        public int PlayerId { get; set; }
        /// <summary>
        /// Socket.
        /// </summary>
        public Socket WorkSocket { get; set; }
    }
}
