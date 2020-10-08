package com.cariochi.recordo;


import com.cariochi.recordo.mockhttp.server.MockHttpServer;
import com.cariochi.recordo.mockhttp.server.interceptors.okhttp.OkHttpClientInterceptor;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.assertions.RecordoAssertion.assertAsJson;

@ExtendWith(RecordoExtension.class)
class GitHubTest {

    @EnableRecordo
    private okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

    private GitHub github = Feign.builder()
            .client(new OkHttpClient(client))
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .target(GitHub.class, "https://api.github.com");

    @Test
    void test_given_verify(
            @Read("/dto/input.json") Dto dto
    ) {
        dto.setValue(dto.getValue() + "_updated");
        assertAsJson(dto).isEqualTo("/dto/output.json");
    }

    @Test
    @WithMockHttpServer("/http/gists.mock.json")
    void test_mock_http() {
        final List<GitHub.GistResponse> gists = github.getGists();
        assertAsJson(gists).isEqualTo("/http/output.json");
    }

    @Test
    void test_mock_http_with_variables() {
        try (MockHttpServer mockServer =
                     new MockHttpServer("/http/gists_with_variables.mock.json", new OkHttpClientInterceptor(client))) {

            mockServer.set("id1", "36387e79b940de553ad0b381afc29bf4");
            mockServer.set("id2", "cc7e0f8678d69196387b623bd45f0f33");
            mockServer.set("id3", "14c814e5561e8f03fce5f6d815af706c");
            final List<GitHub.GistResponse> gists = github.getGists();

            assertAsJson(gists).isEqualTo("/http/output.json");
        }
    }

    @Data
    public static class Dto {
        private String value;
    }
}
