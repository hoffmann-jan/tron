using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;

using Server.Enum;
using Server.Logic.Event;
using Server.Protocol;

namespace Server.Logic
{
    /// <summary>
    /// Game instance.
    /// </summary>
    public class Tron
    {
        #region fields
        private const double FramesPerMillisecond = 1000d / 60d;
        /// <summary>
        /// Size of the quadratic field.
        /// </summary>
        private int _FliedSize;

        Random _Random = new Random();
        int _FramesToNextTail = 20;
        int _Length = 0;

        List<ExtendedPlayer> _Players;
        private byte _Starter = 0;
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
            _Players = new List<ExtendedPlayer>();
            _CancellationTokenSource = new CancellationTokenSource();
        }
        #endregion

        #region public functions

        /// <summary>
        /// Reguster player.
        /// </summary>
        /// <returns>Start position.</returns>
        public void RegisterPlayer(Player player)
        {
            ExtendedPlayer extendedPlayer = new ExtendedPlayer(player);
            extendedPlayer.Death = false;
            extendedPlayer.Length = 0;

            Position position = new Position();
            int halfSize = _FliedSize / 2;
            switch (_Starter)
            {
                case 0:
                    position.X = halfSize + 5;
                    position.Y = 0;
                    extendedPlayer.Direction = Direction.Up;
                    break;
                case 1:
                    position.X = 10;
                    position.Y = halfSize - 5;
                    extendedPlayer.Direction = Direction.Right;
                    break;
                case 2:
                    position.X = _FliedSize - 10;
                    position.Y = halfSize - 5;
                    extendedPlayer.Direction = Direction.Left;
                    break;
                case 3:
                    position.X = halfSize + 5;
                    position.Y = _FliedSize;
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
        public void StartGameLoop()
        {
            Task gameThread = Task.Run(() => GameLoop(), _CancellationTokenSource.Token);
        }

        /// <summary>
        /// Process action.
        /// </summary>
        /// <param name="protocol"></param>
        public void ProcessInput(Protocol.Protocol protocol, int playerId)
        {
            // Get player
            ExtendedPlayer player = _Players.FirstOrDefault(p => p.Player.Id == playerId);

            switch (protocol.Action)
            {
                case Protocol.Action.ACT_DOWN:
                    if (player.Direction == Direction.Up)
                        break;
                    player.Direction = Direction.Down;
                    break;
                case Protocol.Action.ACT_JUMP:
                    player.Player.Position.Jumping = true;
                    break;
                case Protocol.Action.ACT_LEFT:
                    if (player.Direction == Direction.Right)
                        break;
                    player.Direction = Direction.Left;
                    break;
                case Protocol.Action.ACT_RIGHT:
                    if (player.Direction == Direction.Left)
                        break;
                    player.Direction = Direction.Right;
                    break;
                case Protocol.Action.ACT_UP:
                    if (player.Direction == Direction.Down)
                        break;
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
        /// Collect data and provides.
        /// </summary>
        /// <param name="type"></param>
        private void Broadcast(Protocol.Type type = Protocol.Type.TYPE_UPDATE)
        {
            // Create snapshot.
            Protocol.Protocol protocol = new Protocol.Protocol();
            protocol.Type = type;
            protocol.Length = _Length;

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

            // Time for the client to build the interface(gui).
            Thread.Sleep(2000);

            bool end = false;

            double previous = GetCurrentTime();

            while (!end)
            {
                Thread.Sleep((int)FramesPerMillisecond);
                Update();
                //end = CheckGameEnd();
                Broadcast();
            }

            int winner = 0;
            var lastManStanding = _Players.FirstOrDefault(p => p.Death == false);
            if (lastManStanding != null)
                winner = lastManStanding.Player.Id;
            else
                winner = _Players.First(ep => ep.Length ==_Players.Select(p => p.Length).Max()).Player.Id;

            GameEnded?.Invoke(new GameEndedArguments(winner));
        }

        private void DetectCollisions()
        {
            foreach (var playerA in _Players)
            {
                if (playerA.Death)
                    continue;

                if (playerA.Player.Position.Jumping)
                    continue;

                foreach (var playerB in _Players)
                {
                    if (playerA.Equals(playerB))
                        continue;

                    if (playerB.Death)
                        continue;

                    var head = playerA.Player.Position;

                    // detect head on head collision
                    if (head.X == playerB.Player.Position.X
                        && head.Y == playerB.Player.Position.Y)
                    {
                        // One player is jumping
                        if ((playerA.Player.Position.Jumping && !playerB.Player.Position.Jumping)
                            || (playerB.Player.Position.Jumping && !playerA.Player.Position.Jumping))
                            continue;
                        else
                        {
                            IAmKilled(playerA.Player.Id);
                            IAmKilled(playerB.Player.Id);
                            break;
                        }
                    }

                    // detect head on tail collision
                    foreach (Point point in playerB.Tail)
                    {
                        if (head.X == point.X
                            && head.Y == point.Y)
                        {
                            IAmKilled(playerA.Player.Id);
                            break;
                        }
                    }
                }
            }
        }

        private void Update()
        {
            // Prcocess game.
            // Move players.
            Move();
            DetectCollisions();
            _FramesToNextTail--;

            if (_FramesToNextTail == 0)
            {
                _FramesToNextTail = _Random.Next(5, 26);
                AddTailSegment();
            }
        }

        private void AddTailSegment()
        {
            _Length++;
            foreach(var player in _Players)
            {
                if (player.Tail.Count == 0)
                {
                    Point t = new Point(player.Player.Position.X, player.Player.Position.Y);
                    player.Tail.Add(t);
                }
                else
                {
                    var lastTail = player.Tail.Last();
                    Point t = new Point(lastTail.X, lastTail.Y);
                }
            }
        }

        private bool CheckGameEnd()
        {
            var actives = _Players.Where(p => p.Death == false);
            if (actives.Count() <= 1)
            {
                // game end == true
                return true;
            }
            return false;
        }

        private void IAmKilled(int id)
        {
            _Players.First(p => p.Player.Id == id).Death = true;
            PlayerDied?.Invoke(new DeathArguments(id));
        }

        private void Move()
        {
            foreach (ExtendedPlayer player in _Players)
            {
                if (player.Death)
                    continue;

                switch (player.Direction)
                {
                    case Direction.Down:
                        player.Player.Position.Y += 1;
                        player.Player.Position.Y = Teleport(player.Player.Position.Y);
                        break;
                    case Direction.Left:
                        player.Player.Position.X -= 1;
                        player.Player.Position.X = Teleport(player.Player.Position.X);
                        break;
                    case Direction.Right:
                        player.Player.Position.X += 1;
                        player.Player.Position.X = Teleport(player.Player.Position.X);
                        break;
                    case Direction.Up:
                        player.Player.Position.Y -= 1;
                        player.Player.Position.Y = Teleport(player.Player.Position.Y);
                        break;
                }
            }
        }

        private int Teleport(int pos)
        {
            if (pos < 0)
                return _FliedSize;
            else if (pos > _FliedSize)
                return 0;
            return pos;
        }

        private Point[,] GetPlayerHead(int x, int y)
        {
            int size = 10;
            // x,y are top left
            // player size is 10x10
            Point[,] result = new Point[size, size];

            // bottom left
            x = x - size;
            int yyy;
            for (int xx = 0; xx < size; xx++)
            {
                yyy = y;

                for (int yy = 0; yy < size; yy++)
                {
                    result[xx, yy] = new Point(x, yyy++);
                }

                x++;
            }

            return result;
        }

        private Point[,] GetSegment(int x, int y)
        {
            int size = 4;
            // x,y are top left
            // segment size is 4x4
            Point[,] result = new Point[size, size];

            // bottom left
            x = x - size;
            int yyy;
            for (int xx = 0; xx < size; xx++)
            {
                yyy = y;

                for (int yy = 0; yy < size; yy++)
                {
                    result[xx, yy] = new Point(x, yyy++);
                }

                x++;
            }

            return result;
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


        public delegate void PlayerDeathHandler(DeathArguments d);
        public event PlayerDeathHandler PlayerDied;

        public delegate void GameEndedHandler(GameEndedArguments g);
        public event GameEndedHandler GameEnded;
        #endregion
    }
}
