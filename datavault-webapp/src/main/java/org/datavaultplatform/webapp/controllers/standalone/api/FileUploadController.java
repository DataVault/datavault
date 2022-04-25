package org.datavaultplatform.webapp.controllers.standalone.api;

import javax.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.model.test.Person;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping("/test")
@Slf4j
@Profile("standalone")
public class FileUploadController {

  @RequestMapping(value = "/upload/file/one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method =  RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String uploadFile1(
      @RequestPart("file") MultipartFile file) {
    long size  = file.getSize();
    String type = file.getContentType();
    String name = file.getName();
    String result = String.format("name[%s]type[%s]size[%d]",name,type,size);
    log.info(result);
    return result;
  }

  @RequestMapping(value = "/upload/file/two", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method =  RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String uploadFile(
      MultipartHttpServletRequest request
) {
    MultipartFile file = request.getFile("file");
    return this.uploadFile1(file);
  }

  @RequestMapping(value = "/upload/multi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method =  RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String uploadMulti(
      @RequestPart("file") MultipartFile file,
      @RequestPart("person") Person person) {
    long size  = file.getSize();
    String type = file.getContentType();
    String name = file.getName();
    String result = String.format("name[%s]type[%s]size[%d]first[%s]last[%s]",name,type,size,person.getFirst(),person.getLast());
    log.info(result);
    return result;
  }

}
