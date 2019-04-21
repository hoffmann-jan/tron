#include "bike.h"

void Bike::updatePosition(const Coordinate& coordinate)
{
    if (coordinate.id == id)
    {
        x = coordinate.x;
        y = coordinate.y;
    }
}
