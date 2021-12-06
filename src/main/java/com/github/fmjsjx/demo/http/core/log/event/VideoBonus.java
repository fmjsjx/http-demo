package com.github.fmjsjx.demo.http.core.log.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class VideoBonus extends EventData<VideoBonus> {

    @JsonProperty("video_id")
    private int videoId;
    private int count;
    private int remaining;

}
