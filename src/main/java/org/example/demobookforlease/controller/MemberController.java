package org.example.demobookforlease.controller;

import jakarta.validation.Valid;
import org.example.demobookforlease.model.Member;
import org.example.demobookforlease.model.MemberStatus;
import org.example.demobookforlease.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String listMembers(@RequestParam(required = false) String query,
                              @RequestParam(required = false) MemberStatus status,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
        Page<Member> membersPage = memberService.searchMembers(query, status, PageRequest.of(page, size));

        model.addAttribute("membersPage", membersPage);
        model.addAttribute("statuses", MemberStatus.values());
        model.addAttribute("query", query);
        model.addAttribute("status", status);

        return "members/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("statuses", MemberStatus.values());
        return "members/form";
    }

    @PostMapping
    public String saveMember(@Valid @ModelAttribute("member") Member member,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", MemberStatus.values());
            return "members/form";
        }

        memberService.saveMember(member);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu độc giả thành công!");
        return "redirect:/members";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Member member = memberService.getMemberById(id);
        model.addAttribute("member", member);
        model.addAttribute("statuses", MemberStatus.values());
        return "members/form";
    }
}
