#pragma once

#include <list>
#include <vector>

#include "message/player.h"
#include "message/position.h"

class State
{
public:
    State();

    enum class Screen { Connect, Lobby, Game } screen;

    int length;

    struct Player : public ::Player
    {
        bool dead;
        bool ready;  
        std::list<Position> trail;
    };
    std::vector<Player> players;

    Player* self;
};
