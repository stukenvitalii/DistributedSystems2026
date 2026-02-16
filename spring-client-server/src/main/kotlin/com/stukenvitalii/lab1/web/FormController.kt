package com.stukenvitalii.lab1.web

import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class FormController(
    private val submissionService: FormSubmissionService
) {

    @GetMapping("/")
    fun redirectToForm(): String = "redirect:/feedback"

    @GetMapping("/feedback")
    fun showForm(model: Model): String {
        if (!model.containsAttribute("submission")) {
            model.addAttribute("submission", SubmissionRequest())
        }
        model.addAttribute("records", submissionService.listSubmissions())
        return "submission"
    }

    @PostMapping("/feedback")
    fun submitForm(
        @Valid @ModelAttribute("submission") submissionRequest: SubmissionRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                "org.springframework.validation.BindingResult.submission",
                bindingResult
            )
            redirectAttributes.addFlashAttribute("submission", submissionRequest)
            return "redirect:/feedback"
        }
        val record = submissionService.registerSubmission(submissionRequest)
        redirectAttributes.addFlashAttribute("lastSubmission", record)
        return "redirect:/feedback"
    }
}
