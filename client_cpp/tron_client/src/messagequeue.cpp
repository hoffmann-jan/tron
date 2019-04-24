#include "messagequeue.h"

bool MessageQueue::isEmpty() const
{
    std::lock_guard<std::mutex> lock(mutex);

    return queue.empty();
}

void MessageQueue::enqueue(const Message& message)
{
    std::lock_guard<std::mutex> lock(mutex);

    queue.push_back(message);
}

void MessageQueue::enqueue(const std::string& message)
{
    std::lock_guard<std::mutex> lock(mutex);

    enqueue(json(message).get<Message>());
}

Message MessageQueue::dequeue()
{
    std::lock_guard<std::mutex> lock(mutex);

    Message message = queue.front();

    queue.pop_front();

    return message;
}
