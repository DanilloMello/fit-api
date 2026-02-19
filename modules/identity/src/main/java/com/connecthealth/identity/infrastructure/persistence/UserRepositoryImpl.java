package com.connecthealth.identity.infrastructure.persistence;

import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = toJpa(user);
        UserJpaEntity saved = jpaRepository.save(jpaEntity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue()).map(UserRepositoryImpl::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue()).map(UserRepositoryImpl::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    private static UserJpaEntity toJpa(User user) {
        return new UserJpaEntity(
                user.getId().getValue(),
                user.getName(),
                user.getEmail().getValue(),
                user.getPasswordHash(),
                user.getPhone(),
                user.getPhotoUrl(),
                user.getCreatedAt()
        );
    }

    private static User toDomain(UserJpaEntity jpa) {
        return new User(
                UserId.of(jpa.getId()),
                jpa.getName(),
                new Email(jpa.getEmail()),
                jpa.getPasswordHash(),
                jpa.getPhone(),
                jpa.getPhotoUrl(),
                jpa.getCreatedAt()
        );
    }
}
