using System.Net.Sockets;
using System.Text;

namespace Server
{
    /// <summary>
    /// State object for reading client data asynchronously.
    /// </summary>
    public class StateObject
    {
        // Client socket.
        public Socket WorkSocket = null;
        // Size of receive buffe
        public const int BufferSize = 1024;
        // Receive buffer.
        public byte[] Buffer = new byte[BufferSize];
        // Received data string.
        public StringBuilder StringBuilder = new StringBuilder();
    }
}
