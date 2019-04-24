#include "player.h"

void to_json(json& j, const Player& p)
{
    j = json({
        {"id", p.id},
        {"name", p.name},
        {"color", p.color},
        {"position", p.position}
    });

}

void from_json(const json& j, Player& p)
{
    j.at("id").get_to(p.id);
    j.at("name").get_to(p.name);
    j.at("color").get_to(p.color);
    j.at("position").get_to(p.color);
}
