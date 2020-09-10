package com.cariochi.recordo;


import com.cariochi.recordo.given.Assertion;
import com.cariochi.recordo.mockhttp.MockHttpContext;
import com.cariochi.recordo.mockhttp.MockHttpServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(RecordoExtension.class)
class GitHubTest {

    @EnableRecordo
    private okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

    @EnableRecordo
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private GitHub github = Feign.builder()
            .client(new OkHttpClient(client))
            .decoder(new GsonDecoder(gson))
            .encoder(new GsonEncoder(gson))
            .target(GitHub.class, "https://api.github.com");

    @Test
    void test_given_verify(
            @Given("/dto/input.json") Dto dto,
            @Given("/dto/output.json") Assertion<Dto> assertion
    ) {
        dto.setValue(dto.getValue() + "_updated");
        assertion.assertAsExpected(dto);
    }

    @Test
    @MockHttp("/http/gists.mock.json")
    void test_mock_http(
            @Given("/http/output.json") Assertion<List<GitHub.GistResponse>> responseAssertion
    ) {
        final List<GitHub.GistResponse> gists = github.getGists();
        responseAssertion.assertAsExpected(gists);
    }

    @Test
    void test_mock_http_with_variables(
            @MockHttp("/http/gists_with_variables.mock.json") MockHttpServer mockHttpServer,
            @Given("/http/output.json") Assertion<List<GitHub.GistResponse>> assertion
    ) {
        try (final MockHttpContext context = mockHttpServer.run()) {
            context.set("id1", "36387e79b940de553ad0b381afc29bf4");
            context.set("id2", "cc7e0f8678d69196387b623bd45f0f33");
            context.set("id3", "14c814e5561e8f03fce5f6d815af706c");
            final List<GitHub.GistResponse> gists = github.getGists();
            assertion.assertAsExpected(gists);
        }
    }

    @Data
    public static class Dto {
        private String value;
    }
}
