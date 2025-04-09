#include <iostream>
#include "../utils/uuid.h"


int main(int argc, char *argv[])
{
    std::string output;
    UUID::generate_uuid(&output);
    std::cout << output << std::endl;

    return 0;
}
