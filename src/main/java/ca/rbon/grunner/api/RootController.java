package ca.rbon.grunner.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Root URI redirection to Swagger UI
 */
@Controller
public class RootController {

  @RequestMapping("/")
  public String index() {
    return "redirect:swagger-ui.html";
  }

}
