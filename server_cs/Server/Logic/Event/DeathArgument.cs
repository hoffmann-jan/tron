using System;
using System.Collections.Generic;
using System.Text;

namespace Server.Logic.Event
{
    public class DeathArgument : EventArgs
    {
        private int _Id;

        public int Id { get => _Id; }

        public DeathArgument(int id)
        {
            _Id = id;
        }
    }
}
