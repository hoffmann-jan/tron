using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;

using Server.Enum;
using Server.Logic.Event;
using System;
using Server.Protocol;

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

        List<ExtendedPlayer> _Players;
        private byte _Starter = 0;
        FieldInformation[,] _GameField;
        CancellationTokenSource _CancellationTokenSource;
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
            _Players = new List<ExtendedPlayer>();
            _CancellationTokenSource = new CancellationTokenSource();
            InitEmptyGameField();
        }
        #endregion

        #region public functions

        /// <summary>
        /// Reguster player.
        /// </summary>
        /// <param name="playerId">Player id.</param>
        /// <returns>Start position.</returns>
        public void RegisterPlayer(Player player)
        {
            ExtendedPlayer extendedPlayer = new ExtendedPlayer(player);
            Position position = new Position();
            int halfSize = _FliedSize / 2;
            switch (_Starter)
            {
                case 0:
                    position.X = halfSize;
                    position.Y = 0;
                    _GameField[0, halfSize] = FieldInformation.Player1;
                    _Map.Player1 = player.Id;
                    extendedPlayer.Direction = Direction.Up;
                    break;
                case 1:
                    position.X = 0;
                    position.Y = halfSize;
                    _GameField[0, halfSize] = FieldInformation.Player2;
                    _Map.Player2 = player.Id;
                    extendedPlayer.Direction = Direction.Right;
                    break;
                case 2:
                    position.X = _FliedSize;
                    position.Y = halfSize;
                    _GameField[0, halfSize] = FieldInformation.Player3;
                    _Map.Player3 = player.Id;
                    extendedPlayer.Direction = Direction.Left;
                    break;
                case 3:
                    position.X = halfSize;
                    position.Y = _FliedSize;
                    _GameField[0, halfSize] = FieldInformation.Player4;
                    _Map.Player4 = player.Id;
                    extendedPlayer.Direction = Direction.Down;
                    break;
                default:
                    break;
            }
            _Starter++;
            player.Position = position;
            _Players.Add(extendedPlayer);
            return;
        }

        /// <summary>
        /// Start a new game in a new thread.
        /// </summary>
        /// <param name="playerIds"></param>
        public void StartGameLoop()
        {
            Task gameThread = Task.Run(GameLoop, _CancellationTokenSource.Token);
        }

        /// <summary>
        /// Process action.
        /// </summary>
        /// <param name="protocol"></param>
        public void ProcessInput(Protocol.Protocol protocol)
        {
            // Get player
            ExtendedPlayer player = _Players.FirstOrDefault(p => p.Player.Id == protocol.LobbyId);

            switch (protocol.Action)
            {
                case Protocol.Action.ACT_DOWN:
                    player.Direction = Direction.Down;
                    break;
                case Protocol.Action.ACT_JUMP:
                    player.Player.Position.Jumping = true;
                    break;
                case Protocol.Action.ACT_LEFT:
                    player.Direction = Direction.Left;
                    break;
                case Protocol.Action.ACT_RIGHT:
                    player.Direction = Direction.Right;
                    break;
                case Protocol.Action.ACT_UP:
                    player.Direction = Direction.Up;
                    break;
            }
        }
        #endregion

        #region private functions

        /// <summary>
        /// Get the current time in milliseconds
        /// </summary>
        /// <returns></returns>
        private double GetCurrentTime()
        {
            return DateTime.UtcNow.Millisecond;
        }

        /// <summary>
        /// Initialise the game field with all fields empty.
        /// </summary>
        /// <param name="gameField"></param>
        private void InitEmptyGameField()
        {
            _GameField = new FieldInformation[_FliedSize, _FliedSize];
            for (int x = 0; x < _FliedSize; x++)
            {
                for (int y = 0; y < _FliedSize; y++)
                {
                    _GameField[x, y] = FieldInformation.Empty;
                }
            }
        }

        /// <summary>
        /// Collect data and provides.
        /// </summary>
        /// <param name="type"></param>
        private void Broadcast(Protocol.Type type = Protocol.Type.TYPE_UPDATE)
        {
            // Create snapshot.
            Protocol.Protocol protocol = new Protocol.Protocol();
            protocol.Type = type;

            foreach (ExtendedPlayer player in _Players)
            {
                protocol.Players.Add(player.Player);
            }

            // Provide for broadcasting
            SnapshotCreated?.Invoke(new SnapshotArguments(protocol));
        }

        /// <summary>
        /// Game loop.
        /// </summary>
        private void GameLoop()
        {
            Broadcast(Protocol.Type.TYPE_START);

            bool end = false;

            double previous = GetCurrentTime();

            while (!end)
            {
                double current = GetCurrentTime();
                double elapsed = current - previous;
                previous = current;

                while (elapsed * 1000 < FramesPerSecond)
                {
                    Thread.Sleep(10);
                }
                Update(elapsed);
                Broadcast();
            }

        }

        private void Update(double deltaTime)
        {
            // Prcocess game.
            // Move players.
            Move(deltaTime);

            // Detect collisions and register postion
            foreach (ExtendedPlayer player in _Players)
            {
                Draw(player);
            }
        }

        private void Draw(ExtendedPlayer player)
        {
            switch (_GameField[(int)player.Player.Position.X, (int)player.Player.Position.Y])
            {
                case FieldInformation.Empty:
                    _GameField[(int)player.Player.Position.X, (int)player.Player.Position.Y] = GetPlayerById(player.Player.Id);
                    break;

                case FieldInformation.Player1:
                    if (GetPlayerById(player.Player.Id) != FieldInformation.Player1)
                        IAmKilled(player.Player.Id);
                    break;

                case FieldInformation.Player2:
                    if (GetPlayerById(player.Player.Id) != FieldInformation.Player2)
                        IAmKilled(player.Player.Id);
                    break;

                case FieldInformation.Player3:
                    if (GetPlayerById(player.Player.Id) != FieldInformation.Player3)
                        IAmKilled(player.Player.Id);
                    break;

                case FieldInformation.Player4:
                    if (GetPlayerById(player.Player.Id) != FieldInformation.Player4)
                        IAmKilled(player.Player.Id);
                    break;

                case FieldInformation.PlayerSegment1:
                case FieldInformation.PlayerSegment2:
                case FieldInformation.PlayerSegment3:
                case FieldInformation.PlayerSegment4:
                case FieldInformation.Cross:
                    IAmKilled(player.Player.Id);
                    break;
            }

        }

        private void IAmKilled(int id)
        {
            _Players.First(p => p.Player.Id == id).Death = true;
            PlayerDied?.Invoke(new DeathArgument(id));
            RemoveFromGameField(GetPlayerById(id), GetPlayerSegemntById(id));
        }

        private void RemoveFromGameField(FieldInformation player, FieldInformation segment)
        {
            for (int x = 0; x < _FliedSize; x++)
            {
                for (int y = 0; y < _FliedSize; y++)
                {
                    if (_GameField[x, y] == player
                        || _GameField[x, y] == segment)
                        _GameField[x, y] = FieldInformation.Empty;
                }
            }
        }

        private void Move(double deltaTime)
        {
            foreach (ExtendedPlayer player in _Players)
            {
                Position oldPosition = player.Player.Position;
                Position newPosition = player.Player.Position;
                switch (player.Direction)
                {
                    case Direction.Down:
                        newPosition.Y -= (int) (PlayerSpeed * deltaTime);
                        break;
                    case Direction.Left:
                        newPosition.Y -= (int)(PlayerSpeed * deltaTime);
                        break;
                    case Direction.Right:
                        newPosition.X += (int)(PlayerSpeed * deltaTime);
                        break;
                    case Direction.Up:
                        newPosition.Y += (int)(PlayerSpeed * deltaTime);
                        break;
                }

                player.Player.Position = newPosition;
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

        //private bool DetectCollision(ExtendedPlayer other)
        //{
        //    bool result = false;
        //    return result;
        //}

        //private bool DetectCollision(IEnumerable<ExtendedPlayer> others)
        //{
        //    foreach (ExtendedPlayer player in others)
        //    {
        //        if (DetectCollision(player))
        //            return true;
        //    }
        //    return false;
        //}


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


        public delegate void PlayerDeathHandler(DeathArgument d);
        public event PlayerDeathHandler PlayerDied;
        #endregion
    }
}
