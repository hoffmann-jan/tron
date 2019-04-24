#include "message.h"

void to_json(json& j, const Message& m)
{
    j = json({
        {"type", m.type},
        {"action", m.action},
        {"lobbyId", m.lobbyId},
        {"length", m.length},
        {"players", m.players}
    });

}

void from_json(const json& j, Message& m)
{
    j.at("type").get_to(m.type);
    j.at("action").get_to(m.action);
    j.at("lobbyId").get_to(m.lobbyId);
    j.at("length").get_to(m.length);
    j.at("players").get_to(m.players);
}
