#include "client.h"

Client::Client()
    : window(
        sf::VideoMode(500, 500), 
        "Tron C++ Client", 
        sf::Style::Default
    )
    , state(State::Connect)
{
    window.setActive(true);
}

Client::~Client()
{
    window.close();
}

void Client::run()
{
    while (window.isOpen())
    {
        sf::Event event;
        while (window.pollEvent(event))
        {
            if (event.type == sf::Event::Closed)
                return;
        }

        if (!queue.isEmpty())
        {
            processMessage(queue.dequeue());

            switch (state)
            {
            case State::Connect:
                renderConnect();
                break;

            case State::Lobby:
                renderLobby();
                break;

            case State::Game:
                renderGame();
                break;
            }
        }
    }
}

void Client::processMessage(const Message& message)
{

}

void Client::renderConnect()
{

}

void Client::renderLobby()
{

}

void Client::renderGame()
{

}
