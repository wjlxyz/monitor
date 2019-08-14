package com.mason.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author puyol
 */
@Getter
@Setter
public class AllUrlResponse {
    private List<UrlInfo> allUrl;

    public AllUrlResponse(){
        this.allUrl = new ArrayList<>();
    }
}
