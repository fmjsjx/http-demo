package com.github.fmjsjx.demo.http.core.config;

import com.github.fmjsjx.demo.http.core.model.AuthToken;

public interface ShardingConfig<S> {

    S shard(int slot);

    default S shard(AuthToken token) {
        return shard(token.getSlot());
    }

}
