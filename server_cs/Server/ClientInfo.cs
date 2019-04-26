using System.Net.Sockets;
using System.Text;

namespace Server
{
    /// <summary>
    /// Information about a Tron game client.
    /// </summary>
    public class ClientInfo
    {
        #region Fields
        private int _PlayerId;
        private StateObject _StateObject;
        #endregion

        #region Properties
        public bool Ready { get; set; }
        public int LobbyId { get; set; }
        public int Color { get; set; }
        public string Name { get; set; }
        /// <summary>
        /// Player Id.
        /// </summary>
        public int PlayerId { get => _PlayerId; }
        /// <summary>
        /// State object for reading client data asynchronously.
        /// </summary>
        public StateObject StateObject { get => _StateObject; set => _StateObject = value; }
        #endregion

        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public ClientInfo(StateObject client, int playerId, string name, int lobbyId)
        {
            _PlayerId = playerId;
            _StateObject = client;
            LobbyId = lobbyId;
            Name = name;
        }
        #endregion

    }
}
