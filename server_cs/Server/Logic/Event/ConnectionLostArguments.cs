using Server.Tcp;

namespace Server.Logic.Event
{
    public class ConnectionLostArguments
    {
        public ConnectionThread ConnectionThread { get; private set; }
        public string Address { get; private set; }

        public ConnectionLostArguments(string address, ConnectionThread connectionThread)
        {
            Address = address;
            ConnectionThread = connectionThread;
        }
    }
}
