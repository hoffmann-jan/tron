using Server.Enum;
using System;
using System.Collections.Generic;
using System.Text;

namespace Server.Logic
{
    /// <summary>
    /// Client.
    /// </summary>
    public class TronPlayer
    {
        #region fields
        private readonly int _Id;
        private Messages.ClientMessage.Move _Direction = Messages.ClientMessage.Move.MOVE_RIGHT;
        #endregion

        #region properties
        /// <summary>
        /// Player Id.
        /// </summary>
        public int Id { get => _Id; }
        #endregion

        #region ctor
        /// <summary>
        /// Empty ctor.
        /// </summary>
        public TronPlayer(int id, ref FieldInformation[,] gameField)
        {
            _Id = id;
        }
        #endregion

        #region public functions
        public bool DetectCollision(TronPlayer other)
        {
            bool result = false;
            return result;
        }

        public bool DetectCollision(TronPlayer[] others)
        {
            bool result = false;
            foreach(TronPlayer player in others)
            {
                if (DetectCollision(player))
                    return !result;
            }
            return result;
        }

        public void Move()
        {

        }
        #endregion

        #region private functions

        #endregion
    }

    /// <summary>
    /// Player start position.
    /// </summary>
    public enum Startposition
    {
        left,
        right,
        top,
        bottom
    }
}
