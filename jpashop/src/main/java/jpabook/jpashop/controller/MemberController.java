package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /*
     * 배열 member객체이기 때문에 유연하지않다.
     * api스펙에서 요구하지 않는것까지 응답한다.
     * */
    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        // result DTO를 만들어서 응답값에 유연성을 확보했다.
        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    /*
     * 엔티티로 요청을 받지말자
     * 엔티티로 유효성 검증 하기 어려움
     * 엔티티 수정시 api스펙 변경됨 (클라에서 큰 혼란 초래)
     * */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV2(@RequestBody Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /*
     * dto로 요청을 받자 내보낼때도 마찬가지
     * dto와 api 1:1매칭이된다
     * 엔티티 수정해도 api스펙는 변경되지 않는다.
     * */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.name);

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody UpdateMemberRequest request
    ) {
        memberService.update(id, request.getName());    // 커맨드와 쿼리는 분리한다.
        Member findMember = memberService.findOne(id);  // 커맨드와 쿼리는 분리한다.
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }
}