#include "position.h"

void to_json(json& j, const Position& p)
{
    j = json({
        {"x", p.x},
        {"y", p.y},
        {"jumping", p.jumping}
    });
}

void from_json(const json& j, Position& p)
{
    j.at("x").get_to(p.x);
    j.at("y").get_to(p.y);
    j.at("jumping").get_to(p.jumping);
}
