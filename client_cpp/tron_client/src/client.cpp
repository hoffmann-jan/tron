#include "client.h"

Client::Client()
    : window(
        sf::VideoMode(500, 500), 
        "Tron C++ Client", 
        sf::Style::Default
    )
{
    window.setActive(true);
}

Client::~Client()
{
    window.close();
}

void Client::run()
{
    while (window.isOpen() && !closed)
    {
        if (!queue.isEmpty())
        {
            Message message = queue.dequeue();

            switch (message.type)
            {
            case TYPE_CONNECT:
                processConnect(message);
                break;

            case TYPE_LOBBY:
                processLobby(message);
                break;

            case TYPE_ADD:
                processAdd(message);
                break;

            case TYPE_UPDATE:
                processUpdate(message);
                break;
            }
        }

        switch (state.screen)
        {
        case State::Screen::Connect:
            renderConnect();
            break;

        case State::Screen::Lobby:
            renderLobby();
            break;

        case State::Screen::Game:
            renderGame();
            break;
        }
        window.display();
    }
}

void Client::processConnect(const Message& message)
{
    state.self->id = message.players[0].id;
}

void Client::processLobby(const Message& message)
{
    state.screen = State::Screen::Lobby;

    for (const Player& p : message.players)
    {
        if (p.id != state.self->id)
        {
            State::Player sp;

            sp.id       = p.id;
            sp.color    = p.color;
            sp.name     = p.name;
            sp.position = p.position;
            sp.ready    = false;

            state.players.push_back(sp);
        }
    }
}

void Client::processAdd(const Message& message)
{
    State::Player sp;

    sp.id       = message.players[0].id;
    sp.color    = message.players[0].color;
    sp.name     = message.players[0].name;
    sp.position = message.players[0].position;
    sp.ready    = false;

    state.players.push_back(sp);
}

void Client::processStart(const Message& message)
{
    state.screen = State::Screen::Game;
}

void Client::processUpdate(const Message& message)
{
    state.length = message.length;

    for (const Player& p : message.players)
    {
        for (State::Player &sp : state.players)
        {
            if (sp.dead || p.id != sp.id)
                continue;

            sp.position = p.position;

            if (state.length == sp.trail.size())
                sp.trail.pop_back();

            sp.trail.push_front(sp.position);
        }
    }
}

void Client::processDead(const Message& message)
{
    const Player& p = message.players[0];

    for (State::Player& sp : state.players)
    {
        if (sp.id == p.id)
        {
            sp.dead = true;
            break;
        }
    }
}

void Client::renderConnect()
{
    sf::Event event;
    while (window.pollEvent(event))
    {
        if (event.type == sf::Event::Closed)
            closed = true;
    }
}

void Client::renderLobby()
{

}

void Client::renderGame()
{

}
