package org.example.videoapi21.Repository;

import org.example.videoapi21.Entity.Role;
import org.example.videoapi21.Enum.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
