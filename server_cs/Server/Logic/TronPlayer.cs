using Server.Enum;
using Server.Messages;

namespace Server.Logic
{
    /// <summary>
    /// Client.
    /// </summary>
    public class TronPlayer
    {
        #region fields
        private readonly int _Id;
        private bool _Jump;
        private Coordinates _Coordinates;
        private Move _Direction;
        private StartPosition _StartPosition;
        #endregion

        #region properties
        /// <summary>
        /// Player Id.
        /// </summary>
        public int Id { get => _Id; }
        /// <summary>
        /// Player jumping.
        /// </summary>
        public bool Jump { get => _Jump; set => _Jump = value; }
        /// <summary>
        /// Coordinates.
        /// </summary>
        public Coordinates Coordinates { get => _Coordinates; set => _Coordinates = value; }
        /// <summary>
        /// Direction.
        /// </summary>
        public Move Direction { get => _Direction; set => _Direction = value; }
        /// <summary>
        /// Start position.
        /// </summary>
        public StartPosition StartPosition { get => _StartPosition; set => _StartPosition = value; }
        #endregion

        #region ctor
        /// <summary>
        /// Ctor.
        /// </summary>
        public TronPlayer(int id, Move direction = Move.MOVE_RIGHT, StartPosition startPosition = StartPosition.left)
        {
            _Id = id;
            _Direction = direction;
            _StartPosition = startPosition;
        }
        #endregion

        #region public functions

        #endregion

        #region private functions

        #endregion
    }

    /// <summary>
    /// Player start position.
    /// </summary>
    public enum StartPosition
    {
        left = 0,
        right = 1,
        top = 2,
        bottom = 3
    }
}
