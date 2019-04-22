using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;

using Server.Enum;
using Server.Logic.Event;
using System;
using Server.Messages;

namespace Server.Logic
{
    /// <summary>
    /// Game instance.
    /// </summary>
    public class Tron
    {
        #region fields
        /// <summary>
        /// Speed of a player. 500 field row / 60 sec => 1 field per second.
        /// </summary>
        private const float PlayerSpeed = 500f / 60f;
        private const double FramesPerSecond = 1d / 60d;
        /// <summary>
        /// Size of the quadratic field.
        /// </summary>
        private int _FliedSize;
        /// <summary>
        /// Maps player id to enum number.
        /// </summary>
        private PlayerIdPlayerNumberMap _Map;

        List<TronPlayer> _Players;
        #endregion

        #region properties

        #endregion

        #region ctor
        /// <summary>
        /// Ctor.
        /// </summary>
        public Tron(int fieldSize)
        {
            _FliedSize = fieldSize;
            _Map = new PlayerIdPlayerNumberMap();
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
            _Players = new List<TronPlayer>();
            foreach(int id in playerIds)
            {
                TronPlayer tronPlayer = new TronPlayer(id);
                _Players.Add(tronPlayer);
            }

            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
            CancellationToken cancellationToken = cancellationTokenSource.Token;
            Task gameThread = Task.Run(() => GameLoop(gameField, _Players), cancellationToken);
        }
        #endregion

        #region private functions
        /// <summary>
        /// Game loop.
        /// </summary>
        private void GameLoop(FieldInformation[,] gameField, List<TronPlayer> players)
        {
            bool end = false;
            InitEmptyGameField(gameField);
            SetUpPlayers(gameField, players);

            double previous = GetCurrentTime();
            double lag = 0.0d;

            while (!end)
            {
                double current = GetCurrentTime();
                double elapsed = current - previous;
                previous = current;
                lag += elapsed;

                while (lag >= FramesPerSecond)
                {
                    Update(gameField, players, elapsed);
                    lag -= FramesPerSecond;
                }

                Broadcast(gameField, players);
            }

        }

        private void SetUpPlayers(FieldInformation[,] gameField, List<TronPlayer> players)
        {
            int halfSize = _FliedSize / 2;
            foreach (TronPlayer player in players)
            {
                Coordinates coordinates = player.Coordinates;
                coordinates.id = player.Id;

                switch (player.StartPosition)
                {
                    case StartPosition.bottom:
                        coordinates.x = halfSize; 
                        coordinates.y = 0;
                        gameField[0, halfSize] = FieldInformation.Player1;
                        _Map.Player1 = player.Id;
                        break;
                    case StartPosition.left:
                        coordinates.x = 0;
                        coordinates.y = halfSize;
                        gameField[0, halfSize] = FieldInformation.Player2;
                        _Map.Player2 = player.Id;
                        break;
                    case StartPosition.right:
                        coordinates.x = _FliedSize;
                        coordinates.y = halfSize;
                        gameField[0, halfSize] = FieldInformation.Player3;
                        _Map.Player3 = player.Id;
                        break;
                    case StartPosition.top:
                        coordinates.x = halfSize;
                        coordinates.y = _FliedSize;
                        gameField[0, halfSize] = FieldInformation.Player4;
                        _Map.Player4 = player.Id;
                        break;
                }
                player.Coordinates = coordinates;
            }
        }

        private void Broadcast(FieldInformation[,] gameField, List<TronPlayer> players)
        {
            // Create snapsot.
            SnapshotArguments snapshot = new SnapshotArguments();

            foreach (TronPlayer player in players)
            {
                Message message = new Message();
                message.type = Enum.Type.TYPE_UPDATE;
                message.id = player.Id;
                message.coordinates = player.Coordinates;
                Field field = new Field();
                field.heigth = _FliedSize;
                field.width = _FliedSize;
                message.field = field;

                snapshot.Messages.Add(message);
            }

            // Provide for broadcasting.
            SnapshotCreated?.Invoke(snapshot);
        }

