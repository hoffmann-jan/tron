#pragma once

#include "json/coordinate.h";

class Bike
{
public:
    void updatePosition(const Coordinate& coordinate);

    int x;
    int y;
    int id;
};