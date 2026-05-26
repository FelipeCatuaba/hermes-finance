package com.hermes.finance.domain.family;

import com.hermes.finance.util.NativeQueryCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyMemberRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private NativeQueryCatalog nativeQueryCatalog;

    @Mock
    private FamilyMemberRowMapper rowMapper;

    @InjectMocks
    private FamilyMemberRepository repository;

    @Test
    void shouldSaveMember() {
        when(nativeQueryCatalog.get("familyMember.insert")).thenReturn("insert");

        FamilyMember member = new FamilyMember();
        member.setUserId(UUID.randomUUID());
        member.setName("Isa");
        member.setRelation("Filha");
        member.setActive(true);

        FamilyMember saved = repository.save(member);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void shouldFindByUserAndById() {
        when(nativeQueryCatalog.get("familyMember.findByUserId")).thenReturn("list");
        when(nativeQueryCatalog.get("familyMember.findById")).thenReturn("find");

        FamilyMember member = new FamilyMember();
        member.setId(UUID.randomUUID());
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(FamilyMemberRowMapper.class)))
            .thenReturn(List.of(member));

        List<FamilyMember> list = repository.findByUserId(UUID.randomUUID(), true);
        Optional<FamilyMember> found = repository.findById(UUID.randomUUID());

        assertThat(list).hasSize(1);
        assertThat(found).isPresent();
    }

    @Test
    void shouldUpdateAndDeactivate() {
        when(nativeQueryCatalog.get("familyMember.update")).thenReturn("upd");
        when(nativeQueryCatalog.get("familyMember.deactivate")).thenReturn("deact");

        FamilyMember member = new FamilyMember();
        member.setId(UUID.randomUUID());
        member.setName("Nome");
        member.setRelation("Rel");
        member.setActive(true);

        FamilyMember updated = repository.update(member);
        repository.deactivate(member.getId());

        assertThat(updated).isSameAs(member);
    }
}
