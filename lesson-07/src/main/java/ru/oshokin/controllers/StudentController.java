package ru.oshokin.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.oshokin.persist.entities.Student;
import ru.oshokin.services.StudentService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private StudentService studentService;

    @GetMapping
    public String showStartPage(Model model, @RequestParam Map<String, String> parameters) {
        final int defaultParametersCount = 4;
        Map<String, Object> parsedParameters = new HashMap<>(parameters.size() + defaultParametersCount);

        parsedParameters.put("nameFilter", parameters.get("nameFilter"));
        parsedParameters.put("minAge", CommonUtils.castShort(parameters.get("minAge")));
        parsedParameters.put("maxAge", CommonUtils.castShort(parameters.get("maxAge")));

        parsedParameters.put("page", CommonUtils.getIntegerOrDefault(parameters.get("page"), 1));
        parsedParameters.put("size", CommonUtils.getIntegerOrDefault(parameters.get("size"), DEFAULT_PAGE_SIZE));
        parsedParameters.put("sortField", parameters.getOrDefault("sortField", "id"));
        parsedParameters.put("sortOrder", parameters.getOrDefault("sortOrder", "asc"));

        model.addAttribute("sizeAttribute", parsedParameters.get("size"));
        model.addAttribute("sortFieldAttribute", parsedParameters.get("sortField"));
        model.addAttribute("sortOrderAttribute", parsedParameters.get("sortOrder"));

        model.addAttribute("students", studentService.applyFilter(parsedParameters));

        return "students_list";
    }

    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable(value = "id") Long id, Model model) {
        logger.info("Editing student with id {}", id);
        model.addAttribute("student", studentService.findById(id).orElseThrow(() -> new NotFoundException()));
        return "student_update";
    }

    @GetMapping("/new")
    public String newStudent(Model model) {
        logger.info("Adding new student");
        model.addAttribute("student", new Student());
        return "student_create";
    }

    @PostMapping("/update")
    public String updateStudent(@Valid Student student, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "student_update";
        studentService.save(student);
        return "redirect:/students";
    }

    @PostMapping("/insert")
    public String insertStudent(@Valid Student student, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "student_create";
        studentService.save(student);
        return "redirect:/students";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable(value = "id") Long id, Model model) {
        logger.info("Deleting student with id {}", id);
        studentService.deleteById(id);
        return "redirect:/students";
    }

    @ExceptionHandler
    public ModelAndView notFoundExceptionHandler(NotFoundException e) {
        ModelAndView modelAndView = new ModelAndView("page_not_found");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

}