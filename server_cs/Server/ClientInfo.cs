using System.Net.Sockets;
using System.Text;
using Server.Protocol;

namespace Server
{
    /// <summary>
    /// Information about a Tron game client.
    /// </summary>
    public class ClientInfo
    {
        #region Fields
        private StateObject _StateObject;
        #endregion

        #region Properties
        public bool Ready { get; set; }
        public int LobbyId { get; set; }
        public Player Player { get; set; }
        /// <summary>
        /// State object for reading client data asynchronously.
        /// </summary>
        public StateObject StateObject { get => _StateObject; set => _StateObject = value; }
        #endregion

        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public ClientInfo(StateObject client, int playerId, Player player, int lobbyId)
        {
            StateObject = client;
            LobbyId = lobbyId;
            Player = player;
            Player.Id = playerId;
        }
        #endregion

    }
}
