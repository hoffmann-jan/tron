using System.Net.Sockets;
using System.Security.Cryptography;
using System.Threading;
using Server.Protocol;

namespace Server
{
    /// <summary>
    /// Information about a Tron game client.
    /// </summary>
    public class ClientInfo
    {
        #region Fields
        #endregion

        #region Properties
        public bool Ready { get; set; }
        public int LobbyId { get; set; }
        public Player Player { get; set; }
        public TcpClient TcpClient { get; set; }
        public RSACryptoServiceProvider crypto { get; set; } = null;
        public Thread Connection { get; set; }
        #endregion

        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public ClientInfo(TcpClient tcpClient, int playerId, Player player, int lobbyId)
        {
            TcpClient = tcpClient;
            LobbyId = lobbyId;
            Player = player;
            Player.Id = playerId;
        }
        #endregion

    }
}
