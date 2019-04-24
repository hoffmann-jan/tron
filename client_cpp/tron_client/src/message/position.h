#pragma once

#include <nlohmann/json.hpp>

using json = nlohmann::json;

struct Position
{
    int x;         // Player x coordinate
    int y;         // Player y coordinate
    bool jumping;  // Player jumping status
};

void to_json(json& j, const Position& p);
void from_json(const json& j, Position& p);
