package com.cariochi.recordo;

import feign.RequestLine;
import lombok.Data;

import java.util.List;
import java.util.Map;

public interface GitHub {

    @RequestLine("GET /gists?per_page=3&page=1")
    List<GistResponse> getGists();

    @Data
    class GistResponse {
        private String id;
        private String description;
        private String url;
        private String html_url;
        private Map<String, GistFile> files;
    }

    @Data
    class GistFile {
        private String filename;
        private String type;
        private String language;
        private String raw_url;
        private Integer size;
    }
}