        private void Update(FieldInformation[,] gameField, List<TronPlayer> players, double deltaTime)
        {
            // Prcocess game.
            // Move players.
            Move(gameField, players, deltaTime);

            // Detect collisions.
            foreach (TronPlayer player in players)
            {
                IEnumerable<TronPlayer> others = players.Where(e => e.Id != player.Id);
                DetectCollision(others);
            }
        }

        public void ProcessInput(List<TronPlayer> players)
        {

        }

        /// <summary>
        /// Get the current time in milliseconds
        /// </summary>
        /// <returns></returns>
        private double GetCurrentTime()
        {
            return DateTime.UtcNow.Millisecond / 1000d;
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

        private bool DetectCollision(TronPlayer other)
        {
            bool result = false;
            return result;
        }

        internal void ProcessInput(Message message)
        {
            switch (message.type)
            {
                case Enum.Type.TYPE_MOVE:
                    TronPlayer player =_Players.FirstOrDefault(p => p.Id == message.id);
                    if (message.move == Enum.Move.MOVE_JUMP)
                        player.Jump = true;
                    else
                        player.Direction = message.move;
                    break;
            }
        }

        private bool DetectCollision(IEnumerable<TronPlayer> others)
        {
            foreach (TronPlayer player in others)
            {
                if (DetectCollision(player))
                    return true;
            }
            return false;
        }

        private void Move(FieldInformation[,] gameField, List<TronPlayer> players, double deltaTime)
        {
            foreach (TronPlayer player in players)
            {
                Coordinates oldPosition = player.Coordinates;
                Coordinates newPosition = player.Coordinates;
                switch (player.Direction)
                {
                    case Enum.Move.MOVE_DOWN:
                        newPosition.y -= PlayerSpeed * deltaTime;
                        break;
                    case Enum.Move.MOVE_LEFT:
                        newPosition.y -= PlayerSpeed * deltaTime;
                        break;
                    case Enum.Move.MOVE_RIGHT:
                        newPosition.x += PlayerSpeed * deltaTime;
                        break;
                    case Enum.Move.MOVE_UP:
                        newPosition.y += PlayerSpeed * deltaTime;
                        break;
                }

                player.Coordinates = newPosition;

                if (gameField[(int)player.Coordinates.x, (int)player.Coordinates.y] == FieldInformation.Empty)
                {
                    gameField[(int)player.Coordinates.x, (int)player.Coordinates.y] = GetPlayerById(player.Id);
                    gameField[(int)oldPosition.x, (int)oldPosition.y] = GetPlayerSegemntById(player.Id);
                }

            }
        }

        private FieldInformation GetPlayerById(int id)
        {
            if (_Map.Player1 == id)
                return FieldInformation.Player1;
            else if (_Map.Player2 == id)
                return FieldInformation.Player2;
            else if (_Map.Player3 == id)
                return FieldInformation.Player3;
            else
                return FieldInformation.Player4;
        }

        private FieldInformation GetPlayerSegemntById(int id)
        {
            if (_Map.Player1 == id)
                return FieldInformation.PlayerSegment1;
            else if (_Map.Player2 == id)
                return FieldInformation.PlayerSegment2;
            else if (_Map.Player3 == id)
                return FieldInformation.PlayerSegment3;
            else
                return FieldInformation.PlayerSegment4;
        }


        #endregion

        #region events
        /// <summary>
        /// Occurs when a new snapshot is created.
        /// </summary>
        /// <param name="s">SnapshotArguments.</param>
        public delegate void SnapshotHandler(SnapshotArguments s);
        /// <summary>
        /// Occurs when a new snapshot is created.
        /// </summary>
        public event SnapshotHandler SnapshotCreated;
        #endregion
    }
}
