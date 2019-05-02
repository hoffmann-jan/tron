using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;

namespace Server.Udp
{
    public class CommunicationAdapter
    {
        #region Fields
        /// <summary>
        /// The server.
        /// </summary>
        readonly UdpClient _Server;
        /// <summary>
        /// The listener.
        /// </summary>
        readonly UpdListener _Listener;
        /// <summary>
        /// The sender.
        /// </summary>
        readonly UdpSender _Sender;
        /// <summary>
        /// The cancellation token source.
        /// </summary>
        readonly CancellationTokenSource _CancellationTokenSource;
        #endregion

        #region .ctor
        public CommunicationAdapter(int port)
        {
            _Server = new UdpClient(port);
            _Listener = new UpdListener(_Server);
            _Sender = new UdpSender(_Server);
            _CancellationTokenSource = new CancellationTokenSource();
        }
        #endregion

        #region public
        /// <summary>
        /// Starts the listening.
        /// </summary>
        public void StartListening()
        {
            _Listener.ProtocolReceived += Listener_ProtocolRecieved;
            Task.Run(() => _Listener.RecieveProtocol(), _CancellationTokenSource.Token);
        }

        /// <summary>
        /// Stops the listening.
        /// </summary>
        public void StopListening()
        {
            _Listener.ProtocolReceived -= Listener_ProtocolRecieved;
            _CancellationTokenSource.Cancel();
        }

        public void Send()
        {
            //_Sender
        }
        #endregion

        #region private
        /// <summary>
        /// Listeners the protocol recieved.
        /// </summary>
        /// <param name="protocolReceivedArguments">Protocol received arguments.</param>
        private void Listener_ProtocolRecieved(ProtocolReceivedArguments protocolReceivedArguments)
        {
            ProtocolReceived?.Invoke(protocolReceivedArguments);
        }
        #endregion

        #region event
        public delegate void ProtocolReceivedHandler(ProtocolReceivedArguments protocolReceivedArguments);
        public event ProtocolReceivedHandler ProtocolReceived;
        #endregion
    }
}
