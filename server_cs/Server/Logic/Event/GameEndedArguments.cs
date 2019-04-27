using System;

namespace Server.Logic.Event
{
    public class GameEndedArguments : EventArgs
    {
        private int _Id;

        public int Id { get => _Id; }

        public GameEndedArguments(int id)
        {
            _Id = id;
        }
    }
}
