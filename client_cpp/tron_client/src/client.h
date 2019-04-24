#pragma once

#include <SFML/Graphics.hpp>

#include "messagequeue.h"

class Client
{
public:
    Client();
    ~Client();

    void run();

private:
    void processMessage(const Message& message);

    void renderConnect();
    void renderLobby();
    void renderGame();

    sf::Window window;
    
    enum class State { Connect, Lobby, Game } state;

    MessageQueue queue;
    Message message;
};

