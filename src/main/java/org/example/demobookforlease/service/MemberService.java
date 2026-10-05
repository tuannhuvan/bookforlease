package org.example.demobookforlease.service;

import org.example.demobookforlease.exception.ResourceNotFoundException;
import org.example.demobookforlease.model.Member;
import org.example.demobookforlease.model.MemberStatus;
import org.example.demobookforlease.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public Page<Member> searchMembers(String query, MemberStatus status, Pageable pageable) {
        return memberRepository.searchMembers(query, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Member> getActiveMembers() {
        return memberRepository.findByStatus(MemberStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả có ID: " + id));
    }

    @Transactional
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}
