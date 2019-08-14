package com.mason.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author puyol
 */
@Getter
@Setter
public class UrlInfo {
    private List<String> patterns;
    private String className;
    private String programMethodName;
    private List<String> httpMethodName;
}
