#pragma once

#include <nlohmann/json.hpp>

using nlohmann::json;

struct Coordinate
{
    int id;
    int x;
    int y;
};

void to_json(json& j, const Coordinate& c)
{
    j = json({
        {"id", c.id},
        {"x", c.x},
        {"y", c.y}
    });
}

void from_json(const json& j, Coordinate& c)
{
    j.at("id").get_to(c.id);
    j.at("x").get_to(c.x);
    j.at("y").get_to(c.y);
}
