package org.example.demobookforlease.controller;

import jakarta.validation.Valid;
import org.example.demobookforlease.dto.FinePolicyForm;
import org.example.demobookforlease.model.FinePolicy;
import org.example.demobookforlease.service.FinePolicyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fine-policies")
public class FinePolicyController {

    private final FinePolicyService finePolicyService;

    public FinePolicyController(FinePolicyService finePolicyService) {
        this.finePolicyService = finePolicyService;
    }

    @GetMapping
    public String listPolicies(Model model) {
        FinePolicy activePolicy = finePolicyService.getActivePolicy();
        FinePolicyForm form = new FinePolicyForm(
                activePolicy.getDailyFineAmount(),
                activePolicy.getMaxFineAmount(),
                activePolicy.getGraceDays(),
                true
        );

        model.addAttribute("activePolicy", activePolicy);
        model.addAttribute("policies", finePolicyService.getAllPolicies());
        model.addAttribute("policyForm", form);
        return "fine-policies/list";
    }

    @PostMapping
    public String savePolicy(@Valid @ModelAttribute("policyForm") FinePolicyForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePolicy", finePolicyService.getActivePolicy());
            model.addAttribute("policies", finePolicyService.getAllPolicies());
            return "fine-policies/list";
        }

        finePolicyService.savePolicy(form);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cấu hình phí phạt thành công!");
        return "redirect:/fine-policies";
    }
}
