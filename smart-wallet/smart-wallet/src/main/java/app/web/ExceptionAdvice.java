package app.web;

import app.exception.UsernameAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionAdvice  {


    // 1. (First) POST HTTP Request -> /register -> redirect:/register
    // 2. (Second) GET HTTP Request -> /register -> register.html view

    // ВАЖНО: При редирект не връщаме @ResponseStatus(...)!!!

    // 1
    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExistError(RedirectAttributes redirectAttributes) {

        // Option 1
        //Autowire HttpServletRequest request
        //String username = request.getParameter("username");
        //String message = "%s is already in use!".formatted(username);

        redirectAttributes.addFlashAttribute("usernameAlreadyExist", "This username already exist");

        return "redirect:/register";
    }




    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
            {
            AccessDeniedException.class, // Когато се опитва да достъпи ендпойнт, до който не му е позволено/нямам достъп
            NoResourceFoundException.class, // Когато се опитва да достъпи невалиден ендпойнт
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        return new ModelAndView("not-found");
    }




    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}
