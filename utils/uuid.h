#ifndef UUID_H
#define UUID_H

#include <uuid/uuid.h>
#include <string>

namespace UUID
{

    // 生成唯一ID
    bool generate_uuid(std::string& output)
    {
        uuid_t uuid;
        uuid_generate(uuid);

        char uuid_str[37];
        uuid_unparse(uuid, uuid_str);

        output = uuid_str;

        return true;
    }

} // namespace UUID

#endif // UUID_H