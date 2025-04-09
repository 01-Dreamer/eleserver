#ifndef BASE64_H
#define BASE64_H

#include <iostream>
#include <string>
#include <sstream>
#include <b64/encode.h>
#include <b64/decode.h>

namespace BASE64 {

    // Base64 编码函数
    bool encode(const std::string *input, std::string &output)
    {
        try
        {
            std::istringstream input_stream(*input);
            std::ostringstream output_stream;

            base64::encoder encoder;
            encoder.encode(input_stream, output_stream);

            const std::string& out = output_stream.str();
            output.clear();
            output.reserve(out.size());
            for(char c : out)
               if(c != '\n')
                  output += c;
    
            return true;
        }
        catch (const std::exception &e)
        {
            std::cerr << "failed to encode: " << e.what() << std::endl;
            return false;
        }
    }

    // base64 解码函数
    bool decode(const std::string *input, std::string &output)
    {
        try
        {
            std::istringstream input_stream(*input);
            std::ostringstream output_stream;

            base64::decoder decoder;
            decoder.decode(input_stream, output_stream);

            output = output_stream.str();
            return true;
        }
        catch (const std::exception &e)
        {
            std::cerr << "failed to decode: " << e.what() << std::endl;
            return false;
        }
    }

} // namespace BASE64

#endif // BASE64_H