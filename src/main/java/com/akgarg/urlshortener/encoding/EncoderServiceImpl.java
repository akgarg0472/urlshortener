package com.akgarg.urlshortener.encoding;

import org.springframework.stereotype.Service;

@Service
public class EncoderServiceImpl implements EncoderService {

    @Override
    public String encode(final String longUrl) {
        return String.valueOf(longUrl.hashCode());
    }

}
