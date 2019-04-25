#pragma once

#include <SFML/Graphics.hpp>

#include "messagequeue.h"
#include "state.h"

class Client
{
public:
    Client();
    ~Client();

    void run();

private:
    void processConnect(const Message& message);
    void processLobby(const Message& message);
    void processAdd(const Message& message);
    void processStart(const Message& message);
    void processUpdate(const Message& message);
    void processDead(const Message& message);

    void renderConnect();
    void renderLobby();
    void renderGame();

    sf::Window window;
    bool closed;
    
    State state;

    MessageQueue queue;
    Message message;
};

