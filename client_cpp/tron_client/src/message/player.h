#pragma once

#include <string>

#include <nlohmann/json.hpp>

#include "position.h"

using json = nlohmann::json;

struct Player
{
    int id;
    std::string name;
    int color;
    Position position;
};

void to_json(json& j, const Player& p);
void from_json(const json& j, Player& p);
