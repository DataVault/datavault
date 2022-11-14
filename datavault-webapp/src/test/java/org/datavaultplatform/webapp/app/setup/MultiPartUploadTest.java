package org.datavaultplatform.webapp.app.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartResolver;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ProfileStandalone
public class MultiPartUploadTest {

  @Value("classpath:images/logo-dvsmall.jpg")
  Resource dvLogo;

  @Value("classpath:person.json")
  Resource person;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  MultipartResolver mpResolver;

  MockMultipartFile file1;

  MockMultipartFile file2;

  long expectedFile1Size;

  @BeforeEach
  void setup() throws IOException {
    file1 = new MockMultipartFile("file",dvLogo.getFilename(), MediaType.IMAGE_JPEG_VALUE, dvLogo.getInputStream());
    file2 = new MockMultipartFile("person",person.getFilename(), MediaType.APPLICATION_JSON_VALUE, person.getInputStream());
    expectedFile1Size = dvLogo.contentLength();

    assertEquals(mpResolver, mockMvc.getDispatcherServlet().getMultipartResolver());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/test/upload/file/one","/test/upload/file/two"})
  public void testUploadFile(String uploadURL) throws Exception {

    String expectedResult = String.format("name[file]type[image/jpeg]size[%d]", expectedFile1Size);

    mockMvc.perform(
            multipart(uploadURL).file(file1).with(csrf()))
        .andExpect(content().string(expectedResult))
        .andExpect(status().isOk());
  }

  @Test
  public void testUploadMulti() throws Exception {

    String expectedResult = String.format("name[file]type[image/jpeg]size[%d]first[James]last[Bond]", expectedFile1Size);

    mockMvc.perform(multipart("/test/upload/multi")
            .file(file1)
            .file(file2)
            .with(csrf()))
        .andExpect(content().string(expectedResult))
        .andExpect(status().isOk());
  }

}
