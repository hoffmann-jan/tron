#include <memory>

#include "client.h"

int main()
{
    std::make_shared<Client>()->run();

    return 0;

    //sf::TcpSocket socket;
    //sf::Socket::Status status = socket.connect("10.202.134.229", 4321);
    //if (status == sf::Socket::Status::Done)
    //{
    //    std::size_t received;
    //    char data[1000];
    //    if (socket.receive(data, 1000, received) == sf::Socket::Done)
    //    {

    //    }
    //}
}