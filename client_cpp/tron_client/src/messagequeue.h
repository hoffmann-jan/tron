#pragma once

#include <list>
#include <mutex>
#include <string>

#include "message/message.h"

class MessageQueue
{
public:
    bool isEmpty() const;

    void enqueue(const Message& message);
    void enqueue(const std::string& message);

    Message dequeue();

private:
    std::list<Message> queue;
    mutable std::mutex mutex;
};

