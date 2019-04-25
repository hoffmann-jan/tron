#include "state.h"

State::State()
    : screen(Screen::Connect)
{
    players.reserve(1);
    
    self = &players[0];
}
