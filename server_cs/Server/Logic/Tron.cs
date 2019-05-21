using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;

using Server.Enum;
using Server.Logic.Event;
using Server.Protocol;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Collections.Concurrent;
using Server.Logic.Player;

namespace Server.Logic
{
    /// <summary>
    /// Game instance.
    /// </summary>
    public class Tron
    {
        #region fields
        private const double FramesPerMillisecond = 1000d / 30d;
        /// <summary>
        /// Size of the quadratic field.
        /// </summary>
        private readonly int _FliedSize;

        Random _Random = new Random();
        int _FramesToNextTail = 20;
        int _Length = 1;
        byte _PlayerSize = 10;
        byte _SegmentSize = 5;
        byte _JumpLength = 16;
        byte _MoveSpeed = 3;

        List<ExtendedPlayer> _Players;
        private byte _Starter = 0;
        CancellationTokenSource _CancellationTokenSource;
        Point _LastCollisionPoint;
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
        public void RegisterPlayer(Protocol.Player player)
        {
            ExtendedPlayer extendedPlayer = new ExtendedPlayer(player);
            extendedPlayer.Death = false;
            extendedPlayer.Length = 0;

            Position position = new Position();
            int halfSize = _FliedSize / 2;
            switch (_Starter)
            {
                case 0:
                    position.X = halfSize + (_PlayerSize / 2);
                    position.Y = 0;
                    extendedPlayer.Direction = Direction.Up;
                    break;
                case 1:
                    position.X = _PlayerSize;
                    position.Y = halfSize - (_PlayerSize / 2);
                    extendedPlayer.Direction = Direction.Right;
                    break;
                case 2:
                    position.X = _FliedSize - _PlayerSize;
                    position.Y = halfSize - (_PlayerSize / 2);
                    extendedPlayer.Direction = Direction.Left;
                    break;
                case 3:
                    position.X = halfSize + (_PlayerSize / 2);
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
            StartGameLoopInternal();
        }

        public void RestartGameLoop(ConcurrentDictionary<int, ClientInfo> clients)
        {
            _CancellationTokenSource = new CancellationTokenSource();
            _Players.Clear();
            _Starter = 0;
            _Length = 1;
            foreach (var client in clients)
            {
                RegisterPlayer(client.Value.Player);
            }

            StartGameLoopInternal();
        }

        public void StopGameLoop()
        {
            _CancellationTokenSource.Cancel();
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
                    if (player.Jumping == 0)
                    {
                        player.Jumping = _JumpLength;
                        player.Player.Position.Jumping = true;
                    }
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
        /// Collect data and provides.
        /// </summary>
        /// <param name="type"></param>
        private void Broadcast(Protocol.Type type = Protocol.Type.TYPE_UPDATE)
        {
            // Create snapshot.
            Protocol.Protocol protocol = new Protocol.Protocol
            {
                Type = type,
                Length = _Length
            };

            foreach (ExtendedPlayer player in _Players)
            {
                if (player.Jumping > 0)
                    player.Player.Position.Jumping = true;
                else
                    player.Player.Position.Jumping = false;

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

            while (!end)
            {
                Thread.Sleep((int)FramesPerMillisecond);
                Update();
                end = CheckGameEnd();
                Broadcast();
            }

            int winner = 0;
            var lastManStanding = _Players.FirstOrDefault(p => p.Death == false);
            if (lastManStanding != null)
                winner = lastManStanding.Player.Id;
            else
                winner = _Players.First(ep => ep.Length ==_Players.Select(p => p.Length).Max()).Player.Id;
#if DEBUG
            DrawStateAndSave();
#endif
            GameEnded?.Invoke(new GameEndedArguments(winner));
        }

        private void StartGameLoopInternal()
        {
            Task.Factory.StartNew(
                GameLoop,
                _CancellationTokenSource.Token,
                TaskCreationOptions.LongRunning,
                TaskScheduler.Default);
        }

        private void DrawStateAndSave()
        {
            if (Environment.OSVersion.Platform == PlatformID.Unix)
                return;

            try
            {
                Bitmap bitmap = new Bitmap(_FliedSize, _FliedSize, PixelFormat.Format32bppArgb);

                for (int x = 0; x < _FliedSize; x++)
                {
                    for (int y = 0; y < _FliedSize; y++)
                    {
                        bitmap.SetPixel(x, y, Color.DimGray);
                    }
                }

                foreach (var player in _Players)
                {
                    Color color = Color.FromArgb(player.Player.Color);

                    foreach (Point point in GetFrame(player.Player.Position.X, player.Player.Position.Y, _PlayerSize))
                    {
                        bitmap.SetPixel(SetPixel(point.X), SetPixel(point.Y), color);
                    }

                    foreach (var tail in player.Tail)
                    {
                        foreach (var f in GetFrame(tail.Position.X, tail.Position.Y, _SegmentSize))
                        {
                            bitmap.SetPixel(SetPixel(f.X), SetPixel(f.Y), Color.Red);
                        }
                    }
                }

                // Draw last collision
                Color colour = Color.LimeGreen;
                bitmap.SetPixel(_LastCollisionPoint.X, _LastCollisionPoint.Y, colour);
                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X + 1), SetPixel(_LastCollisionPoint.Y + 1), colour);
                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X + 2), SetPixel(_LastCollisionPoint.Y + 2), colour);

                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X + 1), SetPixel(_LastCollisionPoint.Y - 1), colour);
                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X + 2), SetPixel(_LastCollisionPoint.Y - 2), colour);

                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X - 1), SetPixel(_LastCollisionPoint.Y + 1), colour);
                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X - 2), SetPixel(_LastCollisionPoint.Y + 2), colour);

                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X - 1), SetPixel(_LastCollisionPoint.Y - 1), colour);
                bitmap.SetPixel(SetPixel(_LastCollisionPoint.X - 2), SetPixel(_LastCollisionPoint.Y - 2), colour);

                string path = Path.Combine(Directory.GetCurrentDirectory(), "snapshots"); 

                if (!Directory.Exists(path))
                {
                    Directory.CreateDirectory(path);
                }

                bitmap.Save(Path.Combine(path, $"snapshot{DateTime.Now.ToString("yyyyMMddHHmmss")}.png"), ImageFormat.Png);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
        }

        private int SetPixel(int pos)
        {
            pos += _FliedSize;
            pos = pos % _FliedSize;
            return pos;
        }

        private void DetectCollisions()
        {
            foreach (var playerA in _Players)
            {
                if (playerA.Death)
                    continue;

                if (playerA.Jumping > 0)
                    continue;

                foreach (var playerB in _Players)
                {

                    if (playerB.Death)
                        continue;

                    var headA = playerA.Player.Position;
                    var headArrayA = GetFrame(headA.X, headA.Y, _PlayerSize);

                    // if player a and B are the same, only detect collisions with the own tail
                    if (!playerA.Equals(playerB))
                    {
                        var headB = playerB.Player.Position;
                        var headArrayB = GetFrame(headB.X, headB.Y, _PlayerSize);

                        // detect head on head collision
                        foreach (Point pointA in headArrayA)
                        {
                            if (playerA.Death)
                                break;

                            // optimize: if headB is not in range => skip
                            if (headB.X > (headA.X + _PlayerSize)
                                || (headB.X + _PlayerSize) < headA.X

                                || (headB.Y - _PlayerSize) > headA.Y
                                || headB.Y < (headA.Y - _PlayerSize))
                                break;


                            foreach (Point pointB in headArrayB)
                            {
                                // One player is jumping
                                if (((playerA.Jumping > 0) && !(playerB.Jumping > 0))
                                    || ((playerB.Jumping > 0) && !(playerA.Jumping > 0)))
                                    break;

                                if (pointA.X == pointB.X
                                    && pointA.Y == pointB.Y)
                                {
                                    // head on head => both players killed
                                    IAmKilled(playerA.Player.Id);
                                    IAmKilled(playerB.Player.Id);
                                    _LastCollisionPoint = pointA;
                                    break;
                                }
                            }
                        }
                    }

                    // detect head on tail collision
                    // player has tail
                    if (playerB.Length > _PlayerSize)
                    {
                        foreach (var segmentPart in playerB.Tail)
                        {
                            if (playerA.Death)
                                break;

                            // if player a and B are the same, only detect collisions with the own tail after 5 Segments
                            if (playerA.Equals(playerB))
                            {
                                if (segmentPart.RowCount < 5)
                                    continue;
                            }
                            else
                            {
                                // optimize: if head is not in range of segment => skip
                                if (segmentPart.Position.X > (headA.X + _PlayerSize)
                                || (segmentPart.Position.X + _PlayerSize) < headA.X

                                || (segmentPart.Position.Y - _PlayerSize) > headA.Y
                                || segmentPart.Position.Y < (headA.Y - _PlayerSize))
                                    continue;
                            }


                            var segmentArray = GetFrame(segmentPart.Position.X, segmentPart.Position.Y, _SegmentSize);
                            foreach (Point head in headArrayA)
                            {
                                if (playerA.Death)
                                    break;

                                foreach (Point segmentPoint in segmentArray)
                                {
                                    if (segmentPoint.X == head.X
                                        && segmentPoint.Y == head.Y)
                                    {
                                        IAmKilled(playerA.Player.Id);
                                        _LastCollisionPoint = head;
                                        break;
                                    }
                                }
                            }
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
                _Length++;

                foreach (var player in _Players)
                {
                    player.Length++;
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

                if (player.Jumping > 0)
                    player.Player.Position.Jumping = true;
                else
                    player.Player.Position.Jumping = false;

                foreach(var tail in player.Tail)
                {
                    tail.RowCount++;
                }

                player.Tail.Enqueue(new TailSegment(new Point(player.Player.Position.X + ((_PlayerSize - _SegmentSize) / 2), player.Player.Position.Y - ((_PlayerSize - _SegmentSize) / 2)), 1));

                switch (player.Direction)
                {
                    case Direction.Down:
                        player.Player.Position.Y += _MoveSpeed;
                        player.Player.Position.Y = Teleport(player.Player.Position.Y);
                        break;
                    case Direction.Left:
                        player.Player.Position.X -= _MoveSpeed;
                        player.Player.Position.X = Teleport(player.Player.Position.X);
                        break;
                    case Direction.Right:
                        player.Player.Position.X += _MoveSpeed;
                        player.Player.Position.X = Teleport(player.Player.Position.X);
                        break;
                    case Direction.Up:
                        player.Player.Position.Y -= _MoveSpeed;
                        player.Player.Position.Y = Teleport(player.Player.Position.Y);
                        break;
                }

                while (player.Tail.Count > _Length - 1)
                {
                    player.Tail.Dequeue();
                }

                if (player.Jumping > 0)
                    player.Jumping--;
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

        private List<Point> GetFrame(int x, int y, int size)
        {
            // x,y are top left
            // segment size is size x size
            // only gets the frame
            List<Point> result = new List<Point>();

            for (int ix = 0; ix < size; ix++)
            {
                result.Add(new Point(x + ix, y));
                result.Add(new Point(x + ix, y - (size - 1)));
            }

            for (int iy = 1; iy < size - 1; iy++)
            {
                result.Add(new Point(x, y - iy));
                result.Add(new Point(x + size - 1, y - iy));
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
