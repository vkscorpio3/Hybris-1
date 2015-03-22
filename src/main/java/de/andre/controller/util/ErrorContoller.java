package de.andre.controller.util;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by andreika on 3/22/2015.
 */

@Controller
public class ErrorContoller {
    @RequestMapping("/error")
    public String error(final HttpServletRequest request, final Model model) {
        model.addAttribute("errorCode", request.getAttribute("javax.servlet.error.status_code"));
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        final String errorMessage = (null == throwable) ? null : throwable.getMessage();
        model.addAttribute("errorMessage", errorMessage);

        return "error";
    }

    @RequestMapping("/simulateError")
    public void simulateError() {
        throw new RuntimeException("This is a simulated error message");
    }
}
