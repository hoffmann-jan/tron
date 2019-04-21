#pragma once

#include <vector>

#include <nlohmann/json.hpp>

#include "coordinate.h"
#include "enums.h"

using nlohmann::json;

struct Message
{
    Type type;
    Move move;

    int id;

    std::vector<Coordinate> coordinates;
};

void to_json(json& j, const Message& m)
{
    j = json({
        {"type", m.type},
        {"move", m.move},
        {"id", m.id},
        {"coordinates", m.coordinates}
    });
}

void from_json(const json& j, Message& m)
{
    j.at("type").get_to(m.type);
    j.at("move").get_to(m.move);
    j.at("id").get_to(m.id);
    j.at("coordinates").get_to(m.coordinates);
}
