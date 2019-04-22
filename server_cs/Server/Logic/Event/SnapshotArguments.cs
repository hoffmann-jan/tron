using System;
using System.Collections.Generic;

namespace Server.Logic.Event
{
    /// <summary>
    /// Snapshot of the game state.
    /// </summary>
    public class SnapshotArguments : EventArgs
    {
        private List<Message> _Messages;

        public List<Message> Messages { get => _Messages; }

        public SnapshotArguments()
        {
            _Messages = new List<Message>();
        }
    }
}
