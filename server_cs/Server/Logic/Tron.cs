using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using Server.Enum;

namespace Server.Logic
{
    /// <summary>
    /// Game instance.
    /// </summary>
    public class Tron
    {
        #region fields
        /// <summary>
        /// Speed of the game;
        /// </summary>
        private float _GameSpeed = 500f / 60f;
        /// <summary>
        /// Size of the quadratic field.
        /// </summary>
        private int _FliedSize;
        #endregion

        #region properties

        #endregion

        #region ctor
        /// <summary>
        /// Empty ctor.
        /// </summary>
        public Tron(int fieldSize)
        {
            _FliedSize = fieldSize;
        }
        #endregion

        #region public functions
        /// <summary>
        /// Start a new game in a new thread.
        /// </summary>
        /// <param name="playerIds"></param>
        public void StartGameLoop(int[] playerIds)
        {
            FieldInformation[,] gameField = new FieldInformation[_FliedSize, _FliedSize];
            List<TronPlayer> players = new List<TronPlayer>();
            foreach(int id in playerIds)
            {
                TronPlayer tronPlayer = new TronPlayer(id, ref gameField);
                players.Add(tronPlayer);
            }

            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
            CancellationToken cancellationToken = cancellationTokenSource.Token;
            Task gameThread = Task.Run(() => GameLoop(gameField), cancellationToken);
        }
        #endregion

        #region private functions
        /// <summary>
        /// Game loop.
        /// </summary>
        private void GameLoop(FieldInformation[,] gameField)
        {
            InitEmptyGameField(gameField);

            while (true)
            {
                // Set frame rate, fps.
                Thread.Sleep((int)(1000 / _GameSpeed));
            }
        }

        /// <summary>
        /// Initialise the game field with all fields empty.
        /// </summary>
        /// <param name="gameField"></param>
        private void InitEmptyGameField(FieldInformation[,] gameField)
        {
            for (int x = 0; x < _FliedSize; x++)
            {
                for (int y = 0; y < _FliedSize; y++)
                {
                    gameField[x, y] = FieldInformation.Empty;
                }
            }
        }
        #endregion

        #region events

        #endregion
    }
}
