package com.github.fmjsjx.demo.http.api.video;

import com.github.fmjsjx.demo.http.api.ItemsResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VideoBonusInfo extends ItemsResult<VideoBonusInfo> {

    private int id;
    private int remaining;
    
}
