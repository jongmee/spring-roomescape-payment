package roomescape.member.dto.response;

import roomescape.member.domain.Member;

public record MemberResponse(
        Long id,
        String email,
        String name
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail().getValue(), member.getName());
    }
}
