using System;

namespace Server.Logic.Event
{
    public class DeathArguments : EventArgs
    {
        private int _Id;

        public int Id { get => _Id; }

        public DeathArguments(int id)
        {
            _Id = id;
        }
    }
}
