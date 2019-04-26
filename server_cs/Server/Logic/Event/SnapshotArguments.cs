using System;

namespace Server.Logic.Event
{
    /// <summary>
    /// Snapshot of the game state.
    /// </summary>
    public class SnapshotArguments : EventArgs
    {
        private Protocol.Protocol _Protocol;

        public Protocol.Protocol Protocol { get => _Protocol; }

        public SnapshotArguments(Protocol.Protocol protocol)
        {
            _Protocol = protocol;
        }
    }
}
